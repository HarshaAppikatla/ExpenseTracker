package com.expenseflow.settlement.service;

import com.expenseflow.settlement.dto.SettlementResponse;
import com.expenseflow.settlement.dto.SettlementSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Settlement query operations — all reads.
 * Separated from command operations per CQRS-lite (ADR-005 §3.3).
 */
public interface SettlementQueryService {

    /**
     * Returns the full settlement summary for a group — "who owes whom".
     * Includes all PENDING and DISPUTED settlements.
     */
    SettlementSummaryResponse getSettlementSummary(String groupId, String currentUserId);

    /**
     * Returns all settlements scoped to a specific trip.
     */
    Page<SettlementResponse> getSettlementsByTrip(String groupId, String tripId, String currentUserId, Pageable pageable);

    /**
     * Returns all settlements where the current user is the debtor or creditor
     * within a specific group.
     */
    List<SettlementResponse> getMySettlements(String groupId, String currentUserId);

    /**
     * Returns the detail of a single settlement by ID.
     */
    SettlementResponse getSettlementById(String settlementId, String currentUserId);
}
