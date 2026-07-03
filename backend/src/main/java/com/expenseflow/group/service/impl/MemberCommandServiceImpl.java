package com.expenseflow.group.service.impl;

import com.expenseflow.entity.UserEntity;
import com.expenseflow.exception.SecurityHardeningException;
import com.expenseflow.group.dto.GroupDto;
import com.expenseflow.group.dto.JoinGroupRequest;
import com.expenseflow.group.dto.TransferOwnershipRequest;
import com.expenseflow.group.entity.*;
import com.expenseflow.group.event.*;
import com.expenseflow.group.exception.*;
import com.expenseflow.group.mapper.GroupMapper;
import com.expenseflow.group.repository.GroupActivityRepository;
import com.expenseflow.group.repository.GroupMemberRepository;
import com.expenseflow.group.repository.GroupRepository;
import com.expenseflow.group.service.MemberCommandService;
import com.expenseflow.group.validation.GroupValidator;
import com.expenseflow.group.validation.OwnershipValidator;
import com.expenseflow.group.validation.PermissionValidator;
import com.expenseflow.group.validation.RoomCodeValidator;
import com.expenseflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandServiceImpl implements MemberCommandService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupActivityRepository groupActivityRepository;
    private final GroupValidator groupValidator;
    private final PermissionValidator permissionValidator;
    private final OwnershipValidator ownershipValidator;
    private final RoomCodeValidator roomCodeValidator;
    private final GroupMapper groupMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public GroupDto joinGroup(JoinGroupRequest request, String currentUserId) {
        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new SecurityHardeningException("User not found.", "USR_001"));

        GroupCode groupCode = new GroupCode(request.roomCode());
        GroupEntity group = groupRepository.findByGroupCodeAndIsDeletedFalse(groupCode)
                .orElseThrow(() -> new InvalidRoomCodeException("Invalid room code"));

        roomCodeValidator.validateRoomCodeActive(group);

        Optional<GroupMemberEntity> existingMemberOpt = groupMemberRepository
                .findByGroupIdAndUserIdAndIsDeletedFalse(group.getId(), currentUserId);

        GroupMemberEntity member;
        if (existingMemberOpt.isPresent()) {
            member = existingMemberOpt.get();
            if (member.getStatus() == GroupMemberStatus.ACTIVE) {
                throw new DuplicateMemberException("User is already a member of this group");
            }
            // Reactivate historical member
            member.setStatus(GroupMemberStatus.ACTIVE);
            member.setRole(GroupRole.MEMBER);
            member.setJoinedAt(LocalDateTime.now());
        } else {
            member = GroupMemberEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .group(group)
                    .user(user)
                    .role(GroupRole.MEMBER)
                    .status(GroupMemberStatus.ACTIVE)
                    .joinedAt(LocalDateTime.now())
                    .build();
        }

        GroupMemberEntity savedMember = groupMemberRepository.save(member);

        // Log joined activity
        Map<String, Object> metadata = Map.of(
            "actorId", user.getId(),
            "actorName", user.getFullName(),
            "targetUserId", user.getId(),
            "targetUserName", user.getFullName()
        );
        logActivity(group, user, ActivityType.MEMBER_JOINED, metadata);

        eventPublisher.publishEvent(new MemberJoinedEvent(this, group.getId(), currentUserId));

        return groupMapper.toDto(group, currentUserId, savedMember.getRole().name());
    }

    @Override
    public void leaveGroup(String groupId, String currentUserId) {
        GroupEntity group = fetchActiveGroup(groupId);
        GroupMemberEntity member = fetchActiveMember(groupId, currentUserId);

        ownershipValidator.validateOwnerLeaving(member.getRole());

        UserEntity user = member.getUser();

        member.setStatus(GroupMemberStatus.LEFT);
        groupMemberRepository.save(member);

        Map<String, Object> metadata = Map.of(
            "actorId", user.getId(),
            "actorName", user.getFullName(),
            "targetUserId", user.getId(),
            "targetUserName", user.getFullName()
        );
        logActivity(group, user, ActivityType.MEMBER_LEFT, metadata);

        eventPublisher.publishEvent(new MemberLeftEvent(this, groupId, currentUserId));
    }

    @Override
    public void removeMember(String groupId, String memberId, String currentUserId) {
        if (memberId.equals(currentUserId)) {
            throw new PermissionDeniedException("Cannot remove yourself. Use leave instead.");
        }

        GroupEntity group = fetchActiveGroup(groupId);
        GroupMemberEntity actor = fetchActiveMember(groupId, currentUserId);
        GroupMemberEntity target = fetchActiveMember(groupId, memberId);

        permissionValidator.validateAdminOrOwner(actor.getRole());

        // Enforce hierarchy constraints
        if (actor.getRole() == GroupRole.ADMIN) {
            if (target.getRole() == GroupRole.OWNER || target.getRole() == GroupRole.ADMIN) {
                throw new PermissionDeniedException("Admins cannot remove other admins or owners");
            }
        }

        UserEntity actorUser = actor.getUser();
        UserEntity targetUser = target.getUser();

        target.setStatus(GroupMemberStatus.KICKED);
        groupMemberRepository.save(target);

        Map<String, Object> metadata = Map.of(
            "actorId", actorUser.getId(),
            "actorName", actorUser.getFullName(),
            "targetUserId", targetUser.getId(),
            "targetUserName", targetUser.getFullName()
        );
        logActivity(group, actorUser, ActivityType.MEMBER_REMOVED, metadata);

        eventPublisher.publishEvent(new MemberRemovedEvent(this, groupId, memberId, currentUserId));
    }

    @Override
    public void promoteMember(String groupId, String memberId, String currentUserId) {
        GroupEntity group = fetchActiveGroup(groupId);
        GroupMemberEntity actor = fetchActiveMember(groupId, currentUserId);
        GroupMemberEntity target = fetchActiveMember(groupId, memberId);

        permissionValidator.validateOwnerOnly(actor.getRole());

        if (target.getRole() == GroupRole.ADMIN || target.getRole() == GroupRole.OWNER) {
            return; // Already has elevated permissions
        }

        UserEntity actorUser = actor.getUser();
        UserEntity targetUser = target.getUser();

        target.setRole(GroupRole.ADMIN);
        groupMemberRepository.save(target);

        Map<String, Object> metadata = Map.of(
            "actorId", actorUser.getId(),
            "actorName", actorUser.getFullName(),
            "targetUserId", targetUser.getId(),
            "targetUserName", targetUser.getFullName(),
            "oldRole", "MEMBER",
            "newRole", "ADMIN"
        );
        logActivity(group, actorUser, ActivityType.ROLE_CHANGED, metadata);

        eventPublisher.publishEvent(new RoleChangedEvent(this, groupId, memberId, "MEMBER", "ADMIN", currentUserId));
    }

    @Override
    public void demoteMember(String groupId, String memberId, String currentUserId) {
        GroupEntity group = fetchActiveGroup(groupId);
        GroupMemberEntity actor = fetchActiveMember(groupId, currentUserId);
        GroupMemberEntity target = fetchActiveMember(groupId, memberId);

        permissionValidator.validateOwnerOnly(actor.getRole());

        if (target.getRole() != GroupRole.ADMIN) {
            throw new PermissionDeniedException("Only admins can be demoted");
        }

        UserEntity actorUser = actor.getUser();
        UserEntity targetUser = target.getUser();

        target.setRole(GroupRole.MEMBER);
        groupMemberRepository.save(target);

        Map<String, Object> metadata = Map.of(
            "actorId", actorUser.getId(),
            "actorName", actorUser.getFullName(),
            "targetUserId", targetUser.getId(),
            "targetUserName", targetUser.getFullName(),
            "oldRole", "ADMIN",
            "newRole", "MEMBER"
        );
        logActivity(group, actorUser, ActivityType.ROLE_CHANGED, metadata);

        eventPublisher.publishEvent(new RoleChangedEvent(this, groupId, memberId, "ADMIN", "MEMBER", currentUserId));
    }

    @Override
    public void transferOwnership(String groupId, TransferOwnershipRequest request, String currentUserId) {
        if (request.newOwnerId().equals(currentUserId)) {
            throw new PermissionDeniedException("You are already the owner of this group");
        }

        GroupEntity group = fetchActiveGroup(groupId);
        GroupMemberEntity actor = fetchActiveMember(groupId, currentUserId);
        GroupMemberEntity target = fetchActiveMember(groupId, request.newOwnerId());

        permissionValidator.validateOwnerOnly(actor.getRole());

        UserEntity actorUser = actor.getUser();
        UserEntity targetUser = target.getUser();

        // Perform atomic transfer
        group.setOwner(targetUser);
        groupRepository.save(group);

        actor.setRole(GroupRole.ADMIN);
        groupMemberRepository.save(actor);

        target.setRole(GroupRole.OWNER);
        groupMemberRepository.save(target);

        Map<String, Object> metadata = Map.of(
            "actorId", actorUser.getId(),
            "actorName", actorUser.getFullName(),
            "targetUserId", targetUser.getId(),
            "targetUserName", targetUser.getFullName()
        );
        logActivity(group, actorUser, ActivityType.OWNER_TRANSFERRED, metadata);

        eventPublisher.publishEvent(new OwnershipTransferredEvent(this, groupId, currentUserId, request.newOwnerId()));
    }

    private GroupEntity fetchActiveGroup(String groupId) {
        return groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));
    }

    private GroupMemberEntity fetchActiveMember(String groupId, String userId) {
        return groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, userId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new PermissionDeniedException("User is not an active member of this group"));
    }

    private void logActivity(GroupEntity group, UserEntity user, ActivityType actionType, Map<String, Object> metadata) {
        GroupActivityEntity activity = GroupActivityEntity.builder()
                .id(UUID.randomUUID().toString())
                .group(group)
                .user(user)
                .actionType(actionType)
                .metadata(metadata)
                .createdBy(user.getFullName())
                .createdAt(LocalDateTime.now())
                .build();
        groupActivityRepository.save(activity);
    }
}
