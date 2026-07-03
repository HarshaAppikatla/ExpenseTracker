package com.expenseflow.group.service;

import com.expenseflow.group.entity.GroupCode;
import com.expenseflow.group.entity.GroupEntity;

public interface GroupAccessService {

    GroupCode generateGroupCode();

    GroupEntity validateAndResolveCode(String roomCode);

    String generateJoinLink(GroupCode groupCode);
}
