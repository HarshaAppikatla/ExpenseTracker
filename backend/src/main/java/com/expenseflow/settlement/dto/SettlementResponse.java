package com.expenseflow.settlement.dto;

import com.expenseflow.settlement.domain.valueobject.SettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * API response DTO representing a single Settlement.
 */
public record SettlementResponse(
        String id,
        String groupId,
        String tripId,
        String fromUserId,
        String fromUserName,
        String toUserId,
        String toUserName,
        BigDecimal amount,
        String currency,
        SettlementStatus status,
        LocalDateTime settledAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
