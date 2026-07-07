package com.expenseflow.notification.domain.event;

import com.expenseflow.core.event.ApplicationEvent;
import com.expenseflow.notification.dto.NotificationResponse;

/**
 * Event published when a new notification is successfully persisted.
 * Listened to by SsePublisher to push live updates to connected users.
 */
public class NotificationCreatedEvent extends ApplicationEvent {
    private final String notificationId;
    private final String userId;
    private final NotificationResponse notification;

    public NotificationCreatedEvent(Object source, String notificationId, String userId, NotificationResponse notification) {
        super(source);
        this.notificationId = notificationId;
        this.userId = userId;
        this.notification = notification;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public NotificationResponse getNotification() {
        return notification;
    }
}
