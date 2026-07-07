package com.expenseflow.notification.exception;

/**
 * Thrown when a user attempts to access a notification they do not own.
 */
public class NotificationPermissionDeniedException extends NotificationException {

    public NotificationPermissionDeniedException(String notificationId, String userId) {
        super("NOTIF_002", "User " + userId + " does not have access to notification " + notificationId);
    }
}
