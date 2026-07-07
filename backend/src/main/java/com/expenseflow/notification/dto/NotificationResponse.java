package com.expenseflow.notification.dto;

import com.expenseflow.notification.domain.valueobject.*;
import java.time.LocalDateTime;

/**
 * Full notification response returned from read/list endpoints.
 */
public record NotificationResponse(
        String id,
        String title,
        String message,
        NotificationStatus status,
        NotificationPriority priority,
        NotificationCategory category,
        NotificationType type,
        String expenseId,
        String groupId,
        String tripId,
        LocalDateTime createdAt
) {}
