package com.expenseflow.group.service;

import com.expenseflow.entity.UserEntity;
import com.expenseflow.group.dto.CreateGroupRequest;
import com.expenseflow.group.dto.GroupDto;
import com.expenseflow.group.dto.GroupSettingsDto;
import com.expenseflow.group.dto.UpdateGroupRequest;
import com.expenseflow.group.entity.*;
import com.expenseflow.group.event.*;
import com.expenseflow.group.exception.GroupNotFoundException;
import com.expenseflow.group.exception.PermissionDeniedException;
import com.expenseflow.group.mapper.GroupMapper;
import com.expenseflow.group.repository.GroupActivityRepository;
import com.expenseflow.group.repository.GroupMemberRepository;
import com.expenseflow.group.repository.GroupRepository;
import com.expenseflow.group.service.impl.GroupCommandServiceImpl;
import com.expenseflow.group.validation.GroupValidator;
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
class GroupCommandServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private GroupActivityRepository groupActivityRepository;

    @Mock
    private GroupValidator groupValidator;

    @Mock
    private PermissionValidator permissionValidator;

    @Mock
    private GroupMapper groupMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private GroupAccessService groupAccessService;

    @InjectMocks
    private GroupCommandServiceImpl groupCommandService;

    @Test
    void createGroup_ShouldCreateGroupWithOwnerAndActivityAndEvent() {
        String userId = "user-123";
        UserEntity user = UserEntity.builder().id(userId).fullName("Group Owner").build();
        CreateGroupRequest request = new CreateGroupRequest("Trip Pack", "Sharing trip costs", "USD");
        GroupCode dummyCode = new GroupCode("XYZ12345");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(groupAccessService.generateGroupCode()).thenReturn(dummyCode);
        when(groupRepository.save(any(GroupEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(groupMapper.toDto(any(GroupEntity.class), eq(userId), eq("OWNER"))).thenReturn(mock(GroupDto.class));

        groupCommandService.createGroup(request, userId);

        // Verify group settings initialization
        ArgumentCaptor<GroupEntity> groupCaptor = ArgumentCaptor.forClass(GroupEntity.class);
        verify(groupRepository).save(groupCaptor.capture());
        GroupEntity savedGroup = groupCaptor.getValue();
        assertThat(savedGroup.getName()).isEqualTo("Trip Pack");
        assertThat(savedGroup.getOwner()).isSameAs(user);
        assertThat(savedGroup.getSettings().isAllowJoinByCode()).isTrue();
        assertThat(savedGroup.getSettings().isArchived()).isFalse();

        // Verify membership creation
        ArgumentCaptor<GroupMemberEntity> memberCaptor = ArgumentCaptor.forClass(GroupMemberEntity.class);
        verify(groupMemberRepository).save(memberCaptor.capture());
        GroupMemberEntity savedMember = memberCaptor.getValue();
        assertThat(savedMember.getUser()).isSameAs(user);
        assertThat(savedMember.getRole()).isEqualTo(GroupRole.OWNER);
        assertThat(savedMember.getStatus()).isEqualTo(GroupMemberStatus.ACTIVE);

        // Verify activity audit log
        verify(groupActivityRepository).save(any(GroupActivityEntity.class));

        // Verify event published
        verify(eventPublisher).publishEvent(any(GroupCreatedEvent.class));
    }

    @Test
    void updateGroup_ShouldUpdateGroupSuccessfullyAndPublishEvent() {
        String groupId = "group-123";
        String userId = "user-123";
        GroupEntity group = GroupEntity.builder().id(groupId).name("Old Name").settings(GroupSettings.builder().archived(false).build()).build();
        UserEntity user = UserEntity.builder().id(userId).fullName("Owner").build();
        GroupMemberEntity member = GroupMemberEntity.builder().group(group).user(user).role(GroupRole.OWNER).build();

        UpdateGroupRequest request = new UpdateGroupRequest("New Name", "New Desc", new GroupSettingsDto(true, true, false));

        when(groupRepository.findByIdAndIsDeletedFalse(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, userId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.of(member));
        when(groupRepository.save(any(GroupEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        groupCommandService.updateGroup(groupId, request, userId);

        verify(permissionValidator).validateAdminOrOwner(GroupRole.OWNER);
        verify(groupValidator).validateNotArchived(group);
        assertThat(group.getName()).isEqualTo("New Name");
        assertThat(group.getDescription()).isEqualTo("New Desc");
        verify(eventPublisher).publishEvent(any(GroupUpdatedEvent.class));
    }

    @Test
    void archiveGroup_ShouldSetArchivedFlagTrueAndDisableJoins() {
        String groupId = "group-123";
        String userId = "user-123";
        GroupEntity group = GroupEntity.builder().id(groupId).settings(GroupSettings.builder().archived(false).allowJoinByCode(true).build()).build();
        UserEntity user = UserEntity.builder().id(userId).fullName("Owner").build();
        GroupMemberEntity member = GroupMemberEntity.builder().group(group).user(user).role(GroupRole.OWNER).build();

        when(groupRepository.findByIdAndIsDeletedFalse(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, userId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.of(member));
        when(groupRepository.save(any(GroupEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        groupCommandService.archiveGroup(groupId, userId);

        assertThat(group.getSettings().isArchived()).isTrue();
        assertThat(group.getSettings().isAllowJoinByCode()).isFalse();
        assertThat(group.getSettings().isAllowJoinByLink()).isFalse();
        verify(eventPublisher).publishEvent(any(GroupArchivedEvent.class));
    }

    @Test
    void restoreGroup_ShouldSetArchivedFlagFalseAndEnableJoins() {
        String groupId = "group-123";
        String userId = "user-123";
        GroupEntity group = GroupEntity.builder().id(groupId).settings(GroupSettings.builder().archived(true).build()).build();
        UserEntity user = UserEntity.builder().id(userId).fullName("Owner").build();
        GroupMemberEntity member = GroupMemberEntity.builder().group(group).user(user).role(GroupRole.OWNER).build();

        when(groupRepository.findByIdAndIsDeletedFalse(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, userId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.of(member));
        when(groupRepository.save(any(GroupEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        groupCommandService.restoreGroup(groupId, userId);

        assertThat(group.getSettings().isArchived()).isFalse();
        assertThat(group.getSettings().isAllowJoinByCode()).isTrue();
        assertThat(group.getSettings().isAllowJoinByLink()).isTrue();
        verify(eventPublisher).publishEvent(any(GroupRestoredEvent.class));
    }

    @Test
    void deleteGroup_ShouldPerformSoftDeleteAndPublishEvent() {
        String groupId = "group-123";
        String userId = "user-123";
        GroupEntity group = GroupEntity.builder().id(groupId).build();
        UserEntity user = UserEntity.builder().id(userId).fullName("Owner").build();
        GroupMemberEntity member = GroupMemberEntity.builder().group(group).user(user).role(GroupRole.OWNER).build();

        when(groupRepository.findByIdAndIsDeletedFalse(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, userId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.of(member));

        groupCommandService.deleteGroup(groupId, userId);

        verify(permissionValidator).validateOwnerOnly(GroupRole.OWNER);
        assertThat(group.isDeleted()).isTrue();
        assertThat(group.getDeletedAt()).isNotNull();
        assertThat(group.getDeletedBy()).isEqualTo("Owner");
        verify(eventPublisher).publishEvent(any(GroupDeletedEvent.class));
    }
}
