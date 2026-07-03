package com.expenseflow.group.facade;

import com.expenseflow.group.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupFacade {

    GroupDto createGroup(CreateGroupRequest request, String currentUserId);

    GroupDto updateGroup(String groupId, UpdateGroupRequest request, String currentUserId);

    GroupDto archiveGroup(String groupId, String currentUserId);

    GroupDto restoreGroup(String groupId, String currentUserId);

    void deleteGroup(String groupId, String currentUserId);

    GroupDto joinGroup(JoinGroupRequest request, String currentUserId);

    void leaveGroup(String groupId, String currentUserId);

    void removeMember(String groupId, String memberId, String currentUserId);

    void promoteMember(String groupId, String memberId, String currentUserId);

    void demoteMember(String groupId, String memberId, String currentUserId);

    void transferOwnership(String groupId, TransferOwnershipRequest request, String currentUserId);

    Page<GroupDto> getMyGroups(String currentUserId, String search, Pageable pageable);

    GroupDto getGroupDetails(String groupId, String currentUserId);

    GroupDashboardDto getGroupDashboard(String groupId, String currentUserId);
}
