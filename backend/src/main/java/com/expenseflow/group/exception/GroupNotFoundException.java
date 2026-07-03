package com.expenseflow.group.exception;

public class GroupNotFoundException extends GroupException {
    public GroupNotFoundException(String message) {
        super(message, "GROUP_101");
    }
}
