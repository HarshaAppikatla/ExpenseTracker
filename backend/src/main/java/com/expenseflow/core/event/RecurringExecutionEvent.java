package com.expenseflow.core.event;

import java.math.BigDecimal;

public class RecurringExecutionEvent extends ApplicationEvent {
    private final String recurringId;
    private final String userId;
    private final String transactionType;
    private final BigDecimal amount;
    private final String description;

    public RecurringExecutionEvent(Object source, String recurringId, String userId, String transactionType, BigDecimal amount, String description) {
        super(source);
        this.recurringId = recurringId;
        this.userId = userId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
    }

    public String getRecurringId() { return recurringId; }
    public String getUserId() { return userId; }
    public String getTransactionType() { return transactionType; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
}
