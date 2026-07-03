package com.expenseflow.group.dto;

import java.util.List;

public record GroupDashboardDto(
    GroupDto group,
    List<GroupMemberDto> members,
    List<GroupActivityDto> recentActivities,
    int activeMemberCount
) {}
