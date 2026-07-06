package com.expenseflow.core.event;

public class NotificationArchivedEvent extends ApplicationEvent {
    private final String notificationId;
    private final String userId;

    public NotificationArchivedEvent(Object source, String notificationId, String userId) {
        super(source);
        this.notificationId = notificationId;
        this.userId = userId;
    }

    public String getNotificationId() { return notificationId; }
    public String getUserId() { return userId; }
}
