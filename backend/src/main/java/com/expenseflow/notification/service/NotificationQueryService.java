package com.expenseflow.notification.service;

import com.expenseflow.notification.dto.NotificationResponse;
import com.expenseflow.notification.dto.UnreadCountResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Notification query operations — all reads.
 * Separated from command operations per CQRS-lite (ADR-006 §3.3).
 *
 * <p>All methods are read-only and run in a read-only transaction.
 * Callers: {@code NotificationController}
 */
public interface NotificationQueryService {

    /**
     * Returns a paginated list of all non-deleted notifications for a user,
     * ordered by {@code created_at DESC}.
     */
    Page<NotificationResponse> getNotifications(String userId, Pageable pageable);

    /**
     * Returns the count of unread non-deleted notifications for the notification bell badge.
     */
    UnreadCountResponse getUnreadCount(String userId);

    /**
     * Returns the latest limit notifications for a user, ordered by newest first.
     */
    List<NotificationResponse> getLatestNotifications(String userId, int limit);
}
