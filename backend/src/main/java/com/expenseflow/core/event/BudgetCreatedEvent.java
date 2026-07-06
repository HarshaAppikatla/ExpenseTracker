package com.expenseflow.core.event;

import java.math.BigDecimal;

public class BudgetCreatedEvent extends ApplicationEvent {
    private final String budgetId;
    private final String userId;
    private final String categoryId;
    private final BigDecimal monthlyLimit;

    public BudgetCreatedEvent(Object source, String budgetId, String userId, String categoryId, BigDecimal monthlyLimit) {
        super(source);
        this.budgetId = budgetId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.monthlyLimit = monthlyLimit;
    }

    public String getBudgetId() { return budgetId; }
    public String getUserId() { return userId; }
    public String getCategoryId() { return categoryId; }
    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
}
