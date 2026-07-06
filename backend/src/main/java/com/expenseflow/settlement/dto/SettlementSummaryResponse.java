package com.expenseflow.settlement.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * API response DTO representing a high-level summary of outstanding balances within a group.
 */
public record SettlementSummaryResponse(
        String groupId,
        String tripId,
        String currency,
        BigDecimal totalOutstanding,
        int pendingCount,
        List<SettlementResponse> settlements
) {}
