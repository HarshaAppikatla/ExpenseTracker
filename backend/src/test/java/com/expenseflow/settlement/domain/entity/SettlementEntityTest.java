package com.expenseflow.settlement.domain.entity;

import com.expenseflow.expense.domain.valueobject.CurrencyCode;
import com.expenseflow.expense.domain.valueobject.Money;
import com.expenseflow.settlement.domain.valueobject.SettlementStatus;
import com.expenseflow.settlement.exception.InvalidSettlementStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SettlementEntityTest {

    @Test
    @DisplayName("Should initialize in PENDING status")
    void testInitialStatus() {
        SettlementEntity settlement = SettlementEntity.builder()
                .id("test-id")
                .groupId("group-1")
                .fromUserId("user-a")
                .toUserId("user-b")
                .money(new Money(new BigDecimal("100.00"), CurrencyCode.INR))
                .build();

        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.PENDING);
        assertThat(settlement.getHistory()).isEmpty();
    }

    @Test
    @DisplayName("Should successfully transition PENDING -> CONFIRMED and record in history")
    void testPendingToConfirmed() {
        SettlementEntity settlement = createPendingSettlement();

        settlement.transitionTo(SettlementStatus.CONFIRMED, "user-b", "Payment received");

        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.CONFIRMED);
        assertThat(settlement.getSettledAt()).isNotNull();
        assertThat(settlement.getHistory()).hasSize(1);

        SettlementHistoryEntity history = settlement.getHistory().get(0);
        assertThat(history.getFromStatus()).isEqualTo(SettlementStatus.PENDING.name());
        assertThat(history.getToStatus()).isEqualTo(SettlementStatus.CONFIRMED.name());
        assertThat(history.getChangedBy()).isEqualTo("user-b");
        assertThat(history.getNote()).isEqualTo("Payment received");
    }

    @Test
    @DisplayName("Should successfully transition PENDING -> DISPUTED and record in history")
    void testPendingToDisputed() {
        SettlementEntity settlement = createPendingSettlement();

        settlement.transitionTo(SettlementStatus.DISPUTED, "user-b", "Incorrect amount paid");

        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.DISPUTED);
        assertThat(settlement.getHistory()).hasSize(1);

        SettlementHistoryEntity history = settlement.getHistory().get(0);
        assertThat(history.getFromStatus()).isEqualTo(SettlementStatus.PENDING.name());
        assertThat(history.getToStatus()).isEqualTo(SettlementStatus.DISPUTED.name());
        assertThat(history.getChangedBy()).isEqualTo("user-b");
        assertThat(history.getNote()).isEqualTo("Incorrect amount paid");
    }

    @Test
    @DisplayName("Should successfully transition DISPUTED -> CONFIRMED")
    void testDisputedToConfirmed() {
        SettlementEntity settlement = createPendingSettlement();
        settlement.transitionTo(SettlementStatus.DISPUTED, "user-b", "Disputed");
        settlement.transitionTo(SettlementStatus.CONFIRMED, "admin-1", "Resolved and confirmed");

        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.CONFIRMED);
        assertThat(settlement.getHistory()).hasSize(2);

        SettlementHistoryEntity history = settlement.getHistory().get(1);
        assertThat(history.getFromStatus()).isEqualTo(SettlementStatus.DISPUTED.name());
        assertThat(history.getToStatus()).isEqualTo(SettlementStatus.CONFIRMED.name());
        assertThat(history.getChangedBy()).isEqualTo("admin-1");
        assertThat(history.getNote()).isEqualTo("Resolved and confirmed");
    }

    @Test
    @DisplayName("Should reject illegal transitions and throw InvalidSettlementStateException")
    void testIllegalTransitions() {
        SettlementEntity settlement = createPendingSettlement();

        // CONFIRMED is terminal — cannot transition to DISPUTED or back to PENDING
        settlement.transitionTo(SettlementStatus.CONFIRMED, "user-b", "Settled");
        
        assertThatThrownBy(() -> settlement.transitionTo(SettlementStatus.PENDING, "user-a", "Undo"))
                .isInstanceOf(InvalidSettlementStateException.class);

        assertThatThrownBy(() -> settlement.transitionTo(SettlementStatus.DISPUTED, "user-a", "Reopen"))
                .isInstanceOf(InvalidSettlementStateException.class);
    }

    @Test
    @DisplayName("Should allow amount updates only in PENDING status")
    void testUpdateAmount() {
        SettlementEntity settlement = createPendingSettlement();
        Money newMoney = new Money(new BigDecimal("150.00"), CurrencyCode.INR);

        settlement.updateAmount(newMoney, "user-b");
        assertThat(settlement.getMoney().getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));

        // transition to CONFIRMED
        settlement.transitionTo(SettlementStatus.CONFIRMED, "user-b", "Settled");

        // try to update amount when CONFIRMED -> should fail
        assertThatThrownBy(() -> settlement.updateAmount(newMoney, "user-b"))
                .isInstanceOf(InvalidSettlementStateException.class);
    }

    private SettlementEntity createPendingSettlement() {
        return SettlementEntity.builder()
                .id("test-id")
                .groupId("group-1")
                .fromUserId("user-a")
                .toUserId("user-b")
                .money(new Money(new BigDecimal("100.00"), CurrencyCode.INR))
                .build();
    }
}
