package com.expenseflow.group.service;

import com.expenseflow.group.dto.CreateGroupRequest;
import com.expenseflow.group.dto.GroupDto;
import com.expenseflow.group.dto.UpdateGroupRequest;

public interface GroupCommandService {

    GroupDto createGroup(CreateGroupRequest request, String currentUserId);

    GroupDto updateGroup(String groupId, UpdateGroupRequest request, String currentUserId);

    GroupDto archiveGroup(String groupId, String currentUserId);

    GroupDto restoreGroup(String groupId, String currentUserId);

    void deleteGroup(String groupId, String currentUserId);
}
