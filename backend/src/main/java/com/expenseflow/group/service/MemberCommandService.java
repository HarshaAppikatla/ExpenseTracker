package com.expenseflow.group.service;

import com.expenseflow.group.dto.GroupDto;
import com.expenseflow.group.dto.JoinGroupRequest;
import com.expenseflow.group.dto.TransferOwnershipRequest;

public interface MemberCommandService {

    GroupDto joinGroup(JoinGroupRequest request, String currentUserId);

    void leaveGroup(String groupId, String currentUserId);

    void removeMember(String groupId, String memberId, String currentUserId);

    void promoteMember(String groupId, String memberId, String currentUserId);

    void demoteMember(String groupId, String memberId, String currentUserId);

    void transferOwnership(String groupId, TransferOwnershipRequest request, String currentUserId);
}
