package com.expenseflow.settlement.service.impl;

import com.expenseflow.expense.domain.entity.ExpenseEntity;
import com.expenseflow.expense.domain.entity.ExpenseSplitEntity;
import com.expenseflow.expense.domain.repository.ExpenseRepository;
import com.expenseflow.expense.domain.valueobject.CurrencyCode;
import com.expenseflow.expense.domain.valueobject.ExpenseStatus;
import com.expenseflow.expense.domain.valueobject.Money;
import com.expenseflow.group.dto.GroupDto;
import com.expenseflow.group.service.GroupQueryService;
import com.expenseflow.settlement.domain.engine.DebtMinimizationSolver;
import com.expenseflow.settlement.domain.engine.NetBalanceCalculator;
import com.expenseflow.settlement.domain.entity.SettlementEntity;
import com.expenseflow.settlement.domain.model.SettlementPair;
import com.expenseflow.settlement.domain.model.UserBalance;
import com.expenseflow.settlement.domain.valueobject.SettlementStatus;
import com.expenseflow.settlement.dto.SettlementResponse;
import com.expenseflow.settlement.dto.SettlementSummaryResponse;
import com.expenseflow.settlement.exception.NoPostedExpensesException;
import com.expenseflow.settlement.exception.SettlementPermissionDeniedException;
import com.expenseflow.settlement.repository.SettlementRepository;
import com.expenseflow.settlement.service.SettlementBusinessRuleService;
import com.expenseflow.settlement.service.SettlementQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementCommandServiceImplTest {

    @Mock
    private SettlementRepository settlementRepository;
    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private NetBalanceCalculator netBalanceCalculator;
    @Mock
    private DebtMinimizationSolver debtMinimizationSolver;
    @Mock
    private SettlementBusinessRuleService businessRuleService;
    @Mock
    private GroupQueryService groupQueryService;
    @Mock
    private SettlementQueryService settlementQueryService;

    @InjectMocks
    private SettlementCommandServiceImpl settlementCommandService;

    private final String groupId = "group-1";
    private final String currentUserId = "user-admin";

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Should fail to generate settlements if no posted expenses exist")
    void testGenerateSettlements_NoPostedExpenses() {
        // Given
        when(groupQueryService.getGroupDetails(groupId, currentUserId))
                .thenReturn(new GroupDto(groupId, "Trip", "Desc", "INR", "CODE", true, "OWNER", null, java.time.LocalDateTime.now()));
        when(expenseRepository.findByGroupIdAndStatusAndIsDeletedFalse(groupId, ExpenseStatus.POSTED))
                .thenReturn(Collections.emptyList());

        // When/Then
        assertThatThrownBy(() -> settlementCommandService.generateSettlements(groupId, null, currentUserId))
                .isInstanceOf(NoPostedExpensesException.class);

        verify(settlementRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should successfully generate settlements and handle idempotent updates/deletions")
    void testGenerateSettlements_Success() {
        // Given
        when(groupQueryService.getGroupDetails(groupId, currentUserId))
                .thenReturn(new GroupDto(groupId, "Trip", "Desc", "INR", "CODE", true, "OWNER", null, java.time.LocalDateTime.now()));

        ExpenseEntity expense = mock(ExpenseEntity.class);
        when(expense.getPaidByUserId()).thenReturn("user-a");
        when(expense.getMoney()).thenReturn(new Money(new BigDecimal("100.00"), CurrencyCode.INR));

        ExpenseSplitEntity split = mock(ExpenseSplitEntity.class);
        when(split.isDeleted()).thenReturn(false);
        when(split.getUserId()).thenReturn("user-b");
        when(split.getOwedAmount()).thenReturn(new BigDecimal("40.00"));
        when(expense.getSplits()).thenReturn(List.of(split));

        when(expenseRepository.findByGroupIdAndStatusAndIsDeletedFalse(groupId, ExpenseStatus.POSTED))
                .thenReturn(List.of(expense));

        List<UserBalance> mockBalances = List.of(
                new UserBalance("user-a", new Money(new BigDecimal("40.00"), CurrencyCode.INR)),
                new UserBalance("user-b", new Money(new BigDecimal("-40.00"), CurrencyCode.INR))
        );
        when(netBalanceCalculator.calculate(anyList())).thenReturn(mockBalances);

        List<SettlementPair> mockPairs = List.of(
                new SettlementPair("user-b", "user-a", new Money(new BigDecimal("40.00"), CurrencyCode.INR))
        );
        when(debtMinimizationSolver.solve(eq(mockBalances), eq(CurrencyCode.INR))).thenReturn(mockPairs);

        // Mock existing PENDING settlement that should be updated
        SettlementEntity existingPending = SettlementEntity.builder()
                .id("settle-1")
                .groupId(groupId)
                .fromUserId("user-b")
                .toUserId("user-a")
                .money(new Money(new BigDecimal("30.00"), CurrencyCode.INR))
                .status(SettlementStatus.PENDING)
                .build();
        when(settlementRepository.findByGroupIdAndStatusAndIsDeletedFalse(groupId, SettlementStatus.PENDING))
                .thenReturn(new ArrayList<>(List.of(existingPending)));

        // When
        settlementCommandService.generateSettlements(groupId, null, currentUserId);

        // Then
        verify(settlementRepository).saveAll(anyList());
        assertThat(existingPending.getMoney().getAmount()).isEqualByComparingTo(new BigDecimal("40.00"));
    }

    @Test
    @DisplayName("Should successfully mark settlement as paid when called by debtor")
    void testMarkAsPaid_Success() {
        // Given
        SettlementEntity settlement = SettlementEntity.builder()
                .id("settle-1")
                .groupId(groupId)
                .fromUserId("user-debtor")
                .toUserId("user-creditor")
                .money(new Money(new BigDecimal("50.00"), CurrencyCode.INR))
                .status(SettlementStatus.PENDING)
                .build();

        when(settlementRepository.findByIdAndIsDeletedFalse("settle-1")).thenReturn(Optional.of(settlement));
        when(settlementRepository.save(any(SettlementEntity.class))).thenAnswer(i -> i.getArgument(0));

        // When
        settlementCommandService.markAsPaid("settle-1", "user-debtor");

        // Then
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.CONFIRMED);
        verify(settlementRepository).save(settlement);
    }

    @Test
    @DisplayName("Should throw permission denied when non-debtor tries to mark as paid")
    void testMarkAsPaid_PermissionDenied() {
        // Given
        SettlementEntity settlement = SettlementEntity.builder()
                .id("settle-1")
                .groupId(groupId)
                .fromUserId("user-debtor")
                .toUserId("user-creditor")
                .money(new Money(new BigDecimal("50.00"), CurrencyCode.INR))
                .status(SettlementStatus.PENDING)
                .build();

        when(settlementRepository.findByIdAndIsDeletedFalse("settle-1")).thenReturn(Optional.of(settlement));

        // When/Then
        assertThatThrownBy(() -> settlementCommandService.markAsPaid("settle-1", "user-creditor"))
                .isInstanceOf(SettlementPermissionDeniedException.class)
                .hasMessageContaining("Only the debtor");

        verify(settlementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully dispute settlement when called by creditor")
    void testDisputeSettlement_Success() {
        // Given
        SettlementEntity settlement = SettlementEntity.builder()
                .id("settle-1")
                .groupId(groupId)
                .fromUserId("user-debtor")
                .toUserId("user-creditor")
                .money(new Money(new BigDecimal("50.00"), CurrencyCode.INR))
                .status(SettlementStatus.PENDING)
                .build();

        when(settlementRepository.findByIdAndIsDeletedFalse("settle-1")).thenReturn(Optional.of(settlement));
        when(settlementRepository.save(any(SettlementEntity.class))).thenAnswer(i -> i.getArgument(0));

        // When
        settlementCommandService.disputeSettlement("settle-1", "Wrong amount", "user-creditor");

        // Then
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.DISPUTED);
        verify(settlementRepository).save(settlement);
    }

    @Test
    @DisplayName("Should throw permission denied when non-creditor tries to dispute")
    void testDisputeSettlement_PermissionDenied() {
        // Given
        SettlementEntity settlement = SettlementEntity.builder()
                .id("settle-1")
                .groupId(groupId)
                .fromUserId("user-debtor")
                .toUserId("user-creditor")
                .money(new Money(new BigDecimal("50.00"), CurrencyCode.INR))
                .status(SettlementStatus.PENDING)
                .build();

        when(settlementRepository.findByIdAndIsDeletedFalse("settle-1")).thenReturn(Optional.of(settlement));

        // When/Then
        assertThatThrownBy(() -> settlementCommandService.disputeSettlement("settle-1", "Wrong amount", "user-debtor"))
                .isInstanceOf(SettlementPermissionDeniedException.class)
                .hasMessageContaining("Only the creditor");

        verify(settlementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully resolve disputed settlement when called by group owner/admin")
    void testResolveSettlement_Success() {
        // Given
        SettlementEntity settlement = SettlementEntity.builder()
                .id("settle-1")
                .groupId(groupId)
                .fromUserId("user-debtor")
                .toUserId("user-creditor")
                .money(new Money(new BigDecimal("50.00"), CurrencyCode.INR))
                .status(SettlementStatus.DISPUTED)
                .build();

        when(settlementRepository.findByIdAndIsDeletedFalse("settle-1")).thenReturn(Optional.of(settlement));
        when(settlementRepository.save(any(SettlementEntity.class))).thenAnswer(i -> i.getArgument(0));

        // When
        settlementCommandService.resolveSettlement("settle-1", currentUserId);

        // Then
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.CONFIRMED);
        verify(businessRuleService).verifyUserIsAdminOrOwner(groupId, currentUserId);
        verify(settlementRepository).save(settlement);
    }

    @Test
    @DisplayName("Should throw illegal state exception on invalid transitions")
    void testInvalidTransition() {
        // Given a settlement that is already CONFIRMED
        SettlementEntity settlement = SettlementEntity.builder()
                .id("settle-1")
                .groupId(groupId)
                .fromUserId("user-debtor")
                .toUserId("user-creditor")
                .money(new Money(new BigDecimal("50.00"), CurrencyCode.INR))
                .status(SettlementStatus.CONFIRMED)
                .build();

        when(settlementRepository.findByIdAndIsDeletedFalse("settle-1")).thenReturn(Optional.of(settlement));

        // When/Then: Debtor tries to confirm again
        assertThatThrownBy(() -> settlementCommandService.markAsPaid("settle-1", "user-debtor"))
                .isInstanceOf(com.expenseflow.settlement.exception.InvalidSettlementStateException.class)
                .hasMessageContaining("Invalid transition");

        // When/Then: Creditor tries to dispute a confirmed settlement
        assertThatThrownBy(() -> settlementCommandService.disputeSettlement("settle-1", "Dispute", "user-creditor"))
                .isInstanceOf(com.expenseflow.settlement.exception.InvalidSettlementStateException.class)
                .hasMessageContaining("Invalid transition");
    }
}

