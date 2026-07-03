package com.expenseflow.group.exception;

public class OwnerCannotLeaveException extends GroupException {
    public OwnerCannotLeaveException(String message) {
        super(message, "GROUP_202");
    }
}
