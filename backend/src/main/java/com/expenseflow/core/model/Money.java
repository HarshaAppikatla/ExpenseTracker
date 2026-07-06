package com.expenseflow.core.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount, String currencyCode) {
    public Money {
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(currencyCode, "Currency code must not be null");
        if (currencyCode.isBlank()) {
            throw new IllegalArgumentException("Currency code must not be blank");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, currencyCode);
    }

    public Money add(Money other) {
        validateCurrency(other);
        return new Money(this.amount.add(other.amount), this.currencyCode);
    }

    public Money subtract(Money other) {
        validateCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currencyCode);
    }

    private void validateCurrency(Money other) {
        if (!this.currencyCode.equalsIgnoreCase(other.currencyCode)) {
            throw new IllegalArgumentException("Currency codes do not match: " + this.currencyCode + " and " + other.currencyCode);
        }
    }
}
