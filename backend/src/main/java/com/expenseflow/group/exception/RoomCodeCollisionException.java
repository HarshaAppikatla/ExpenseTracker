package com.expenseflow.group.exception;

public class RoomCodeCollisionException extends GroupException {
    public RoomCodeCollisionException(String message) {
        super(message, "GROUP_901");
    }
}
