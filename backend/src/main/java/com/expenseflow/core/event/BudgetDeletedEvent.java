package com.expenseflow.core.event;

public class BudgetDeletedEvent extends ApplicationEvent {
    private final String budgetId;
    private final String userId;

    public BudgetDeletedEvent(Object source, String budgetId, String userId) {
        super(source);
        this.budgetId = budgetId;
        this.userId = userId;
    }

    public String getBudgetId() { return budgetId; }
    public String getUserId() { return userId; }
}
