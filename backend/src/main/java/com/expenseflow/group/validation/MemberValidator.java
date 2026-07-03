package com.expenseflow.group.validation;

import com.expenseflow.group.exception.DuplicateMemberException;
import com.expenseflow.group.exception.PermissionDeniedException;
import org.springframework.stereotype.Component;

@Component
public class MemberValidator {

    public void validateNotAlreadyMember(boolean isMember, String userId, String groupId) {
        if (isMember) {
            throw new DuplicateMemberException("User " + userId + " is already a member of group " + groupId);
        }
    }

    public void validateIsActiveMember(boolean isMember, String userId, String groupId) {
        if (!isMember) {
            throw new PermissionDeniedException("User " + userId + " is not an active member of group " + groupId);
        }
    }
}
