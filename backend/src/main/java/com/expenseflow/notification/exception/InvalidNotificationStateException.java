package com.expenseflow.notification.exception;

/**
 * Thrown when an invalid state transition is attempted on a notification
 * (e.g., archiving an already-deleted notification).
 */
public class InvalidNotificationStateException extends NotificationException {

    public InvalidNotificationStateException(String message) {
        super("NOTIF_003", message);
    }
}
