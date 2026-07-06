package com.expenseflow.core.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ExpenseCreatedEvent extends ApplicationEvent {
    private final String expenseId;
    private final String userId;
    private final String categoryId;
    private final BigDecimal amount;
    private final String currencyCode;
    private final LocalDateTime expenseDate;

    public ExpenseCreatedEvent(Object source, String expenseId, String userId, String categoryId, BigDecimal amount, String currencyCode, LocalDateTime expenseDate) {
        super(source);
        this.expenseId = expenseId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.expenseDate = expenseDate;
    }

    public String getExpenseId() { return expenseId; }
    public String getUserId() { return userId; }
    public String getCategoryId() { return categoryId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrencyCode() { return currencyCode; }
    public LocalDateTime getExpenseDate() { return expenseDate; }
}
