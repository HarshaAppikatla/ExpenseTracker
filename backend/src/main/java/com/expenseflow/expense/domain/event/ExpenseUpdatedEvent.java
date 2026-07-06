package com.expenseflow.expense.domain.event;

import com.expenseflow.core.event.ApplicationEvent;
import java.math.BigDecimal;

public class ExpenseUpdatedEvent extends ApplicationEvent {
    private final String expenseId;
    private final String groupId;
    private final BigDecimal amount;
    private final String currency;
    private final String userId;

    public ExpenseUpdatedEvent(Object source, String expenseId, String groupId, BigDecimal amount, String currency, String userId) {
        super(source);
        this.expenseId = expenseId;
        this.groupId = groupId;
        this.amount = amount;
        this.currency = currency;
        this.userId = userId;
    }

    public String getExpenseId() { return expenseId; }
    public String getGroupId() { return groupId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getUserId() { return userId; }
}
