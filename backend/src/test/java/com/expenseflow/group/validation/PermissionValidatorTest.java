package com.expenseflow.group.validation;

import com.expenseflow.group.entity.GroupRole;
import com.expenseflow.group.exception.PermissionDeniedException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PermissionValidatorTest {

    private final PermissionValidator validator = new PermissionValidator();

    @Test
    void validateRoleRequired_ShouldSucceedWhenRoleMeetsOrExceedsRequirement() {
        assertThatNoException().isThrownBy(() -> validator.validateRoleRequired(GroupRole.OWNER, GroupRole.ADMIN));
        assertThatNoException().isThrownBy(() -> validator.validateRoleRequired(GroupRole.ADMIN, GroupRole.ADMIN));
        assertThatNoException().isThrownBy(() -> validator.validateRoleRequired(GroupRole.ADMIN, GroupRole.MEMBER));
    }

    @Test
    void validateRoleRequired_ShouldThrowExceptionWhenRoleIsInsufficient() {
        assertThatThrownBy(() -> validator.validateRoleRequired(GroupRole.MEMBER, GroupRole.ADMIN))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("required role is ADMIN");
    }

    @Test
    void validateOwnerOnly_ShouldSucceedForOwner() {
        assertThatNoException().isThrownBy(() -> validator.validateOwnerOnly(GroupRole.OWNER));
    }

    @Test
    void validateOwnerOnly_ShouldThrowExceptionForNonOwners() {
        assertThatThrownBy(() -> validator.validateOwnerOnly(GroupRole.ADMIN))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("requires Owner privileges");
    }

    @Test
    void validateAdminOrOwner_ShouldSucceedForAdminOrOwner() {
        assertThatNoException().isThrownBy(() -> validator.validateAdminOrOwner(GroupRole.OWNER));
        assertThatNoException().isThrownBy(() -> validator.validateAdminOrOwner(GroupRole.ADMIN));
    }

    @Test
    void validateAdminOrOwner_ShouldThrowExceptionForNormalMembers() {
        assertThatThrownBy(() -> validator.validateAdminOrOwner(GroupRole.MEMBER))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("requires Admin or Owner privileges");
    }
}
