package com.expenseflow.group.service;

import com.expenseflow.entity.UserEntity;
import com.expenseflow.group.dto.GroupDto;
import com.expenseflow.group.dto.JoinGroupRequest;
import com.expenseflow.group.dto.TransferOwnershipRequest;
import com.expenseflow.group.entity.*;
import com.expenseflow.group.event.*;
import com.expenseflow.group.exception.DuplicateMemberException;
import com.expenseflow.group.exception.PermissionDeniedException;
import com.expenseflow.group.mapper.GroupMapper;
import com.expenseflow.group.repository.GroupActivityRepository;
import com.expenseflow.group.repository.GroupMemberRepository;
import com.expenseflow.group.repository.GroupRepository;
import com.expenseflow.group.service.impl.MemberCommandServiceImpl;
import com.expenseflow.group.validation.OwnershipValidator;
import com.expenseflow.group.validation.PermissionValidator;
import com.expenseflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberCommandServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private GroupActivityRepository groupActivityRepository;

    @Mock
    private PermissionValidator permissionValidator;

    @Mock
    private OwnershipValidator ownershipValidator;

    @Mock
    private GroupMapper groupMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private GroupAccessService groupAccessService;

    @InjectMocks
    private MemberCommandServiceImpl memberCommandService;

    @Test
    void joinGroup_ShouldCreateNewActiveMembershipAndPublishEvent() {
        String userId = "user-123";
        String codeStr = "XYZ12345";
        UserEntity user = UserEntity.builder().id(userId).fullName("New Member").build();
        GroupEntity group = GroupEntity.builder().id("group-123").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(groupAccessService.validateAndResolveCode(codeStr)).thenReturn(group);
        when(groupMemberRepository.findByGroupIdAndUserIdAndIsDeletedFalse(group.getId(), userId)).thenReturn(Optional.empty());
        when(groupMemberRepository.save(any(GroupMemberEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        memberCommandService.joinGroup(new JoinGroupRequest(codeStr), userId);

        ArgumentCaptor<GroupMemberEntity> memberCaptor = ArgumentCaptor.forClass(GroupMemberEntity.class);
        verify(groupMemberRepository).save(memberCaptor.capture());
        GroupMemberEntity savedMember = memberCaptor.getValue();
        assertThat(savedMember.getUser()).isSameAs(user);
        assertThat(savedMember.getRole()).isEqualTo(GroupRole.MEMBER);
        assertThat(savedMember.getStatus()).isEqualTo(GroupMemberStatus.ACTIVE);

        verify(eventPublisher).publishEvent(any(MemberJoinedEvent.class));
    }

    @Test
    void joinGroup_ShouldThrowExceptionIfAlreadyActiveMember() {
        String userId = "user-123";
        String codeStr = "XYZ12345";
        UserEntity user = UserEntity.builder().id(userId).build();
        GroupEntity group = GroupEntity.builder().id("group-123").build();
        GroupMemberEntity existing = GroupMemberEntity.builder().status(GroupMemberStatus.ACTIVE).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(groupAccessService.validateAndResolveCode(codeStr)).thenReturn(group);
        when(groupMemberRepository.findByGroupIdAndUserIdAndIsDeletedFalse(group.getId(), userId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> memberCommandService.joinGroup(new JoinGroupRequest(codeStr), userId))
                .isInstanceOf(DuplicateMemberException.class)
                .hasMessageContaining("User is already a member of this group");
    }

    @Test
    void joinGroup_ShouldReactivateLeftMember() {
        String userId = "user-123";
        String codeStr = "XYZ12345";
        UserEntity user = UserEntity.builder().id(userId).build();
        GroupEntity group = GroupEntity.builder().id("group-123").build();
        GroupMemberEntity existing = GroupMemberEntity.builder().status(GroupMemberStatus.LEFT).role(GroupRole.MEMBER).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(groupAccessService.validateAndResolveCode(codeStr)).thenReturn(group);
        when(groupMemberRepository.findByGroupIdAndUserIdAndIsDeletedFalse(group.getId(), userId)).thenReturn(Optional.of(existing));
        when(groupMemberRepository.save(any(GroupMemberEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        memberCommandService.joinGroup(new JoinGroupRequest(codeStr), userId);

        assertThat(existing.getStatus()).isEqualTo(GroupMemberStatus.ACTIVE);
        verify(groupMemberRepository).save(existing);
    }

    @Test
    void leaveGroup_ShouldUpdateStatusAndPublishEvent() {
        String groupId = "group-123";
        String userId = "user-123";
        GroupEntity group = GroupEntity.builder().id(groupId).build();
        UserEntity user = UserEntity.builder().id(userId).fullName("Leaving Member").build();
        GroupMemberEntity member = GroupMemberEntity.builder().group(group).user(user).role(GroupRole.MEMBER).status(GroupMemberStatus.ACTIVE).build();

        when(groupRepository.findByIdAndIsDeletedFalse(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, userId, GroupMemberStatus.ACTIVE)).thenReturn(Optional.of(member));

        memberCommandService.leaveGroup(groupId, userId);

        verify(ownershipValidator).validateOwnerLeaving(GroupRole.MEMBER);
        assertThat(member.getStatus()).isEqualTo(GroupMemberStatus.LEFT);
        verify(eventPublisher).publishEvent(any(MemberLeftEvent.class));
    }

    @Test
    void removeMember_ShouldAllowAdminToRemoveMember() {
        String groupId = "group-123";
        String actorId = "admin-123";
        String targetId = "member-123";

        GroupEntity group = GroupEntity.builder().id(groupId).build();
        UserEntity admin = UserEntity.builder().id(actorId).fullName("Admin User").build();
        UserEntity target = UserEntity.builder().id(targetId).fullName("Target User").build();

        GroupMemberEntity actorMember = GroupMemberEntity.builder().group(group).user(admin).role(GroupRole.ADMIN).build();
        GroupMemberEntity targetMember = GroupMemberEntity.builder().group(group).user(target).role(GroupRole.MEMBER).build();

        when(groupRepository.findByIdAndIsDeletedFalse(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, actorId, GroupMemberStatus.ACTIVE)).thenReturn(Optional.of(actorMember));
        when(groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, targetId, GroupMemberStatus.ACTIVE)).thenReturn(Optional.of(targetMember));

        memberCommandService.removeMember(groupId, targetId, actorId);

        verify(permissionValidator).validateAdminOrOwner(GroupRole.ADMIN);
        assertThat(targetMember.getStatus()).isEqualTo(GroupMemberStatus.KICKED);
        verify(eventPublisher).publishEvent(any(MemberRemovedEvent.class));
    }

    @Test
    void removeMember_ShouldPreventAdminFromRemovingOtherAdmins() {
        String groupId = "group-123";
        String actorId = "admin-123";
        String targetId = "admin-456";

        GroupEntity group = GroupEntity.builder().id(groupId).build();
        GroupMemberEntity actorMember = GroupMemberEntity.builder().group(group).role(GroupRole.ADMIN).build();
        GroupMemberEntity targetMember = GroupMemberEntity.builder().group(group).role(GroupRole.ADMIN).build();

        when(groupRepository.findByIdAndIsDeletedFalse(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, actorId, GroupMemberStatus.ACTIVE)).thenReturn(Optional.of(actorMember));
        when(groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, targetId, GroupMemberStatus.ACTIVE)).thenReturn(Optional.of(targetMember));

        assertThatThrownBy(() -> memberCommandService.removeMember(groupId, targetId, actorId))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("Admins cannot remove other admins or owners");
    }

    @Test
    void removeMember_ShouldPreventSelfKick() {
        String groupId = "group-123";
        String actorId = "user-123";

        assertThatThrownBy(() -> memberCommandService.removeMember(groupId, actorId, actorId))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("Cannot remove yourself");
    }

    @Test
    void promoteMember_ShouldElevateRoleToAdmin() {
        String groupId = "group-123";
        String actorId = "owner-123";
        String targetId = "member-123";

        GroupEntity group = GroupEntity.builder().id(groupId).build();
        UserEntity ownerUser = UserEntity.builder().id(actorId).fullName("Owner").build();
        UserEntity targetUser = UserEntity.builder().id(targetId).fullName("Member").build();

        GroupMemberEntity actorMember = GroupMemberEntity.builder().group(group).user(ownerUser).role(GroupRole.OWNER).build();
        GroupMemberEntity targetMember = GroupMemberEntity.builder().group(group).user(targetUser).role(GroupRole.MEMBER).build();

        when(groupRepository.findByIdAndIsDeletedFalse(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, actorId, GroupMemberStatus.ACTIVE)).thenReturn(Optional.of(actorMember));
        when(groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, targetId, GroupMemberStatus.ACTIVE)).thenReturn(Optional.of(targetMember));

        memberCommandService.promoteMember(groupId, targetId, actorId);

        verify(permissionValidator).validateOwnerOnly(GroupRole.OWNER);
        assertThat(targetMember.getRole()).isEqualTo(GroupRole.ADMIN);
        verify(eventPublisher).publishEvent(any(RoleChangedEvent.class));
    }

    @Test
    void demoteMember_ShouldReduceAdminToMember() {
        String groupId = "group-123";
        String actorId = "owner-123";
        String targetId = "admin-123";

        GroupEntity group = GroupEntity.builder().id(groupId).build();
        UserEntity ownerUser = UserEntity.builder().id(actorId).fullName("Owner").build();
        UserEntity targetUser = UserEntity.builder().id(targetId).fullName("Admin").build();

        GroupMemberEntity actorMember = GroupMemberEntity.builder().group(group).user(ownerUser).role(GroupRole.OWNER).build();
        GroupMemberEntity targetMember = GroupMemberEntity.builder().group(group).user(targetUser).role(GroupRole.ADMIN).build();

        when(groupRepository.findByIdAndIsDeletedFalse(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, actorId, GroupMemberStatus.ACTIVE)).thenReturn(Optional.of(actorMember));
        when(groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, targetId, GroupMemberStatus.ACTIVE)).thenReturn(Optional.of(targetMember));

        memberCommandService.demoteMember(groupId, targetId, actorId);

        verify(permissionValidator).validateOwnerOnly(GroupRole.OWNER);
        assertThat(targetMember.getRole()).isEqualTo(GroupRole.MEMBER);
        verify(eventPublisher).publishEvent(any(RoleChangedEvent.class));
    }

    @Test
    void demoteMember_ShouldThrowExceptionIfTargetIsNotAdmin() {
        String groupId = "group-123";
        String actorId = "owner-123";
        String targetId = "member-123";

        GroupEntity group = GroupEntity.builder().id(groupId).build();
        GroupMemberEntity actorMember = GroupMemberEntity.builder().group(group).role(GroupRole.OWNER).build();
        GroupMemberEntity targetMember = GroupMemberEntity.builder().group(group).role(GroupRole.MEMBER).build();

        when(groupRepository.findByIdAndIsDeletedFalse(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, actorId, GroupMemberStatus.ACTIVE)).thenReturn(Optional.of(actorMember));
        when(groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, targetId, GroupMemberStatus.ACTIVE)).thenReturn(Optional.of(targetMember));

        assertThatThrownBy(() -> memberCommandService.demoteMember(groupId, targetId, actorId))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("Only admins can be demoted");
    }

    @Test
    void transferOwnership_ShouldTransferRolesAndOwnerRelationship() {
        String groupId = "group-123";
        String actorId = "owner-123";
        String targetId = "member-123";

        UserEntity currentOwner = UserEntity.builder().id(actorId).fullName("Current Owner").build();
        UserEntity newOwner = UserEntity.builder().id(targetId).fullName("New Owner").build();
        GroupEntity group = GroupEntity.builder().id(groupId).owner(currentOwner).build();

        GroupMemberEntity actorMember = GroupMemberEntity.builder().group(group).user(currentOwner).role(GroupRole.OWNER).build();
        GroupMemberEntity targetMember = GroupMemberEntity.builder().group(group).user(newOwner).role(GroupRole.MEMBER).build();

        when(groupRepository.findByIdAndIsDeletedFalse(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, actorId, GroupMemberStatus.ACTIVE)).thenReturn(Optional.of(actorMember));
        when(groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, targetId, GroupMemberStatus.ACTIVE)).thenReturn(Optional.of(targetMember));

        memberCommandService.transferOwnership(groupId, new TransferOwnershipRequest(targetId), actorId);

        verify(permissionValidator).validateOwnerOnly(GroupRole.OWNER);
        assertThat(group.getOwner()).isSameAs(newOwner);
        assertThat(actorMember.getRole()).isEqualTo(GroupRole.ADMIN);
        assertThat(targetMember.getRole()).isEqualTo(GroupRole.OWNER);
        verify(eventPublisher).publishEvent(any(OwnershipTransferredEvent.class));
    }
}
