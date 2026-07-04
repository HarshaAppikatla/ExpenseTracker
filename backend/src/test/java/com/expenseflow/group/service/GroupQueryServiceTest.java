package com.expenseflow.group.service;

import com.expenseflow.group.dto.*;
import com.expenseflow.group.entity.GroupEntity;
import com.expenseflow.group.entity.GroupMemberEntity;
import com.expenseflow.group.entity.GroupMemberStatus;
import com.expenseflow.group.entity.GroupRole;
import com.expenseflow.group.exception.PermissionDeniedException;
import com.expenseflow.group.mapper.GroupMapper;
import com.expenseflow.group.repository.GroupActivityRepository;
import com.expenseflow.group.repository.GroupMemberRepository;
import com.expenseflow.group.repository.GroupRepository;
import com.expenseflow.group.service.impl.GroupQueryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupQueryServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private GroupActivityRepository groupActivityRepository;

    @Mock
    private GroupMapper groupMapper;

    @InjectMocks
    private GroupQueryServiceImpl groupQueryService;

    @Test
    @SuppressWarnings("unchecked")
    void getMyGroups_ShouldReturnPageOfGroupDtos() {
        String userId = "user-123";
        Pageable pageable = PageRequest.of(0, 10);
        GroupEntity group = GroupEntity.builder().id("group-123").build();
        Page<GroupEntity> groupPage = new PageImpl<>(List.of(group));
        GroupMemberEntity member = GroupMemberEntity.builder().group(group).role(GroupRole.MEMBER).build();

        when(groupRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(groupPage);
        when(groupMemberRepository.findByUserIdAndGroupIdInAndStatusAndIsDeletedFalse(eq(userId), any(List.class), eq(GroupMemberStatus.ACTIVE)))
                .thenReturn(List.of(member));
        when(groupMapper.toDto(group, userId, "MEMBER")).thenReturn(mock(GroupDto.class));

        Page<GroupDto> result = groupQueryService.getMyGroups(userId, "search", pageable);

        assertThat(result).hasSize(1);
        verify(groupRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getMyGroups_ShouldReturnEmptyPageIfNoGroupsFound() {
        String userId = "user-123";
        Pageable pageable = PageRequest.of(0, 10);
        when(groupRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(Page.empty());

        Page<GroupDto> result = groupQueryService.getMyGroups(userId, null, pageable);

        assertThat(result).isEmpty();
        verifyNoInteractions(groupMemberRepository);
    }

    @Test
    void getGroupDetails_ShouldReturnDtoIfMember() {
        String groupId = "group-123";
        String userId = "user-123";
        GroupEntity group = GroupEntity.builder().id(groupId).build();
        GroupMemberEntity member = GroupMemberEntity.builder().group(group).role(GroupRole.MEMBER).build();

        when(groupRepository.findByIdAndIsDeletedFalse(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, userId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.of(member));
        when(groupMapper.toDto(group, userId, "MEMBER")).thenReturn(mock(GroupDto.class));

        GroupDto details = groupQueryService.getGroupDetails(groupId, userId);

        assertThat(details).isNotNull();
    }

    @Test
    void getGroupDetails_ShouldThrowExceptionIfNotMember() {
        String groupId = "group-123";
        String userId = "user-123";
        GroupEntity group = GroupEntity.builder().id(groupId).build();

        when(groupRepository.findByIdAndIsDeletedFalse(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, userId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupQueryService.getGroupDetails(groupId, userId))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("User is not an active member");
    }

    @Test
    void getGroupDashboard_ShouldAssembleDashboardPayload() {
        String groupId = "group-123";
        String userId = "user-123";
        GroupEntity group = GroupEntity.builder().id(groupId).build();
        GroupMemberEntity member = GroupMemberEntity.builder().group(group).role(GroupRole.OWNER).build();

        when(groupRepository.findByIdAndIsDeletedFalse(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, userId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.of(member));

        when(groupMemberRepository.findActiveMembersByGroupId(eq(groupId), eq(GroupMemberStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(groupActivityRepository.findByGroupIdOrderByCreatedAtDesc(eq(groupId), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(groupMemberRepository.countByGroupIdAndStatusAndIsDeletedFalse(groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(5);

        when(groupMapper.toDto(group, userId, "OWNER")).thenReturn(mock(GroupDto.class));

        GroupDashboardDto dashboard = groupQueryService.getGroupDashboard(groupId, userId);

        assertThat(dashboard).isNotNull();
        assertThat(dashboard.activeMemberCount()).isEqualTo(5);
        verify(groupMemberRepository).findActiveMembersByGroupId(eq(groupId), eq(GroupMemberStatus.ACTIVE), any(Pageable.class));
        verify(groupActivityRepository).findByGroupIdOrderByCreatedAtDesc(eq(groupId), any(Pageable.class));
    }
}
