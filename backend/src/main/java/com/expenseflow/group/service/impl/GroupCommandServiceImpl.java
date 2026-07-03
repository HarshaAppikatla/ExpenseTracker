package com.expenseflow.group.service.impl;

import com.expenseflow.entity.UserEntity;
import com.expenseflow.exception.SecurityHardeningException;
import com.expenseflow.group.dto.CreateGroupRequest;
import com.expenseflow.group.dto.GroupDto;
import com.expenseflow.group.dto.UpdateGroupRequest;
import com.expenseflow.group.entity.*;
import com.expenseflow.group.event.*;
import com.expenseflow.group.exception.GroupNotFoundException;
import com.expenseflow.group.exception.PermissionDeniedException;
import com.expenseflow.group.mapper.GroupMapper;
import com.expenseflow.group.repository.GroupActivityRepository;
import com.expenseflow.group.repository.GroupMemberRepository;
import com.expenseflow.group.repository.GroupRepository;
import com.expenseflow.group.service.GroupAccessService;
import com.expenseflow.group.service.GroupCommandService;
import com.expenseflow.group.validation.GroupValidator;
import com.expenseflow.group.validation.PermissionValidator;
import com.expenseflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupCommandServiceImpl implements GroupCommandService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupActivityRepository groupActivityRepository;
    private final GroupValidator groupValidator;
    private final PermissionValidator permissionValidator;
    private final GroupMapper groupMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final GroupAccessService groupAccessService;

    @Override
    public GroupDto createGroup(CreateGroupRequest request, String currentUserId) {
        UserEntity owner = userRepository.findById(currentUserId)
                .orElseThrow(() -> new SecurityHardeningException("User not found.", "USR_001"));

        GroupCode groupCode = groupAccessService.generateGroupCode();

        GroupEntity group = GroupEntity.builder()
                .id(UUID.randomUUID().toString())
                .name(request.name())
                .description(request.description())
                .currency(request.currency())
                .groupCode(groupCode)
                .owner(owner)
                .settings(GroupSettings.builder()
                        .allowJoinByCode(true)
                        .allowJoinByLink(true)
                        .archived(false)
                        .build())
                .build();

        group.setCreatedBy(owner.getFullName());
        group.setUpdatedBy(owner.getFullName());

        GroupEntity savedGroup = groupRepository.save(group);

        // Add creator as owner member
        GroupMemberEntity member = GroupMemberEntity.builder()
                .id(UUID.randomUUID().toString())
                .group(savedGroup)
                .user(owner)
                .role(GroupRole.OWNER)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        groupMemberRepository.save(member);

        // Log activity
        logActivity(savedGroup, owner, ActivityType.GROUP_CREATED, createActivityMetadata(owner));

        eventPublisher.publishEvent(new GroupCreatedEvent(this, savedGroup.getId(), savedGroup.getName(), owner.getId()));

        return groupMapper.toDto(savedGroup, currentUserId, GroupRole.OWNER.name());
    }

    @Override
    public GroupDto updateGroup(String groupId, UpdateGroupRequest request, String currentUserId) {
        GroupEntity group = fetchActiveGroup(groupId);
        GroupMemberEntity member = fetchActiveMember(groupId, currentUserId);
        
        permissionValidator.validateAdminOrOwner(member.getRole());
        groupValidator.validateNotArchived(group);

        UserEntity actor = member.getUser();

        if (request.name() != null) {
            group.setName(request.name());
        }
        if (request.description() != null) {
            group.setDescription(request.description());
        }
        if (request.settings() != null) {
            GroupSettings settings = group.getSettings();
            group.setSettings(GroupSettings.builder()
                    .allowJoinByCode(request.settings().allowJoinByCode())
                    .allowJoinByLink(request.settings().allowJoinByLink())
                    .archived(settings.isArchived()) // Archive status updated only via explicit commands
                    .build());
        }

        group.setUpdatedBy(actor.getFullName());
        GroupEntity updatedGroup = groupRepository.save(group);

        logActivity(updatedGroup, actor, ActivityType.GROUP_UPDATED, createActivityMetadata(actor));

        eventPublisher.publishEvent(new GroupUpdatedEvent(this, updatedGroup.getId(), currentUserId));

        return groupMapper.toDto(updatedGroup, currentUserId, member.getRole().name());
    }

    @Override
    public GroupDto archiveGroup(String groupId, String currentUserId) {
        GroupEntity group = fetchActiveGroup(groupId);
        GroupMemberEntity member = fetchActiveMember(groupId, currentUserId);

        permissionValidator.validateAdminOrOwner(member.getRole());
        
        if (group.getSettings().isArchived()) {
            return groupMapper.toDto(group, currentUserId, member.getRole().name());
        }

        UserEntity actor = member.getUser();

        group.setSettings(GroupSettings.builder()
                .allowJoinByCode(false) // Disable code join on archive
                .allowJoinByLink(false) // Disable link join on archive
                .archived(true)
                .build());
        group.setUpdatedBy(actor.getFullName());
        GroupEntity archivedGroup = groupRepository.save(group);

        logActivity(archivedGroup, actor, ActivityType.GROUP_ARCHIVED, createActivityMetadata(actor));

        eventPublisher.publishEvent(new GroupArchivedEvent(this, archivedGroup.getId(), currentUserId));

        return groupMapper.toDto(archivedGroup, currentUserId, member.getRole().name());
    }

    @Override
    public GroupDto restoreGroup(String groupId, String currentUserId) {
        GroupEntity group = fetchActiveGroup(groupId);
        GroupMemberEntity member = fetchActiveMember(groupId, currentUserId);

        permissionValidator.validateAdminOrOwner(member.getRole());

        if (!group.getSettings().isArchived()) {
            return groupMapper.toDto(group, currentUserId, member.getRole().name());
        }

        UserEntity actor = member.getUser();

        group.setSettings(GroupSettings.builder()
                .allowJoinByCode(true)
                .allowJoinByLink(true)
                .archived(false)
                .build());
        group.setUpdatedBy(actor.getFullName());
        GroupEntity restoredGroup = groupRepository.save(group);

        logActivity(restoredGroup, actor, ActivityType.GROUP_RESTORED, createActivityMetadata(actor));

        eventPublisher.publishEvent(new GroupRestoredEvent(this, restoredGroup.getId(), currentUserId));

        return groupMapper.toDto(restoredGroup, currentUserId, member.getRole().name());
    }

    @Override
    public void deleteGroup(String groupId, String currentUserId) {
        GroupEntity group = fetchActiveGroup(groupId);
        GroupMemberEntity member = fetchActiveMember(groupId, currentUserId);

        permissionValidator.validateOwnerOnly(member.getRole());

        UserEntity actor = member.getUser();

        group.setDeleted(true);
        group.setDeletedAt(LocalDateTime.now());
        group.setDeletedBy(actor.getFullName());
        groupRepository.save(group);

        logActivity(group, actor, ActivityType.GROUP_DELETED, createActivityMetadata(actor));

        eventPublisher.publishEvent(new GroupDeletedEvent(this, group.getId(), currentUserId));
    }

    private GroupEntity fetchActiveGroup(String groupId) {
        return groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));
    }

    private GroupMemberEntity fetchActiveMember(String groupId, String userId) {
        return groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, userId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new PermissionDeniedException("User is not an active member of this group"));
    }

    private Map<String, Object> createActivityMetadata(UserEntity user) {
        return Map.of(
            "actorId", user.getId(),
            "actorName", user.getFullName()
        );
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
