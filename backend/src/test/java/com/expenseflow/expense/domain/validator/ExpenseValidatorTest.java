package com.expenseflow.expense.domain.validator;

import com.expenseflow.expense.domain.entity.*;
import com.expenseflow.expense.domain.valueobject.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpenseValidatorTest {

    private final ExpenseValidator validator = new ExpenseValidator();

    @Test
    void validateInvariants_ShouldSucceed_WhenBalancedAndConsistent() {
        ExpenseEntity expense = ExpenseEntity.builder()
                .id("exp-1")
                .groupId("group-1")
                .description("Lunch")
                .category(ExpenseCategory.FOOD)
                .money(new Money(new BigDecimal("100.00"), CurrencyCode.INR))
                .paidByUserId("userA")
                .createdByUserId("userA")
                .expenseDate(LocalDate.now())
                .status(ExpenseStatus.DRAFT)
                .participants(new ArrayList<>())
                .splits(new ArrayList<>())
                .build();

        expense.getParticipants().add(ExpenseParticipantEntity.builder().id("p1").expense(expense).userId("userA").build());
        expense.getParticipants().add(ExpenseParticipantEntity.builder().id("p2").expense(expense).userId("userB").build());

        expense.getSplits().add(ExpenseSplitEntity.builder().id("s1").expense(expense).userId("userA").owedAmount(new BigDecimal("40.00")).build());
        expense.getSplits().add(ExpenseSplitEntity.builder().id("s2").expense(expense).userId("userB").owedAmount(new BigDecimal("60.00")).build());

        assertThatCode(() -> validator.validateInvariants(expense)).doesNotThrowAnyException();
    }

    @Test
    void validateInvariants_ShouldThrowException_WhenSumOfSplitsDoesNotEqualTotal() {
        ExpenseEntity expense = ExpenseEntity.builder()
                .id("exp-1")
                .groupId("group-1")
                .description("Lunch")
                .category(ExpenseCategory.FOOD)
                .money(new Money(new BigDecimal("100.00"), CurrencyCode.INR))
                .paidByUserId("userA")
                .createdByUserId("userA")
                .expenseDate(LocalDate.now())
                .status(ExpenseStatus.DRAFT)
                .participants(new ArrayList<>())
                .splits(new ArrayList<>())
                .build();

        expense.getParticipants().add(ExpenseParticipantEntity.builder().id("p1").expense(expense).userId("userA").build());
        expense.getParticipants().add(ExpenseParticipantEntity.builder().id("p2").expense(expense).userId("userB").build());

        expense.getSplits().add(ExpenseSplitEntity.builder().id("s1").expense(expense).userId("userA").owedAmount(new BigDecimal("40.00")).build());
        expense.getSplits().add(ExpenseSplitEntity.builder().id("s2").expense(expense).userId("userB").owedAmount(new BigDecimal("50.00")).build()); // 40 + 50 = 90 (mismatch)

        assertThatThrownBy(() -> validator.validateInvariants(expense))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not equal total amount");
    }

    @Test
    void validateInvariants_ShouldThrowException_WhenDuplicateParticipantsExist() {
        ExpenseEntity expense = ExpenseEntity.builder()
                .id("exp-1")
                .groupId("group-1")
                .description("Lunch")
                .category(ExpenseCategory.FOOD)
                .money(new Money(new BigDecimal("100.00"), CurrencyCode.INR))
                .paidByUserId("userA")
                .createdByUserId("userA")
                .expenseDate(LocalDate.now())
                .status(ExpenseStatus.DRAFT)
                .participants(new ArrayList<>())
                .splits(new ArrayList<>())
                .build();

        expense.getParticipants().add(ExpenseParticipantEntity.builder().id("p1").expense(expense).userId("userA").build());
        expense.getParticipants().add(ExpenseParticipantEntity.builder().id("p2").expense(expense).userId("userA").build()); // Duplicate

        expense.getSplits().add(ExpenseSplitEntity.builder().id("s1").expense(expense).userId("userA").owedAmount(new BigDecimal("100.00")).build());

        assertThatThrownBy(() -> validator.validateInvariants(expense))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate participant found");
    }

    @Test
    void validateInvariants_ShouldThrowException_WhenSplitDebtorIsNotAParticipant() {
        ExpenseEntity expense = ExpenseEntity.builder()
                .id("exp-1")
                .groupId("group-1")
                .description("Lunch")
                .category(ExpenseCategory.FOOD)
                .money(new Money(new BigDecimal("100.00"), CurrencyCode.INR))
                .paidByUserId("userA")
                .createdByUserId("userA")
                .expenseDate(LocalDate.now())
                .status(ExpenseStatus.DRAFT)
                .participants(new ArrayList<>())
                .splits(new ArrayList<>())
                .build();

        expense.getParticipants().add(ExpenseParticipantEntity.builder().id("p1").expense(expense).userId("userA").build());

        expense.getSplits().add(ExpenseSplitEntity.builder().id("s1").expense(expense).userId("userB").owedAmount(new BigDecimal("100.00")).build()); // userB not in participants

        assertThatThrownBy(() -> validator.validateInvariants(expense))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("is not a participant in the expense");
    }
}
