package com.expenseflow.settlement.exception;

/**
 * Thrown when a user attempts an action they are not authorized to perform on a settlement.
 * Maps to HTTP 403.
 */
public class SettlementPermissionDeniedException extends SettlementException {
    public SettlementPermissionDeniedException(String detail) {
        super(detail, "SETTLEMENT_PERMISSION_DENIED");
    }
}
