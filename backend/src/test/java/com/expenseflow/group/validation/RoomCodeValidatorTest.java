package com.expenseflow.group.validation;

import com.expenseflow.group.entity.GroupEntity;
import com.expenseflow.group.entity.GroupSettings;
import com.expenseflow.group.exception.InvalidRoomCodeException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomCodeValidatorTest {

    private final RoomCodeValidator validator = new RoomCodeValidator();

    @Test
    void validateRoomCodeActive_ShouldSucceedWhenEnabledAndNotArchived() {
        GroupEntity group = GroupEntity.builder()
                .settings(GroupSettings.builder().allowJoinByCode(true).archived(false).build())
                .build();

        assertThatNoException().isThrownBy(() -> validator.validateRoomCodeActive(group));
    }

    @Test
    void validateRoomCodeActive_ShouldThrowExceptionWhenAllowJoinByCodeIsFalse() {
        GroupEntity group = GroupEntity.builder()
                .settings(GroupSettings.builder().allowJoinByCode(false).archived(false).build())
                .build();

        assertThatThrownBy(() -> validator.validateRoomCodeActive(group))
                .isInstanceOf(InvalidRoomCodeException.class)
                .hasMessageContaining("room code is currently disabled");
    }

    @Test
    void validateRoomCodeActive_ShouldThrowExceptionWhenArchived() {
        GroupEntity group = GroupEntity.builder()
                .settings(GroupSettings.builder().allowJoinByCode(true).archived(true).build())
                .build();

        assertThatThrownBy(() -> validator.validateRoomCodeActive(group))
                .isInstanceOf(InvalidRoomCodeException.class)
                .hasMessageContaining("group is archived");
    }
}
