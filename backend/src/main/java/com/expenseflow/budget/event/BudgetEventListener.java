package com.expenseflow.budget.event;

import com.expenseflow.core.event.ExpenseCreatedEvent;
import com.expenseflow.core.event.ExpenseDeletedEvent;
import com.expenseflow.core.event.ExpenseUpdatedEvent;
import com.expenseflow.budget.service.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BudgetEventListener {

    private final BudgetService budgetService;

    @EventListener
    public void handleExpenseCreated(ExpenseCreatedEvent event) {
        log.debug("BudgetListener: ExpenseCreatedEvent caught for user {}", event.getUserId());
        budgetService.checkBudgetUtilization(
                event.getUserId(),
                event.getCategoryId(),
                event.getExpenseDate().getYear(),
                event.getExpenseDate().getMonthValue()
        );
    }

    @EventListener
    public void handleExpenseUpdated(ExpenseUpdatedEvent event) {
        log.debug("BudgetListener: ExpenseUpdatedEvent caught for user {}", event.getUserId());
        budgetService.checkBudgetUtilization(
                event.getUserId(),
                event.getCategoryId(),
                event.getExpenseDate().getYear(),
                event.getExpenseDate().getMonthValue()
        );
    }

    @EventListener
    public void handleExpenseDeleted(ExpenseDeletedEvent event) {
        log.debug("BudgetListener: ExpenseDeletedEvent caught for user {}", event.getUserId());
        budgetService.checkBudgetUtilization(
                event.getUserId(),
                event.getCategoryId(),
                event.getExpenseDate().getYear(),
                event.getExpenseDate().getMonthValue()
        );
    }
}
