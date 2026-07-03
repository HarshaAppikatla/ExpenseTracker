package com.expenseflow.group.facade.impl;

import com.expenseflow.group.dto.*;
import com.expenseflow.group.facade.GroupFacade;
import com.expenseflow.group.service.GroupCommandService;
import com.expenseflow.group.service.GroupQueryService;
import com.expenseflow.group.service.MemberCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupFacadeImpl implements GroupFacade {

    private final GroupCommandService groupCommandService;
    private final MemberCommandService memberCommandService;
    private final GroupQueryService groupQueryService;

    @Override
    public GroupDto createGroup(CreateGroupRequest request, String currentUserId) {
        return groupCommandService.createGroup(request, currentUserId);
    }

    @Override
    public GroupDto updateGroup(String groupId, UpdateGroupRequest request, String currentUserId) {
        return groupCommandService.updateGroup(groupId, request, currentUserId);
    }

    @Override
    public GroupDto archiveGroup(String groupId, String currentUserId) {
        return groupCommandService.archiveGroup(groupId, currentUserId);
    }

    @Override
    public GroupDto restoreGroup(String groupId, String currentUserId) {
        return groupCommandService.restoreGroup(groupId, currentUserId);
    }

    @Override
    public void deleteGroup(String groupId, String currentUserId) {
        groupCommandService.deleteGroup(groupId, currentUserId);
    }

    @Override
    public GroupDto joinGroup(JoinGroupRequest request, String currentUserId) {
        return memberCommandService.joinGroup(request, currentUserId);
    }

    @Override
    public void leaveGroup(String groupId, String currentUserId) {
        memberCommandService.leaveGroup(groupId, currentUserId);
    }

    @Override
    public void removeMember(String groupId, String memberId, String currentUserId) {
        memberCommandService.removeMember(groupId, memberId, currentUserId);
    }

    @Override
    public void promoteMember(String groupId, String memberId, String currentUserId) {
        memberCommandService.promoteMember(groupId, memberId, currentUserId);
    }

    @Override
    public void demoteMember(String groupId, String memberId, String currentUserId) {
        memberCommandService.demoteMember(groupId, memberId, currentUserId);
    }

    @Override
    public void transferOwnership(String groupId, TransferOwnershipRequest request, String currentUserId) {
        memberCommandService.transferOwnership(groupId, request, currentUserId);
    }

    @Override
    public Page<GroupDto> getMyGroups(String currentUserId, String search, Pageable pageable) {
        return groupQueryService.getMyGroups(currentUserId, search, pageable);
    }

    @Override
    public GroupDto getGroupDetails(String groupId, String currentUserId) {
        return groupQueryService.getGroupDetails(groupId, currentUserId);
    }

    @Override
    public GroupDashboardDto getGroupDashboard(String groupId, String currentUserId) {
        return groupQueryService.getGroupDashboard(groupId, currentUserId);
    }
}
