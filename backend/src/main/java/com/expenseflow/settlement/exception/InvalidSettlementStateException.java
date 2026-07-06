package com.expenseflow.settlement.exception;

/**
 * Thrown when a settlement state transition is not permitted by the state machine.
 * Maps to HTTP 409.
 */
public class InvalidSettlementStateException extends SettlementException {
    public InvalidSettlementStateException(String detail) {
        super(detail, "INVALID_SETTLEMENT_STATE");
    }
}
