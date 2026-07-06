package com.expenseflow.notification.listener;

import com.expenseflow.core.event.*;
import com.expenseflow.notification.domain.valueobject.NotificationPriority;
import com.expenseflow.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void handleBudgetLimitExceeded(BudgetLimitExceededEvent event) {
        log.info("Received BudgetLimitExceededEvent for user {}", event.getUserId());
        String msg = String.format("Your budget for category '%s' (Limit: %.2f) has been exceeded! Total spent: %.2f",
                event.getCategoryName(), event.getMonthlyLimit(), event.getCurrentSpent());
        notificationService.createNotification(
                event.getUserId(),
                "BUDGET_LIMIT",
                "Budget Exceeded",
                msg,
                NotificationPriority.HIGH
        );
    }

    @EventListener
    public void handleBudgetWarning(BudgetWarningEvent event) {
        log.info("Received BudgetWarningEvent for user {}", event.getUserId());
        String msg = String.format("Your budget for category '%s' (Limit: %.2f) is at %d%% utilization. Total spent: %.2f",
                event.getCategoryName(), event.getMonthlyLimit(), event.getAlertPercentage(), event.getCurrentSpent());
        notificationService.createNotification(
                event.getUserId(),
                "BUDGET_WARNING",
                "Budget Warning",
                msg,
                NotificationPriority.NORMAL
        );
    }

    @EventListener
    public void handleRecurringExecution(RecurringExecutionEvent event) {
        log.info("Received RecurringExecutionEvent for user {}", event.getUserId());
        String msg = String.format("Successfully executed recurring %s: %s (Amount: %.2f)",
                event.getTransactionType(), event.getDescription(), event.getAmount());
        notificationService.createNotification(
                event.getUserId(),
                "RECURRING_EXECUTION",
                "Recurring Payment Executed",
                msg,
                NotificationPriority.NORMAL
        );
    }

    @EventListener
    public void handleRecurringExecutionFailed(RecurringExecutionFailedEvent event) {
        log.info("Received RecurringExecutionFailedEvent for user {}", event.getUserId());
        String msg = String.format("Failed to execute recurring template ID %s: %s",
                event.getRecurringId(), event.getErrorMessage());
        notificationService.createNotification(
                event.getUserId(),
                "RECURRING_FAILED",
                "Recurring Payment Failed",
                msg,
                NotificationPriority.CRITICAL
        );
    }

    @EventListener
    public void handleSavingsGoalCompleted(SavingsGoalCompletedEvent event) {
        log.info("Received SavingsGoalCompletedEvent for user {}", event.getUserId());
        String msg = String.format("Congratulations! You reached your savings goal target of %.2f for '%s'!",
                event.getTargetAmount(), event.getTitle());
        notificationService.createNotification(
                event.getUserId(),
                "SAVINGS_COMPLETED",
                "Savings Goal Reached",
                msg,
                NotificationPriority.HIGH
        );
    }
}
