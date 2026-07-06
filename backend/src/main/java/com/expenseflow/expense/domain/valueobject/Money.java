package com.expenseflow.expense.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
@Getter
public class Money {

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private final BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", length = 10, nullable = false)
    private final CurrencyCode currency;

    // Hibernate requirement
    protected Money() {
        this.amount = null;
        this.currency = null;
    }

    public Money(BigDecimal amount, CurrencyCode currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        this.amount = amount.setScale(2, java.math.RoundingMode.HALF_UP);
        this.currency = currency;
    }

    public Money add(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot add null Money");
        }
        if (this.currency != other.currency) {
            throw new IllegalArgumentException("Currency mismatch: " + this.currency + " vs " + other.currency);
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot subtract null Money");
        }
        if (this.currency != other.currency) {
            throw new IllegalArgumentException("Currency mismatch: " + this.currency + " vs " + other.currency);
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0 && currency == money.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.setScale(2, java.math.RoundingMode.HALF_UP), currency);
    }

    @Override
    public String toString() {
        return amount + " " + currency;
    }
}
