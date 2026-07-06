package com.expenseflow.notification.dto;

import com.expenseflow.notification.domain.valueobject.NotificationPriority;
import com.expenseflow.notification.domain.valueobject.NotificationStatus;

import java.time.LocalDateTime;

public record NotificationDto(
    String id,
    String userId,
    String notificationType,
    String title,
    String message,
    NotificationStatus status,
    NotificationPriority priority,
    LocalDateTime createdAt
) {}
