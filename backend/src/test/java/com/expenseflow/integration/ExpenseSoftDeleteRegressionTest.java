package com.expenseflow.integration;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.expense.domain.entity.ExpenseEntity;
import com.expenseflow.expense.domain.repository.ExpenseRepository;
import com.expenseflow.expense.domain.valueobject.ExpenseCategory;
import com.expenseflow.expense.domain.valueobject.SplitType;
import com.expenseflow.expense.dto.CreateExpenseRequest;
import com.expenseflow.expense.dto.ExpenseDto;
import com.expenseflow.expense.service.ExpenseCommandService;
import com.expenseflow.expense.service.ExpenseQueryService;
import com.expenseflow.group.service.GroupQueryService;
import com.expenseflow.trip.service.query.TripQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ExpenseSoftDeleteRegressionTest extends BaseIntegrationTest {

    @Autowired
    private ExpenseCommandService expenseCommandService;

    @Autowired
    private ExpenseQueryService expenseQueryService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @MockBean
    private GroupQueryService groupQueryService;

    @MockBean
    private TripQueryService tripQueryService;

    private final String groupId = "test-group-soft-delete";
    private final String userId = "user-1";

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();

        // Stub validations
        Mockito.when(groupQueryService.isGroupActive(Mockito.anyString())).thenReturn(true);
        Mockito.when(groupQueryService.isUserGroupMember(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    }

    @Test
    void testSoftDelete_ShouldFilterFromLedgerButRetainInDatabase() {
        // 1. Create shared expense
        Map<String, BigDecimal> allocationValues = new HashMap<>();
        allocationValues.put("user-1", new BigDecimal("1.00"));

        CreateExpenseRequest createRequest = new CreateExpenseRequest(
                null,
                "Soft Delete Test Expense",
                ExpenseCategory.FOOD,
                new BigDecimal("100.00"),
                "USD",
                userId,
                LocalDate.now(),
                SplitType.EQUAL,
                allocationValues
        );

        ExpenseDto created = expenseCommandService.createExpense(groupId, createRequest, userId);

        // Verify active in ledger
        Page<ExpenseDto> pageBefore = expenseQueryService.getUserExpenses(userId, PageRequest.of(0, 10));
        assertThat(pageBefore.getTotalElements()).isEqualTo(1);
        assertThat(pageBefore.getContent().get(0).id()).isEqualTo(created.id());

        // 2. Soft delete the expense
        expenseCommandService.deleteExpense(created.id(), userId);

        // Verify absent from ledger
        Page<ExpenseDto> pageAfter = expenseQueryService.getUserExpenses(userId, PageRequest.of(0, 10));
        assertThat(pageAfter.getTotalElements()).isEqualTo(0);

        // 3. Verify physical record still exists in the database with isDeleted = true
        ExpenseEntity dbEntity = expenseRepository.findById(created.id()).orElseThrow();
        assertThat(dbEntity.isDeleted()).isTrue();
        assertThat(dbEntity.getDeletedBy()).isEqualTo(userId);
        assertThat(dbEntity.getDeletedAt()).isNotNull();
    }
}
