package com.expenseflow.notification.service;

import com.expenseflow.notification.domain.valueobject.*;
import com.expenseflow.notification.dto.NotificationResponse;

/**
 * Notification command operations — all mutations.
 * Separated from query operations per CQRS-lite (ADR-006 §3.3).
 *
 * <p>Callers:
 * <ul>
 *   <li>{@code NotificationEventListener} — creates notifications from domain events</li>
 *   <li>{@code NotificationController} — markAsRead, markAllAsRead, archive</li>
 * </ul>
 */
public interface NotificationCommandService {

    /**
     * Persists a new notification for the given user.
     * Called exclusively by {@code NotificationEventListener} after AFTER_COMMIT domain events.
     *
     * @param userId   target user ID
     * @param type     notification type
     * @param category notification category
     * @param priority notification priority
     * @param title    short headline (≤255 chars)
     * @param message  human-readable body (≤500 chars)
     * @param payload  optional structured payload (may be null)
     */
    void createNotification(
            String userId,
            NotificationType type,
            NotificationCategory category,
            NotificationPriority priority,
            String title,
            String message,
            NotificationPayload payload);

    /**
     * Legacy overload for existing event listeners that pass a raw type string.
     * Deprecated — callers should migrate to the typed overload.
     */
    @Deprecated(forRemoval = true)
    void createNotification(String userId, String type, String title, String message, NotificationPriority priority);

    /**
     * Marks a single notification as READ.
     *
     * @throws com.expenseflow.notification.exception.NotificationNotFoundException       if not found
     * @throws com.expenseflow.notification.exception.NotificationPermissionDeniedException if caller doesn't own it
     */
    NotificationResponse markAsRead(String userId, String notificationId);

    /**
     * Marks all of the user's unread notifications as READ in a single batch.
     *
     * @return number of notifications updated
     */
    int markAllAsRead(String userId);

    /**
     * Soft-deletes a notification. The record is retained in the DB with {@code is_deleted = true}.
     *
     * @throws com.expenseflow.notification.exception.NotificationNotFoundException       if not found
     * @throws com.expenseflow.notification.exception.NotificationPermissionDeniedException if caller doesn't own it
     */
    void archiveNotification(String userId, String notificationId);
}
