package com.expenseflow.group.service;

import com.expenseflow.group.dto.GroupDashboardDto;
import com.expenseflow.group.dto.GroupDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupQueryService {

    Page<GroupDto> getMyGroups(String currentUserId, String search, Pageable pageable);

    GroupDto getGroupDetails(String groupId, String currentUserId);

    GroupDashboardDto getGroupDashboard(String groupId, String currentUserId);

    boolean isGroupActive(String groupId);

    boolean isUserGroupMember(String groupId, String userId);
}
