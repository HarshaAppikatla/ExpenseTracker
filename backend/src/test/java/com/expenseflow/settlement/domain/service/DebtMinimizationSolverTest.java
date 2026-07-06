package com.expenseflow.settlement.domain.service;

import com.expenseflow.expense.domain.valueobject.CurrencyCode;
import com.expenseflow.expense.domain.valueobject.Money;
import com.expenseflow.settlement.domain.engine.DebtMinimizationSolver;
import com.expenseflow.settlement.domain.model.SettlementPair;
import com.expenseflow.settlement.domain.model.UserBalance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DebtMinimizationSolverTest {

    private final DebtMinimizationSolver solver = new DebtMinimizationSolver();

    @Test
    @DisplayName("Should return empty settlements when everyone is already settled")
    void testEveryoneAlreadySettled() {
        // Given
        List<UserBalance> balances = List.of();

        // When
        List<SettlementPair> result = solver.solve(balances, CurrencyCode.INR);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should resolve simple debt between 2 participants")
    void testTwoParticipants() {
        // Given
        // User A is owed 50 INR
        // User B owes 50 INR
        List<UserBalance> balances = List.of(
                new UserBalance("user-a", new Money(new BigDecimal("50.00"), CurrencyCode.INR)),
                new UserBalance("user-b", new Money(new BigDecimal("-50.00"), CurrencyCode.INR))
        );

        // When
        List<SettlementPair> result = solver.solve(balances, CurrencyCode.INR);

        // Then: 1 transaction needed: B pays A 50
        assertThat(result).hasSize(1);
        SettlementPair pair = result.get(0);
        assertThat(pair.fromUserId()).isEqualTo("user-b");
        assertThat(pair.toUserId()).isEqualTo("user-a");
        assertThat(pair.money().getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(pair.money().getCurrency()).isEqualTo(CurrencyCode.INR);
    }

    @Test
    @DisplayName("Should minimize transactions for 3 participants")
    void testThreeParticipants() {
        // Given
        // User A is owed 100 INR
        // User B owes 60 INR
        // User C owes 40 INR
        List<UserBalance> balances = List.of(
                new UserBalance("user-a", new Money(new BigDecimal("100.00"), CurrencyCode.INR)),
                new UserBalance("user-b", new Money(new BigDecimal("-60.00"), CurrencyCode.INR)),
                new UserBalance("user-c", new Money(new BigDecimal("-40.00"), CurrencyCode.INR))
        );

        // When
        List<SettlementPair> result = solver.solve(balances, CurrencyCode.INR);

        // Then: 2 transactions needed:
        // B pays A 60
        // C pays A 40
        assertThat(result).hasSize(2);
        assertThat(result).extracting(SettlementPair::fromUserId).containsExactlyInAnyOrder("user-b", "user-c");
        assertThat(result).extracting(SettlementPair::toUserId).containsOnly("user-a");
    }

    @Test
    @DisplayName("Should handle complex multi-user graph (10+ participants) efficiently")
    void testTenParticipants() {
        // Given: 10 users with mixed balances summing to zero
        List<UserBalance> balances = new ArrayList<>();
        balances.add(new UserBalance("user-1", new Money(new BigDecimal("300.00"), CurrencyCode.INR)));
        balances.add(new UserBalance("user-2", new Money(new BigDecimal("150.00"), CurrencyCode.INR)));
        balances.add(new UserBalance("user-3", new Money(new BigDecimal("50.00"), CurrencyCode.INR)));
        balances.add(new UserBalance("user-4", new Money(new BigDecimal("100.00"), CurrencyCode.INR)));
        // Creditors sum: 600

        balances.add(new UserBalance("user-5", new Money(new BigDecimal("-200.00"), CurrencyCode.INR)));
        balances.add(new UserBalance("user-6", new Money(new BigDecimal("-150.00"), CurrencyCode.INR)));
        balances.add(new UserBalance("user-7", new Money(new BigDecimal("-50.00"), CurrencyCode.INR)));
        balances.add(new UserBalance("user-8", new Money(new BigDecimal("-100.00"), CurrencyCode.INR)));
        balances.add(new UserBalance("user-9", new Money(new BigDecimal("-80.00"), CurrencyCode.INR)));
        balances.add(new UserBalance("user-10", new Money(new BigDecimal("-20.00"), CurrencyCode.INR)));
        // Debtors sum: -600

        // When
        List<SettlementPair> result = solver.solve(balances, CurrencyCode.INR);

        // Then: Should solve completely and leave no residual balances
        // Total transactions should be at most N-1 (9)
        assertThat(result.size()).isLessThan(10);

        // Assert all transactions sum to 600
        BigDecimal totalSettleAmount = result.stream()
                .map(p -> p.money().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalSettleAmount).isEqualByComparingTo(new BigDecimal("600.00"));
    }

    @Test
    @DisplayName("Should throw exception when currency mismatch is present")
    void testCurrencyMismatch() {
        List<UserBalance> balances = List.of(
                new UserBalance("user-a", new Money(new BigDecimal("50.00"), CurrencyCode.INR)),
                new UserBalance("user-b", new Money(new BigDecimal("-50.00"), CurrencyCode.USD))
        );

        assertThatThrownBy(() -> solver.solve(balances, CurrencyCode.INR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency mismatch in balances");
    }
}
