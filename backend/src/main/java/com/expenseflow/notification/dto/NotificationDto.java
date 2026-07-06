package com.expenseflow.notification.dto;

import com.expenseflow.notification.entity.NotificationPriority;
import com.expenseflow.notification.entity.NotificationStatus;

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
