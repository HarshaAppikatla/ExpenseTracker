package com.expenseflow.settlement.exception;

/**
 * Thrown when a requested settlement record does not exist.
 * Maps to HTTP 404.
 */
public class SettlementNotFoundException extends SettlementException {
    public SettlementNotFoundException(String settlementId) {
        super("Settlement not found: " + settlementId, "SETTLEMENT_NOT_FOUND");
    }
}
