package com.expenseflow.notification.exception;

/**
 * Thrown when a notification with the given ID does not exist (or is soft-deleted).
 */
public class NotificationNotFoundException extends NotificationException {

    public NotificationNotFoundException(String notificationId) {
        super("NOTIF_001", "Notification not found: " + notificationId);
    }
}
