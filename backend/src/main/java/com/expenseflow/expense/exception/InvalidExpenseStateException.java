package com.expenseflow.expense.exception;

public class InvalidExpenseStateException extends ExpenseException {
    public InvalidExpenseStateException(String message) {
        super(message, "INVALID_EXPENSE_STATE");
    }
}
