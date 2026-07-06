package com.expenseflow.settlement.domain.model;

import com.expenseflow.expense.domain.valueobject.Money;

/**
 * A single resolved debt pair produced by the DebtMinimizationSolver.
 *
 * Represents the instruction: "{fromUserId} must pay {money} to {toUserId}".
 *
 * The DebtMinimizationSolver guarantees that the full set of SettlementPair
 * records it produces requires the minimum possible number of transactions
 * to zero all member balances.
 */
public record SettlementPair(
        String fromUserId,  // debtor — owes money
        String toUserId,    // creditor — is owed money
        Money money
) {}
