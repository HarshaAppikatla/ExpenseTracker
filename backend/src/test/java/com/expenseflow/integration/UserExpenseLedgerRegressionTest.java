package com.expenseflow.integration;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.expense.domain.entity.ExpenseEntity;
import com.expenseflow.expense.domain.entity.ExpenseParticipantEntity;
import com.expenseflow.expense.domain.entity.ExpenseSplitEntity;
import com.expenseflow.expense.domain.repository.ExpenseRepository;
import com.expenseflow.expense.domain.valueobject.*;
import com.expenseflow.expense.dto.ExpenseDto;
import com.expenseflow.expense.service.ExpenseQueryService;
import com.expenseflow.group.service.GroupQueryService;
import com.expenseflow.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserExpenseLedgerRegressionTest extends BaseIntegrationTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseQueryService expenseQueryService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockBean
    private GroupQueryService groupQueryService;

    @MockBean
    private UserService userService;

    private TransactionTemplate transactionTemplate;
    private final String targetUserId = "user-target";

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.executeWithoutResult(status -> {
            expenseRepository.deleteAll();
        });

        // Stub name enrichment to return empty users list
        Mockito.when(userService.getUsersByIds(Mockito.anyList())).thenReturn(Collections.emptyList());
    }

    @Test
    void testUserExpenseLedger_ShouldReturnCorrectSubsetWithoutDuplicates() {
        transactionTemplate.executeWithoutResult(status -> {
            // 1. Expense A: Target user is ONLY the payer (not in splits/participants)
            ExpenseEntity expenseA = ExpenseEntity.builder()
                    .id("exp-a")
                    .groupId("group-1")
                    .description("Expense A")
                    .category(ExpenseCategory.FOOD)
                    .categoryType(ExpenseCategoryType.SYSTEM)
                    .money(new Money(new BigDecimal("10.00"), CurrencyCode.USD))
                    .paidByUserId(targetUserId)
                    .createdByUserId("user-other")
                    .status(ExpenseStatus.DRAFT)
                    .splitType(SplitType.EQUAL)
                    .expenseDate(LocalDate.now())
                    .participants(new ArrayList<>())
                    .splits(new ArrayList<>())
                    .attachments(new ArrayList<>())
                    .build();

            // Add other user to splits
            ExpenseParticipantEntity pA = ExpenseParticipantEntity.builder()
                    .id("part-a")
                    .expense(expenseA)
                    .userId("user-other")
                    .build();
            expenseA.getParticipants().add(pA);

            ExpenseSplitEntity sA = ExpenseSplitEntity.builder()
                    .id("split-a")
                    .expense(expenseA)
                    .userId("user-other")
                    .owedAmount(new BigDecimal("10.00"))
                    .allocationValue(new BigDecimal("1.00"))
                    .build();
            expenseA.getSplits().add(sA);

            expenseRepository.save(expenseA);

            // 2. Expense B: Target user is ONLY a split participant (someone else paid)
            ExpenseEntity expenseB = ExpenseEntity.builder()
                    .id("exp-b")
                    .groupId("group-1")
                    .description("Expense B")
                    .category(ExpenseCategory.TRANSPORT)
                    .categoryType(ExpenseCategoryType.SYSTEM)
                    .money(new Money(new BigDecimal("30.00"), CurrencyCode.USD))
                    .paidByUserId("user-other")
                    .createdByUserId("user-other")
                    .status(ExpenseStatus.DRAFT)
                    .splitType(SplitType.EQUAL)
                    .expenseDate(LocalDate.now())
                    .participants(new ArrayList<>())
                    .splits(new ArrayList<>())
                    .attachments(new ArrayList<>())
                    .build();

            ExpenseParticipantEntity pB = ExpenseParticipantEntity.builder()
                    .id("part-b")
                    .expense(expenseB)
                    .userId(targetUserId)
                    .build();
            expenseB.getParticipants().add(pB);

            ExpenseSplitEntity sB = ExpenseSplitEntity.builder()
                    .id("split-b")
                    .expense(expenseB)
                    .userId(targetUserId)
                    .owedAmount(new BigDecimal("30.00"))
                    .allocationValue(new BigDecimal("1.00"))
                    .build();
            expenseB.getSplits().add(sB);

            expenseRepository.save(expenseB);

            // 3. Expense C: Target user is BOTH the payer and participant
            ExpenseEntity expenseC = ExpenseEntity.builder()
                    .id("exp-c")
                    .groupId("group-2")
                    .description("Expense C")
                    .category(ExpenseCategory.LODGING)
                    .categoryType(ExpenseCategoryType.SYSTEM)
                    .money(new Money(new BigDecimal("50.00"), CurrencyCode.USD))
                    .paidByUserId(targetUserId)
                    .createdByUserId("user-other")
                    .status(ExpenseStatus.DRAFT)
                    .splitType(SplitType.EQUAL)
                    .expenseDate(LocalDate.now())
                    .participants(new ArrayList<>())
                    .splits(new ArrayList<>())
                    .attachments(new ArrayList<>())
                    .build();

            ExpenseParticipantEntity pC = ExpenseParticipantEntity.builder()
                    .id("part-c")
                    .expense(expenseC)
                    .userId(targetUserId)
                    .build();
            expenseC.getParticipants().add(pC);

            ExpenseSplitEntity sC = ExpenseSplitEntity.builder()
                    .id("split-c")
                    .expense(expenseC)
                    .userId(targetUserId)
                    .owedAmount(new BigDecimal("50.00"))
                    .allocationValue(new BigDecimal("1.00"))
                    .build();
            expenseC.getSplits().add(sC);

            expenseRepository.save(expenseC);

            // 4. Expense D: Unrelated group expense involving only others
            ExpenseEntity expenseD = ExpenseEntity.builder()
                    .id("exp-d")
                    .groupId("group-3")
                    .description("Expense D")
                    .category(ExpenseCategory.ENTERTAINMENT)
                    .categoryType(ExpenseCategoryType.SYSTEM)
                    .money(new Money(new BigDecimal("100.00"), CurrencyCode.USD))
                    .paidByUserId("user-other")
                    .createdByUserId("user-other")
                    .status(ExpenseStatus.DRAFT)
                    .splitType(SplitType.EQUAL)
                    .expenseDate(LocalDate.now())
                    .participants(new ArrayList<>())
                    .splits(new ArrayList<>())
                    .attachments(new ArrayList<>())
                    .build();

            ExpenseParticipantEntity pD = ExpenseParticipantEntity.builder()
                    .id("part-d")
                    .expense(expenseD)
                    .userId("user-other")
                    .build();
            expenseD.getParticipants().add(pD);

            ExpenseSplitEntity sD = ExpenseSplitEntity.builder()
                    .id("split-d")
                    .expense(expenseD)
                    .userId("user-other")
                    .owedAmount(new BigDecimal("100.00"))
                    .allocationValue(new BigDecimal("1.00"))
                    .build();
            expenseD.getSplits().add(sD);

            expenseRepository.save(expenseD);
        });

        // Query User Ledger via Query Service
        Page<ExpenseDto> page = expenseQueryService.getUserExpenses(targetUserId, PageRequest.of(0, 10));

        // Verify total elements matches exactly 3 (exp-a, exp-b, exp-c)
        assertThat(page.getTotalElements()).isEqualTo(3);
        
        // Assert specific IDs are returned
        java.util.List<String> returnedIds = page.getContent().stream().map(ExpenseDto::id).toList();
        assertThat(returnedIds).containsExactlyInAnyOrder("exp-a", "exp-b", "exp-c");
    }
}
