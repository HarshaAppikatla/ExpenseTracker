package com.expenseflow.settlement.domain.model;

import com.expenseflow.expense.domain.valueobject.Money;
import java.math.BigDecimal;

/**
 * Represents the net financial position of a single user within a group or trip.
 *
 *   money.amount > 0  →  user is owed money (creditor)
 *   money.amount < 0  →  user owes money (debtor)
 *   money.amount == 0 →  user is fully settled (excluded from results)
 *
 * Uses the Money value object to prevent primitive obsession and currency mismatch.
 */
public record UserBalance(String userId, Money money) {

    /** True if this user is owed money. */
    public boolean isCreditor() {
        return money.getAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    /** True if this user owes money. */
    public boolean isDebtor() {
        return money.getAmount().compareTo(BigDecimal.ZERO) < 0;
    }

    /** Returns the absolute value of the money. */
    public Money absMoney() {
        return new Money(money.getAmount().abs(), money.getCurrency());
    }
}
