package com.expenseflow.expense.domain.validator;

import com.expenseflow.expense.exception.ExpensePermissionDeniedException;
import com.expenseflow.expense.exception.InvalidExpenseStateException;
import com.expenseflow.group.service.GroupQueryService;
import com.expenseflow.trip.dto.TripDto;
import com.expenseflow.trip.service.query.TripQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseBusinessRuleServiceTest {

    @Mock
    private GroupQueryService groupQueryService;

    @Mock
    private TripQueryService tripQueryService;

    @InjectMocks
    private ExpenseBusinessRuleService businessRuleService;

    @Test
    void verifyGroupActive_ShouldSucceed_WhenGroupIsActive() {
        when(groupQueryService.isGroupActive("group-1")).thenReturn(true);
        assertThatCode(() -> businessRuleService.verifyGroupActive("group-1")).doesNotThrowAnyException();
    }

    @Test
    void verifyGroupActive_ShouldThrowException_WhenGroupIsInactive() {
        when(groupQueryService.isGroupActive("group-1")).thenReturn(false);
        assertThatThrownBy(() -> businessRuleService.verifyGroupActive("group-1"))
                .isInstanceOf(InvalidExpenseStateException.class)
                .hasMessageContaining("Parent group is not active");
    }

    @Test
    void verifyUserIsGroupMember_ShouldSucceed_WhenUserIsMember() {
        when(groupQueryService.isUserGroupMember("group-1", "user-1")).thenReturn(true);
        assertThatCode(() -> businessRuleService.verifyUserIsGroupMember("group-1", "user-1")).doesNotThrowAnyException();
    }

    @Test
    void verifyUserIsGroupMember_ShouldThrowException_WhenUserIsNotMember() {
        when(groupQueryService.isUserGroupMember("group-1", "user-1")).thenReturn(false);
        assertThatThrownBy(() -> businessRuleService.verifyUserIsGroupMember("group-1", "user-1"))
                .isInstanceOf(ExpensePermissionDeniedException.class)
                .hasMessageContaining("must be an active member");
    }

    @Test
    void verifyTripAssociatedWithGroup_ShouldSucceed_WhenTripBelongsToGroup() {
        TripDto trip = new TripDto("trip-1", "group-1", "Roadtrip", null, null, null, null, null, null, null, null, null, null, null, null, null);
        when(tripQueryService.getTripDetails("trip-1", "user-1")).thenReturn(trip);

        assertThatCode(() -> businessRuleService.verifyTripAssociatedWithGroup("trip-1", "group-1", "user-1")).doesNotThrowAnyException();
    }

    @Test
    void verifyTripAssociatedWithGroup_ShouldThrowException_WhenTripBelongsToDifferentGroup() {
        TripDto trip = new TripDto("trip-1", "group-2", "Roadtrip", null, null, null, null, null, null, null, null, null, null, null, null, null);
        when(tripQueryService.getTripDetails("trip-1", "user-1")).thenReturn(trip);

        assertThatThrownBy(() -> businessRuleService.verifyTripAssociatedWithGroup("trip-1", "group-1", "user-1"))
                .isInstanceOf(InvalidExpenseStateException.class)
                .hasMessageContaining("does not belong to group");
    }
}
