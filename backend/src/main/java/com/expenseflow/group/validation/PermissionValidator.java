package com.expenseflow.group.validation;

import com.expenseflow.group.entity.GroupRole;
import com.expenseflow.group.exception.PermissionDeniedException;
import org.springframework.stereotype.Component;

@Component
public class PermissionValidator {

    public void validateRoleRequired(GroupRole currentRole, GroupRole requiredMinimumRole) {
        if (currentRole == null || currentRole.ordinal() > requiredMinimumRole.ordinal()) {
            throw new PermissionDeniedException("Permission denied: required role is " + requiredMinimumRole);
        }
    }

    public void validateOwnerOnly(GroupRole currentRole) {
        if (currentRole != GroupRole.OWNER) {
            throw new PermissionDeniedException("Permission denied: this action requires Owner privileges");
        }
    }

    public void validateAdminOrOwner(GroupRole currentRole) {
        if (currentRole != GroupRole.OWNER && currentRole != GroupRole.ADMIN) {
            throw new PermissionDeniedException("Permission denied: this action requires Admin or Owner privileges");
        }
    }
}
