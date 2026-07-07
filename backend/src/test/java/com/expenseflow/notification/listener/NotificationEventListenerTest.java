package com.expenseflow.notification.listener;

import com.expenseflow.core.event.*;
import com.expenseflow.notification.domain.valueobject.NotificationPriority;
import com.expenseflow.notification.domain.valueobject.NotificationCategory;
import com.expenseflow.notification.domain.valueobject.NotificationType;
import com.expenseflow.notification.service.NotificationCommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventListener unit tests")
class NotificationEventListenerTest {

    @Mock
    private NotificationCommandService commandService;

    @InjectMocks
    private NotificationEventListener eventListener;

    @Test
    @DisplayName("Should handle BudgetLimitExceededEvent and call createNotification")
    void testHandleBudgetLimitExceeded() {
        BudgetLimitExceededEvent event = new BudgetLimitExceededEvent(
                this, "user-123", "cat-1", "Food", 2026, 7,
                new BigDecimal("500.00"), new BigDecimal("550.00"));

        eventListener.handleBudgetLimitExceeded(event);

        verify(commandService).createNotification(
                eq("user-123"),
                eq(NotificationType.BUDGET_LIMIT_EXCEEDED),
                eq(NotificationCategory.EXPENSE),
                eq(NotificationPriority.HIGH),
                eq("Budget Exceeded"),
                eq("Your budget for category 'Food' (Limit: 500.00) has been exceeded! Total spent: 550.00"),
                eq(null)
        );
    }

    @Test
    @DisplayName("Should handle BudgetWarningEvent and call createNotification")
    void testHandleBudgetWarning() {
        BudgetWarningEvent event = new BudgetWarningEvent(
                this, "user-123", "cat-2", "Rent", 2026, 7,
                new BigDecimal("1000.00"), new BigDecimal("900.00"), 90);

        eventListener.handleBudgetWarning(event);

        verify(commandService).createNotification(
                eq("user-123"),
                eq(NotificationType.BUDGET_WARNING),
                eq(NotificationCategory.EXPENSE),
                eq(NotificationPriority.NORMAL),
                eq("Budget Warning"),
                eq("Your budget for category 'Rent' (Limit: 1000.00) is at 90% utilization. Total spent: 900.00"),
                eq(null)
        );
    }

    @Test
    @DisplayName("Should handle RecurringExecutionEvent and call createNotification")
    void testHandleRecurringExecution() {
        RecurringExecutionEvent event = new RecurringExecutionEvent(
                this, "recur-1", "user-123", "EXPENSE",
                new BigDecimal("15.99"), "Netflix subscription");

        eventListener.handleRecurringExecution(event);

        verify(commandService).createNotification(
                eq("user-123"),
                eq(NotificationType.RECURRING_EXECUTION),
                eq(NotificationCategory.EXPENSE),
                eq(NotificationPriority.NORMAL),
                eq("Recurring Payment Executed"),
                eq("Successfully executed recurring EXPENSE: Netflix subscription (Amount: 15.99)"),
                eq(null)
        );
    }

    @Test
    @DisplayName("Should handle RecurringExecutionFailedEvent and call createNotification")
    void testHandleRecurringExecutionFailed() {
        RecurringExecutionFailedEvent event = new RecurringExecutionFailedEvent(
                this, "template-abc", "user-123", "Card declined");

        eventListener.handleRecurringExecutionFailed(event);

        verify(commandService).createNotification(
                eq("user-123"),
                eq(NotificationType.RECURRING_EXECUTION_FAILED),
                eq(NotificationCategory.EXPENSE),
                eq(NotificationPriority.CRITICAL),
                eq("Recurring Payment Failed"),
                eq("Failed to execute recurring template ID template-abc: Card declined"),
                eq(null)
        );
    }

    @Test
    @DisplayName("Should handle SavingsGoalCompletedEvent and call createNotification")
    void testHandleSavingsGoalCompleted() {
        SavingsGoalCompletedEvent event = new SavingsGoalCompletedEvent(
                this, "goal-1", "user-123", "New Car Goal", new BigDecimal("25000.00"));

        eventListener.handleSavingsGoalCompleted(event);

        verify(commandService).createNotification(
                eq("user-123"),
                eq(NotificationType.SAVINGS_GOAL_COMPLETED),
                eq(NotificationCategory.EXPENSE),
                eq(NotificationPriority.HIGH),
                eq("Savings Goal Reached"),
                eq("Congratulations! You reached your savings goal target of 25000.00 for 'New Car Goal'!"),
                eq(null)
        );
    }
}
