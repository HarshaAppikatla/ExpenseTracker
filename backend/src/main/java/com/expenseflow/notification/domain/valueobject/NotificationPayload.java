package com.expenseflow.notification.domain.valueobject;

import java.math.BigDecimal;

/**
 * Value object representing a type-safe notification payload.
 */
public record NotificationPayload(
        String expenseId,
        String groupId,
        String tripId,
        BigDecimal amount,
        String currency,
        Integer version
) {}
