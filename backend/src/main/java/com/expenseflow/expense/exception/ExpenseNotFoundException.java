package com.expenseflow.expense.exception;

public class ExpenseNotFoundException extends ExpenseException {
    public ExpenseNotFoundException(String message) {
        super(message, "EXPENSE_NOT_FOUND");
    }
}
