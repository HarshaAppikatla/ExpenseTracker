package com.expenseflow.settlement.exception;

/**
 * Thrown when settlement generation is attempted but no POSTED expenses exist in the group/trip.
 * Maps to HTTP 400 or 422.
 */
public class NoPostedExpensesException extends SettlementException {
    public NoPostedExpensesException(String groupId) {
        super("No POSTED expenses found for group '" + groupId + "'. Post at least one expense before generating settlements.", "NO_POSTED_EXPENSES");
    }
}
