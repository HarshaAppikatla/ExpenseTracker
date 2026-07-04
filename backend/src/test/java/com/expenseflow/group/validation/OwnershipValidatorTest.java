package com.expenseflow.group.validation;

import com.expenseflow.group.entity.GroupRole;
import com.expenseflow.group.exception.OwnerCannotLeaveException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OwnershipValidatorTest {

    private final OwnershipValidator validator = new OwnershipValidator();

    @Test
    void validateOwnerLeaving_ShouldSucceedForNonOwners() {
        assertThatNoException().isThrownBy(() -> validator.validateOwnerLeaving(GroupRole.ADMIN));
        assertThatNoException().isThrownBy(() -> validator.validateOwnerLeaving(GroupRole.MEMBER));
    }

    @Test
    void validateOwnerLeaving_ShouldThrowExceptionForOwners() {
        assertThatThrownBy(() -> validator.validateOwnerLeaving(GroupRole.OWNER))
                .isInstanceOf(OwnerCannotLeaveException.class)
                .hasMessageContaining("Owner cannot leave the group");
    }
}
