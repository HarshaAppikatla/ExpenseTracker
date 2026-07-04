package com.expenseflow.group.validation;

import com.expenseflow.group.exception.DuplicateMemberException;
import com.expenseflow.group.exception.PermissionDeniedException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberValidatorTest {

    private final MemberValidator validator = new MemberValidator();

    @Test
    void validateNotAlreadyMember_ShouldSucceedWhenNotMember() {
        assertThatNoException().isThrownBy(() -> validator.validateNotAlreadyMember(false, "user-1", "group-1"));
    }

    @Test
    void validateNotAlreadyMember_ShouldThrowExceptionWhenAlreadyMember() {
        assertThatThrownBy(() -> validator.validateNotAlreadyMember(true, "user-1", "group-1"))
                .isInstanceOf(DuplicateMemberException.class)
                .hasMessageContaining("already a member of group");
    }

    @Test
    void validateIsActiveMember_ShouldSucceedWhenIsMember() {
        assertThatNoException().isThrownBy(() -> validator.validateIsActiveMember(true, "user-1", "group-1"));
    }

    @Test
    void validateIsActiveMember_ShouldThrowExceptionWhenNotMember() {
        assertThatThrownBy(() -> validator.validateIsActiveMember(false, "user-1", "group-1"))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("not an active member of group");
    }
}
