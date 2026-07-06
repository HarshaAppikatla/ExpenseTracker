package com.expenseflow.notification.exception;

public class NotificationPermissionDeniedException extends NotificationException {
    public NotificationPermissionDeniedException(String message) {
        super(message);
    }
}
