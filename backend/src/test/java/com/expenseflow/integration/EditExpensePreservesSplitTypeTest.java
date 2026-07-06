package com.expenseflow.integration;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.expense.domain.repository.ExpenseRepository;
import com.expenseflow.expense.domain.valueobject.ExpenseCategory;
import com.expenseflow.expense.domain.valueobject.SplitType;
import com.expenseflow.expense.dto.CreateExpenseRequest;
import com.expenseflow.expense.dto.ExpenseDto;
import com.expenseflow.expense.dto.UpdateExpenseRequest;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EditExpensePreservesSplitTypeTest extends BaseIntegrationTest {

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

    private final String groupId = "test-group-1";
    private final String userId = "user-1";

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();

        // Stub validations
        Mockito.when(groupQueryService.isGroupActive(Mockito.anyString())).thenReturn(true);
        Mockito.when(groupQueryService.isUserGroupMember(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    }

    @Test
    void testEditExpense_ShouldPreserveSplitType() {
        // 1. Create EXACT split expense
        Map<String, BigDecimal> allocationValues = new HashMap<>();
        allocationValues.put("user-1", new BigDecimal("40.00"));
        allocationValues.put("user-2", new BigDecimal("60.00"));

        CreateExpenseRequest createRequest = new CreateExpenseRequest(
                null,
                "Original EXACT Expense",
                ExpenseCategory.FOOD,
                new BigDecimal("100.00"),
                "USD",
                userId,
                LocalDate.now(),
                SplitType.EXACT,
                allocationValues
        );

        ExpenseDto created = expenseCommandService.createExpense(groupId, createRequest, userId);
        assertThat(created.splitType()).isEqualTo(SplitType.EXACT);

        // 2. Update description metadata while keeping EXACT split
        UpdateExpenseRequest updateRequest = new UpdateExpenseRequest(
                "Updated EXACT Expense Description",
                ExpenseCategory.FOOD,
                new BigDecimal("100.00"),
                "USD",
                userId,
                LocalDate.now(),
                SplitType.EXACT,
                allocationValues
        );

        ExpenseDto updated = expenseCommandService.updateExpense(created.id(), updateRequest, userId);
        assertThat(updated.splitType()).isEqualTo(SplitType.EXACT);
        assertThat(updated.description()).isEqualTo("Updated EXACT Expense Description");

        // 3. Reload from database and verify SplitType is STILL EXACT
        ExpenseDto reloaded = expenseQueryService.getExpenseDetails(created.id(), userId);
        assertThat(reloaded.splitType()).isEqualTo(SplitType.EXACT);
    }
}
