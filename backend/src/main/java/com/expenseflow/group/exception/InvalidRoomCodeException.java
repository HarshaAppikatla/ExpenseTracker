package com.expenseflow.group.exception;

public class InvalidRoomCodeException extends GroupException {
    public InvalidRoomCodeException(String message) {
        super(message, "GROUP_301");
    }
}
