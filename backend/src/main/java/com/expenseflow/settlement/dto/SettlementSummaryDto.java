package com.expenseflow.settlement.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * High-level "who owes whom" summary for a group or trip.
 * Returned by GET /api/v1/groups/{groupId}/settlements.
 */
public record SettlementSummaryDto(
        String groupId,
        String tripId,
        String currency,
        BigDecimal totalOutstanding,
        int pendingCount,
        List<SettlementDto> settlements
) {}
