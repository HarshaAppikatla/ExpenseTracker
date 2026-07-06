package com.expenseflow.expense.exception;

import lombok.Getter;

@Getter
public abstract class ExpenseException extends RuntimeException {
    private final String code;

    protected ExpenseException(String message, String code) {
        super(message);
        this.code = code;
    }
}
