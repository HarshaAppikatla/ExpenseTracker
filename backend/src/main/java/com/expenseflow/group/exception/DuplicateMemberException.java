package com.expenseflow.group.exception;

public class DuplicateMemberException extends GroupException {
    public DuplicateMemberException(String message) {
        super(message, "GROUP_102");
    }
}
