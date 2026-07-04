package com.expenseflow.group.validation;

import com.expenseflow.group.entity.GroupEntity;
import com.expenseflow.group.entity.GroupSettings;
import com.expenseflow.group.exception.GroupArchivedException;
import com.expenseflow.group.exception.GroupNotFoundException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroupValidatorTest {

    private final GroupValidator validator = new GroupValidator();

    @Test
    void validateNotArchived_ShouldSucceedWhenNotArchived() {
        GroupEntity group = GroupEntity.builder()
                .settings(GroupSettings.builder().archived(false).build())
                .build();

        assertThatNoException().isThrownBy(() -> validator.validateNotArchived(group));
    }

    @Test
    void validateNotArchived_ShouldThrowExceptionWhenArchived() {
        GroupEntity group = GroupEntity.builder()
                .settings(GroupSettings.builder().archived(true).build())
                .build();

        assertThatThrownBy(() -> validator.validateNotArchived(group))
                .isInstanceOf(GroupArchivedException.class)
                .hasMessageContaining("group is archived");
    }

    @Test
    void validateExists_ShouldSucceedWhenExistsIsTrue() {
        assertThatNoException().isThrownBy(() -> validator.validateExists(true, "groupId"));
    }

    @Test
    void validateExists_ShouldThrowExceptionWhenExistsIsFalse() {
        assertThatThrownBy(() -> validator.validateExists(false, "group-123"))
                .isInstanceOf(GroupNotFoundException.class)
                .hasMessageContaining("Group not found with ID: group-123");
    }
}
