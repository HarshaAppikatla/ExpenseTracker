package com.expenseflow.settlement.domain.valueobject;

/**
 * Allowed lifecycle statuses for a Settlement record.
 *
 * Permitted transitions (enforced by SettlementEntity.transitionTo):
 *   PENDING  → CONFIRMED  (debtor confirms payment)
 *   PENDING  → DISPUTED   (creditor raises a dispute)
 *   DISPUTED → CONFIRMED  (dispute resolved)
 *
 * Any other transition throws InvalidSettlementStateException.
 */
public enum SettlementStatus {
    PENDING,
    CONFIRMED,
    DISPUTED
}
