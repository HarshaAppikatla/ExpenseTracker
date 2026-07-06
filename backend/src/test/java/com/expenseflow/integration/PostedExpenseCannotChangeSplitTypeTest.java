package com.expenseflow.integration;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.expense.domain.repository.ExpenseRepository;
import com.expenseflow.expense.domain.valueobject.ExpenseCategory;
import com.expenseflow.expense.domain.valueobject.ExpenseStatus;
import com.expenseflow.expense.domain.valueobject.SplitType;
import com.expenseflow.expense.dto.CreateExpenseRequest;
import com.expenseflow.expense.dto.ExpenseDto;
import com.expenseflow.expense.dto.UpdateExpenseRequest;
import com.expenseflow.expense.service.ExpenseCommandService;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PostedExpenseCannotChangeSplitTypeTest extends BaseIntegrationTest {

    @Autowired
    private ExpenseCommandService expenseCommandService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @MockBean
    private GroupQueryService groupQueryService;

    @MockBean
    private TripQueryService tripQueryService;

    private final String groupId = "test-group-2";
    private final String userId = "user-1";

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();

        // Stub validations
        Mockito.when(groupQueryService.isGroupActive(Mockito.anyString())).thenReturn(true);
        Mockito.when(groupQueryService.isUserGroupMember(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    }

    @Test
    void testPostedExpense_ShouldBlockMetadataUpdatesAndSplitChanges() {
        // 1. Create a DRAFT expense
        Map<String, BigDecimal> allocationValues = new HashMap<>();
        allocationValues.put("user-1", new BigDecimal("1.00"));

        CreateExpenseRequest createRequest = new CreateExpenseRequest(
                null,
                "Draft Expense",
                ExpenseCategory.FOOD,
                new BigDecimal("100.00"),
                "USD",
                userId,
                LocalDate.now(),
                SplitType.EQUAL,
                allocationValues
        );

        ExpenseDto created = expenseCommandService.createExpense(groupId, createRequest, userId);
        assertThat(created.status()).isEqualTo(ExpenseStatus.DRAFT);

        // 2. Transition status to POSTED
        ExpenseDto posted = expenseCommandService.transitionStatus(created.id(), ExpenseStatus.POSTED, userId);
        assertThat(posted.status()).isEqualTo(ExpenseStatus.POSTED);

        // 3. Attempt to update it and verify it throws IllegalStateException
        UpdateExpenseRequest updateRequest = new UpdateExpenseRequest(
                "Trying to edit posted description",
                ExpenseCategory.FOOD,
                new BigDecimal("100.00"),
                "USD",
                userId,
                LocalDate.now(),
                SplitType.EQUAL,
                allocationValues
        );

        assertThatThrownBy(() -> {
            expenseCommandService.updateExpense(created.id(), updateRequest, userId);
        }).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("cannot be modified");

        // 4. Verify in DB that description and splitType remain unchanged
        ExpenseDto dbRecord = expenseCommandService.transitionStatus(created.id(), ExpenseStatus.POSTED, userId);
        assertThat(dbRecord.description()).isEqualTo("Draft Expense");
        assertThat(dbRecord.splitType()).isEqualTo(SplitType.EQUAL);
    }
}
