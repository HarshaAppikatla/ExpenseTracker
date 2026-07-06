package com.expenseflow.expense.exception;

public class ExpensePermissionDeniedException extends ExpenseException {
    public ExpensePermissionDeniedException(String message) {
        super(message, "EXPENSE_PERMISSION_DENIED");
    }
}
