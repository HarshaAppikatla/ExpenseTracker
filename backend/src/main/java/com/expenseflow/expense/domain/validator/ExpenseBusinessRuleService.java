package com.expenseflow.expense.domain.validator;

import com.expenseflow.expense.domain.entity.ExpenseEntity;
import com.expenseflow.expense.exception.ExpensePermissionDeniedException;
import com.expenseflow.expense.exception.InvalidExpenseStateException;
import com.expenseflow.group.service.GroupQueryService;
import com.expenseflow.trip.dto.TripDto;
import com.expenseflow.trip.service.query.TripQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExpenseBusinessRuleService {

    private final GroupQueryService groupQueryService;
    private final TripQueryService tripQueryService;

    public void verifyGroupActive(String groupId) {
        if (!groupQueryService.isGroupActive(groupId)) {
            throw new InvalidExpenseStateException("Parent group is not active or archived");
        }
    }

    public void verifyUserIsGroupMember(String groupId, String userId) {
        if (!groupQueryService.isUserGroupMember(groupId, userId)) {
            throw new ExpensePermissionDeniedException("User " + userId + " must be an active member of the parent group");
        }
    }

    public void verifyTripAssociatedWithGroup(String tripId, String groupId, String currentUserId) {
        if (tripId == null) {
            return;
        }
        TripDto trip;
        try {
            trip = tripQueryService.getTripDetails(tripId, currentUserId);
        } catch (Exception e) {
            throw new InvalidExpenseStateException("Associated trip " + tripId + " was not found or is inaccessible");
        }
        if (!trip.groupId().equals(groupId)) {
            throw new InvalidExpenseStateException("Associated trip " + tripId + " does not belong to group " + groupId);
        }
    }

    public void verifyUserCanManageExpense(ExpenseEntity expense, String currentUserId) {
        verifyUserIsGroupMember(expense.getGroupId(), currentUserId);
        
        boolean isCreator = expense.getCreatedByUserId().equals(currentUserId);
        boolean isPayer = expense.getPaidByUserId().equals(currentUserId);
        if (!isCreator && !isPayer) {
            throw new ExpensePermissionDeniedException("Only the expense creator or payer can update or delete this expense");
        }
    }
}
