package com.expenseflow.group.service.impl;

import com.expenseflow.group.dto.GroupDashboardDto;
import com.expenseflow.group.dto.GroupDto;
import com.expenseflow.group.dto.GroupMemberDto;
import com.expenseflow.group.dto.GroupActivityDto;
import com.expenseflow.group.entity.GroupEntity;
import com.expenseflow.group.entity.GroupMemberEntity;
import com.expenseflow.group.entity.GroupMemberStatus;
import com.expenseflow.group.entity.GroupActivityEntity;
import com.expenseflow.group.exception.GroupNotFoundException;
import com.expenseflow.group.exception.PermissionDeniedException;
import com.expenseflow.group.mapper.GroupMapper;
import com.expenseflow.group.repository.GroupActivityRepository;
import com.expenseflow.group.repository.GroupMemberRepository;
import com.expenseflow.group.repository.GroupRepository;
import com.expenseflow.group.service.GroupQueryService;
import com.expenseflow.group.specification.GroupSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupQueryServiceImpl implements GroupQueryService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupActivityRepository groupActivityRepository;
    private final GroupMapper groupMapper;

    private static final int DEFAULT_MEMBER_PREVIEW_SIZE = 50;
    private static final int DEFAULT_ACTIVITY_PREVIEW_SIZE = 20;

    @Override
    public Page<GroupDto> getMyGroups(String currentUserId, String search, Pageable pageable) {
        Specification<GroupEntity> spec = Specification.where(GroupSpecification.isNotDeleted())
                .and(GroupSpecification.isMember(currentUserId));

        if (search != null && !search.isBlank()) {
            spec = spec.and(GroupSpecification.nameOrDescriptionContains(search));
        }

        Page<GroupEntity> groupPage = groupRepository.findAll(spec, pageable);

        if (groupPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<String> groupIds = groupPage.getContent().stream()
                .map(GroupEntity::getId)
                .toList();

        // Fetch memberships to resolve roles in one batch query to prevent N+1
        List<GroupMemberEntity> memberships = groupMemberRepository
                .findByUserIdAndGroupIdInAndStatusAndIsDeletedFalse(currentUserId, groupIds, GroupMemberStatus.ACTIVE);

        Map<String, String> roleMap = memberships.stream()
                .collect(Collectors.toMap(
                        m -> m.getGroup().getId(),
                        m -> m.getRole().name()
                ));

        return groupPage.map(group -> {
            String role = roleMap.getOrDefault(group.getId(), "MEMBER");
            return groupMapper.toDto(group, currentUserId, role);
        });
    }

    @Override
    public GroupDto getGroupDetails(String groupId, String currentUserId) {
        GroupEntity group = groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));

        GroupMemberEntity member = groupMemberRepository
                .findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, currentUserId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new PermissionDeniedException("User is not an active member of this group"));

        return groupMapper.toDto(group, currentUserId, member.getRole().name());
    }

    @Override
    public GroupDashboardDto getGroupDashboard(String groupId, String currentUserId) {
        GroupEntity group = groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));

        GroupMemberEntity member = groupMemberRepository
                .findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(groupId, currentUserId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new PermissionDeniedException("User is not an active member of this group"));

        // Fetch paginated preview of active members (max 50)
        Page<GroupMemberEntity> membersPage = groupMemberRepository
                .findActiveMembersByGroupId(groupId, GroupMemberStatus.ACTIVE, PageRequest.of(0, DEFAULT_MEMBER_PREVIEW_SIZE));

        // Fetch paginated recent activity logs (max 20)
        Page<GroupActivityEntity> activityPage = groupActivityRepository
                .findByGroupIdOrderByCreatedAtDesc(groupId, PageRequest.of(0, DEFAULT_ACTIVITY_PREVIEW_SIZE));

        int activeMemberCount = groupMemberRepository.countByGroupIdAndStatusAndIsDeletedFalse(groupId, GroupMemberStatus.ACTIVE);

        List<GroupMemberDto> memberDtos = membersPage.getContent().stream()
                .map(groupMapper::toMemberDto)
                .toList();

        List<GroupActivityDto> activityDtos = activityPage.getContent().stream()
                .map(groupMapper::toActivityDto)
                .toList();

        GroupDto groupDto = groupMapper.toDto(group, currentUserId, member.getRole().name());

        return new GroupDashboardDto(groupDto, memberDtos, activityDtos, activeMemberCount);
    }
}
