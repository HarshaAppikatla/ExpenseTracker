package com.expenseflow.group.exception;

public class GroupArchivedException extends GroupException {
    public GroupArchivedException(String message) {
        super(message, "GROUP_203");
    }
}
