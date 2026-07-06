package com.expenseflow.settlement.service;

import com.expenseflow.settlement.dto.SettlementResponse;
import com.expenseflow.settlement.dto.SettlementSummaryResponse;

/**
 * Settlement command operations — all mutations.
 * Separated from query operations per CQRS-lite (ADR-005 §3.3).
 */
public interface SettlementCommandService {

    /**
     * Generates settlements for all POSTED expenses in a group (or trip).
     *
     * Idempotent: calling this multiple times without expense changes
     * produces identical pairs and no duplicate records (ADR-005 §10.3).
     *
     * @param groupId       The group to generate settlements for.
     * @param tripId        Optional trip scope. Null = group-wide.
     * @param currentUserId Must be group Owner or Admin.
     * @return Summary of generated settlement pairs.
     */
    SettlementSummaryResponse generateSettlements(String groupId, String tripId, String currentUserId);

    /**
     * Marks a settlement as paid (PENDING → CONFIRMED).
     * Only the debtor (fromUser) may confirm payment (ADR-005 §12).
     */
    SettlementResponse markAsPaid(String settlementId, String currentUserId);

    /**
     * Raises a dispute on a settlement (PENDING → DISPUTED).
     * Only the creditor (toUser) may raise a dispute (ADR-005 §12).
     */
    SettlementResponse disputeSettlement(String settlementId, String reason, String currentUserId);

    /**
     * Resolves a disputed settlement (DISPUTED → CONFIRMED).
     * Only the group Owner or Admin may resolve (ADR-005 §12).
     */
    SettlementResponse resolveSettlement(String settlementId, String currentUserId);
}
