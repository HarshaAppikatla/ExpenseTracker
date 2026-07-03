package com.expenseflow.group.validation;

import com.expenseflow.group.entity.GroupRole;
import com.expenseflow.group.exception.OwnerCannotLeaveException;
import org.springframework.stereotype.Component;

@Component
public class OwnershipValidator {

    public void validateOwnerLeaving(GroupRole role) {
        if (role == GroupRole.OWNER) {
            throw new OwnerCannotLeaveException("Owner cannot leave the group. You must transfer group ownership first.");
        }
    }
}
