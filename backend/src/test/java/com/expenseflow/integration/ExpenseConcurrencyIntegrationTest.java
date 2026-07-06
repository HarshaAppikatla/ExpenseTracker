package com.expenseflow.integration;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.expense.domain.entity.ExpenseEntity;
import com.expenseflow.expense.domain.repository.ExpenseRepository;
import com.expenseflow.expense.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ExpenseConcurrencyIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;
    private String expenseId;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.executeWithoutResult(status -> {
            expenseRepository.deleteAll();
        });

        expenseId = transactionTemplate.execute(status -> {
            ExpenseEntity expense = ExpenseEntity.builder()
                    .id("expense-concurrency-id")
                    .groupId("group-concurrency-id")
                    .tripId("trip-concurrency-id")
                    .description("Test Concurrency Expense")
                    .category(ExpenseCategory.FOOD)
                    .categoryType(ExpenseCategoryType.SYSTEM)
                    .money(new Money(new BigDecimal("100.00"), CurrencyCode.USD))
                    .paidByUserId("user-concurrency-id")
                    .createdByUserId("user-creator-id")
                    .status(ExpenseStatus.DRAFT)
                    .splitType(SplitType.EQUAL)
                    .expenseDate(LocalDate.now())
                    .participants(new ArrayList<>())
                    .splits(new ArrayList<>())
                    .attachments(new ArrayList<>())
                    .build();
            return expenseRepository.save(expense).getId();
        });
    }

    @Test
    void testOptimisticLocking_ShouldThrowException_WhenConcurrentUpdatesOccur() {
        ExpenseEntity copy1 = transactionTemplate.execute(status -> expenseRepository.findById(expenseId).orElseThrow());
        ExpenseEntity copy2 = transactionTemplate.execute(status -> expenseRepository.findById(expenseId).orElseThrow());

        assertThat(copy1.getVersion()).isEqualTo(copy2.getVersion());

        // Update copy1
        transactionTemplate.executeWithoutResult(status -> {
            copy1.updateMetadata(
                    "Updated description by A",
                    copy1.getCategory(),
                    copy1.getCategoryType(),
                    copy1.getExpenseDate(),
                    copy1.getPaidByUserId(),
                    copy1.getMoney(),
                    copy1.getSplitType()
            );
            expenseRepository.save(copy1);
        });

        // Update copy2 and assert save fails
        assertThatThrownBy(() -> {
            transactionTemplate.executeWithoutResult(status -> {
                copy2.updateMetadata(
                        "Updated description by B",
                        copy2.getCategory(),
                        copy2.getCategoryType(),
                        copy2.getExpenseDate(),
                        copy2.getPaidByUserId(),
                        copy2.getMoney(),
                        copy2.getSplitType()
                );
                expenseRepository.save(copy2);
            });
        }).isInstanceOf(OptimisticLockingFailureException.class);
    }
}
