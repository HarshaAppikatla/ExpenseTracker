package com.expenseflow.group.exception;

public class PermissionDeniedException extends GroupException {
    public PermissionDeniedException(String message) {
        super(message, "GROUP_201");
    }
}
