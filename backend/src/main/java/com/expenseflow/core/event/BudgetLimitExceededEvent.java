package com.expenseflow.core.event;

import java.math.BigDecimal;

public class BudgetLimitExceededEvent extends ApplicationEvent {
    private final String userId;
    private final String categoryId;
    private final String categoryName;
    private final int year;
    private final int month;
    private final BigDecimal monthlyLimit;
    private final BigDecimal currentSpent;

    public BudgetLimitExceededEvent(Object source, String userId, String categoryId, String categoryName, int year, int month, BigDecimal monthlyLimit, BigDecimal currentSpent) {
        super(source);
        this.userId = userId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.year = year;
        this.month = month;
        this.monthlyLimit = monthlyLimit;
        this.currentSpent = currentSpent;
    }

    public String getUserId() { return userId; }
    public String getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public BigDecimal getCurrentSpent() { return currentSpent; }
}
