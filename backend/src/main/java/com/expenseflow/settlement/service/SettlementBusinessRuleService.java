package com.expenseflow.settlement.service;

import com.expenseflow.group.entity.GroupMemberEntity;
import com.expenseflow.group.entity.GroupRole;
import com.expenseflow.group.repository.GroupMemberRepository;
import com.expenseflow.group.service.GroupQueryService;
import com.expenseflow.settlement.exception.InvalidSettlementStateException;
import com.expenseflow.settlement.exception.SettlementPermissionDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementBusinessRuleService {

    private final GroupQueryService groupQueryService;
    private final GroupMemberRepository groupMemberRepository;

    public void verifyGroupActive(String groupId) {
        if (!groupQueryService.isGroupActive(groupId)) {
            throw new InvalidSettlementStateException("Parent group is not active or archived");
        }
    }

    public void verifyUserIsGroupMember(String groupId, String userId) {
        if (!groupQueryService.isUserGroupMember(groupId, userId)) {
            throw new SettlementPermissionDeniedException("User " + userId + " must be an active member of the parent group");
        }
    }

    public void verifyUserIsAdminOrOwner(String groupId, String userId) {
        verifyUserIsGroupMember(groupId, userId);
        GroupMemberEntity member = groupMemberRepository.findByGroupIdAndUserIdAndIsDeletedFalse(groupId, userId)
                .orElseThrow(() -> new SettlementPermissionDeniedException("User " + userId + " must be an active member of the parent group"));

        if (!member.getRole().hasPermissionOf(GroupRole.ADMIN)) {
            throw new SettlementPermissionDeniedException("Only the group Owner or Admin can perform this action");
        }
    }
}
