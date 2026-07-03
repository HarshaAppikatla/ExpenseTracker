package com.expenseflow.group.validation;

import com.expenseflow.group.entity.GroupEntity;
import com.expenseflow.group.exception.GroupArchivedException;
import com.expenseflow.group.exception.GroupNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class GroupValidator {

    public void validateNotArchived(GroupEntity group) {
        if (group.getSettings() != null && group.getSettings().isArchived()) {
            throw new GroupArchivedException("Action cannot be performed because the group is archived");
        }
    }

    public void validateExists(boolean exists, String groupId) {
        if (!exists) {
            throw new GroupNotFoundException("Group not found with ID: " + groupId);
        }
    }
}
