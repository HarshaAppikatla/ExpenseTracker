package com.expenseflow.notification.listener;

import com.expenseflow.core.event.*;
import com.expenseflow.notification.domain.valueobject.NotificationPriority;
import com.expenseflow.notification.domain.valueobject.NotificationCategory;
import com.expenseflow.notification.domain.valueobject.NotificationType;
import com.expenseflow.notification.service.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listens for domain events published by other bounded contexts and
 * delegates notification creation to {@link NotificationCommandService}.
 *
 * <p>Architectural constraints (ADR-006):
 * <ul>
 *   <li>Uses {@code @TransactionalEventListener(AFTER_COMMIT)} so notifications
 *       are only generated for successfully committed transactions.</li>
 *   <li>Must not import from Expense, Settlement, Group, or Trip contexts directly</li>
 *   <li>All notification content is derived from the published event payload only</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationCommandService notificationCommandService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBudgetLimitExceeded(BudgetLimitExceededEvent event) {
        log.info("Received BudgetLimitExceededEvent for user {}", event.getUserId());
        String msg = String.format(
                "Your budget for category '%s' (Limit: %.2f) has been exceeded! Total spent: %.2f",
                event.getCategoryName(), event.getMonthlyLimit(), event.getCurrentSpent());
        notificationCommandService.createNotification(
                event.getUserId(),
                NotificationType.BUDGET_LIMIT_EXCEEDED,
                NotificationCategory.EXPENSE,
                NotificationPriority.HIGH,
                "Budget Exceeded",
                msg,
                null);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBudgetWarning(BudgetWarningEvent event) {
        log.info("Received BudgetWarningEvent for user {}", event.getUserId());
        String msg = String.format(
                "Your budget for category '%s' (Limit: %.2f) is at %d%% utilization. Total spent: %.2f",
                event.getCategoryName(), event.getMonthlyLimit(), event.getAlertPercentage(), event.getCurrentSpent());
        notificationCommandService.createNotification(
                event.getUserId(),
                NotificationType.BUDGET_WARNING,
                NotificationCategory.EXPENSE,
                NotificationPriority.NORMAL,
                "Budget Warning",
                msg,
                null);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRecurringExecution(RecurringExecutionEvent event) {
        log.info("Received RecurringExecutionEvent for user {}", event.getUserId());
        String msg = String.format(
                "Successfully executed recurring %s: %s (Amount: %.2f)",
                event.getTransactionType(), event.getDescription(), event.getAmount());
        notificationCommandService.createNotification(
                event.getUserId(),
                NotificationType.RECURRING_EXECUTION,
                NotificationCategory.EXPENSE,
                NotificationPriority.NORMAL,
                "Recurring Payment Executed",
                msg,
                null);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRecurringExecutionFailed(RecurringExecutionFailedEvent event) {
        log.info("Received RecurringExecutionFailedEvent for user {}", event.getUserId());
        String msg = String.format(
                "Failed to execute recurring template ID %s: %s",
                event.getRecurringId(), event.getErrorMessage());
        notificationCommandService.createNotification(
                event.getUserId(),
                NotificationType.RECURRING_EXECUTION_FAILED,
                NotificationCategory.EXPENSE,
                NotificationPriority.CRITICAL,
                "Recurring Payment Failed",
                msg,
                null);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSavingsGoalCompleted(SavingsGoalCompletedEvent event) {
        log.info("Received SavingsGoalCompletedEvent for user {}", event.getUserId());
        String msg = String.format(
                "Congratulations! You reached your savings goal target of %.2f for '%s'!",
                event.getTargetAmount(), event.getTitle());
        notificationCommandService.createNotification(
                event.getUserId(),
                NotificationType.SAVINGS_GOAL_COMPLETED,
                NotificationCategory.EXPENSE,
                NotificationPriority.HIGH,
                "Savings Goal Reached",
                msg,
                null);
    }
}
