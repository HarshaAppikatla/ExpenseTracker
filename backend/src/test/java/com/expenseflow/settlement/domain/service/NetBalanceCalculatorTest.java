package com.expenseflow.settlement.domain.service;

import com.expenseflow.expense.domain.valueobject.CurrencyCode;
import com.expenseflow.expense.domain.valueobject.Money;
import com.expenseflow.settlement.domain.engine.NetBalanceCalculator;
import com.expenseflow.settlement.domain.model.UserBalance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NetBalanceCalculatorTest {

    private final NetBalanceCalculator calculator = new NetBalanceCalculator();

    @Test
    @DisplayName("Should correctly calculate balances for an equal split")
    void testEqualSplit() {
        // Given: User A paid 120 INR for a group of 3 (A, B, C). Equal split means each owes 40 INR.
        List<NetBalanceCalculator.SplitRecord> splits = List.of(
                new NetBalanceCalculator.SplitRecord("user-a", "user-a", new Money(new BigDecimal("40.00"), CurrencyCode.INR)),
                new NetBalanceCalculator.SplitRecord("user-a", "user-b", new Money(new BigDecimal("40.00"), CurrencyCode.INR)),
                new NetBalanceCalculator.SplitRecord("user-a", "user-c", new Money(new BigDecimal("40.00"), CurrencyCode.INR))
        );

        // When
        List<UserBalance> balances = calculator.calculate(splits);

        // Then
        // User A paid 120, owes 40 -> Net: +80
        // User B paid 0, owes 40 -> Net: -40
        // User C paid 0, owes 40 -> Net: -40
        assertThat(balances).hasSize(3);
        assertThat(findBalance(balances, "user-a")).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(findBalance(balances, "user-b")).isEqualByComparingTo(new BigDecimal("-40.00"));
        assertThat(findBalance(balances, "user-c")).isEqualByComparingTo(new BigDecimal("-40.00"));
    }

    @Test
    @DisplayName("Should handle exact splits and multiple payers correctly")
    void testExactSplitAndMultiplePayers() {
        // Given: Multiple expenses and multiple payers
        // Expense 1: User A paid 100 INR. B owes 60, C owes 40.
        // Expense 2: User B paid 150 INR. A owes 50, C owes 100.
        List<NetBalanceCalculator.SplitRecord> splits = List.of(
                new NetBalanceCalculator.SplitRecord("user-a", "user-b", new Money(new BigDecimal("60.00"), CurrencyCode.INR)),
                new NetBalanceCalculator.SplitRecord("user-a", "user-c", new Money(new BigDecimal("40.00"), CurrencyCode.INR)),

                new NetBalanceCalculator.SplitRecord("user-b", "user-a", new Money(new BigDecimal("50.00"), CurrencyCode.INR)),
                new NetBalanceCalculator.SplitRecord("user-b", "user-c", new Money(new BigDecimal("100.00"), CurrencyCode.INR))
        );

        // When
        List<UserBalance> balances = calculator.calculate(splits);

        // Then
        // A's net: +60 (paid for B) + 40 (paid for C) - 50 (owes B) = +50
        // B's net: -60 (owes A) + 50 (paid for A) + 100 (paid for C) = +90
        // C's net: -40 (owes A) - 100 (owes B) = -140
        assertThat(balances).hasSize(3);
        assertThat(findBalance(balances, "user-a")).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(findBalance(balances, "user-b")).isEqualByComparingTo(new BigDecimal("90.00"));
        assertThat(findBalance(balances, "user-c")).isEqualByComparingTo(new BigDecimal("-140.00"));
    }

    @Test
    @DisplayName("Should correctly filter out users whose net balance is zero")
    void testZeroBalanceRemoval() {
        // Given: User A paid 100 INR. B owes 50, A owes 50.
        // Expense 2: User B paid 50 INR. A owes 50. (A owes B 50, B owes A 50 -> net 0)
        List<NetBalanceCalculator.SplitRecord> splits = List.of(
                new NetBalanceCalculator.SplitRecord("user-a", "user-a", new Money(new BigDecimal("50.00"), CurrencyCode.INR)),
                new NetBalanceCalculator.SplitRecord("user-a", "user-b", new Money(new BigDecimal("50.00"), CurrencyCode.INR)),
                new NetBalanceCalculator.SplitRecord("user-b", "user-a", new Money(new BigDecimal("50.00"), CurrencyCode.INR))
        );

        // When
        List<UserBalance> balances = calculator.calculate(splits);

        // Then: both user A and B have net 0 balance, so they should be excluded entirely
        assertThat(balances).isEmpty();
    }

    @Test
    @DisplayName("Should handle 0.01 rounding differences correctly")
    void testRoundingTolerance() {
        // Given: Equal split of 100.00 INR among 3 people. One split has 33.34, others have 33.33.
        List<NetBalanceCalculator.SplitRecord> splits = List.of(
                new NetBalanceCalculator.SplitRecord("user-a", "user-a", new Money(new BigDecimal("33.34"), CurrencyCode.INR)),
                new NetBalanceCalculator.SplitRecord("user-a", "user-b", new Money(new BigDecimal("33.33"), CurrencyCode.INR)),
                new NetBalanceCalculator.SplitRecord("user-a", "user-c", new Money(new BigDecimal("33.33"), CurrencyCode.INR))
        );

        // When
        List<UserBalance> balances = calculator.calculate(splits);

        // Then
        // A's net: +33.33 + 33.33 = +66.66
        // B's net: -33.33
        // C's net: -33.33
        assertThat(balances).hasSize(3);
        assertThat(findBalance(balances, "user-a")).isEqualByComparingTo(new BigDecimal("66.66"));
        assertThat(findBalance(balances, "user-b")).isEqualByComparingTo(new BigDecimal("-33.33"));
        assertThat(findBalance(balances, "user-c")).isEqualByComparingTo(new BigDecimal("-33.33"));
    }

    private BigDecimal findBalance(List<UserBalance> balances, String userId) {
        return balances.stream()
                .filter(b -> b.userId().equals(userId))
                .map(b -> b.money().getAmount())
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }
}
