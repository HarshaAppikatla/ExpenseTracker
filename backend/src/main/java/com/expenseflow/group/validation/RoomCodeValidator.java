package com.expenseflow.group.validation;

import com.expenseflow.group.entity.GroupEntity;
import com.expenseflow.group.exception.InvalidRoomCodeException;
import org.springframework.stereotype.Component;

@Component
public class RoomCodeValidator {

    public void validateRoomCodeActive(GroupEntity group) {
        if (group == null || group.getSettings() == null || !group.getSettings().isAllowJoinByCode()) {
            throw new InvalidRoomCodeException("Joining via room code is currently disabled for this group");
        }
        if (group.getSettings().isArchived()) {
            throw new InvalidRoomCodeException("Cannot join because the group is archived");
        }
    }
}
