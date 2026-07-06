package com.expenseflow.settlement.exception;

import lombok.Getter;

/**
 * Base exception for all settlement domain errors.
 * Subclasses map to specific error codes.
 */
@Getter
public abstract class SettlementException extends RuntimeException {
    private final String code;

    protected SettlementException(String message, String code) {
        super(message);
        this.code = code;
    }
}
