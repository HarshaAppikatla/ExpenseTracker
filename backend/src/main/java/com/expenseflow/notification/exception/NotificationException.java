package com.expenseflow.notification.exception;

import lombok.Getter;

/**
 * Base exception for the Notification bounded context.
 * Carries an error code for structured error responses.
 */
@Getter
public class NotificationException extends RuntimeException {

    private final String code;

    public NotificationException(String code, String message) {
        super(message);
        this.code = code;
    }
}
