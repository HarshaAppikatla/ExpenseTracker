package com.expenseflow.expense.domain.event;

import com.expenseflow.core.event.ApplicationEvent;
import java.math.BigDecimal;

public class ExpenseCreatedEvent extends ApplicationEvent {
    private final String expenseId;
    private final String groupId;
    private final String tripId;
    private final BigDecimal amount;
    private final String currency;
    private final String paidByUserId;

    public ExpenseCreatedEvent(Object source, String expenseId, String groupId, String tripId, BigDecimal amount, String currency, String paidByUserId) {
        super(source);
        this.expenseId = expenseId;
        this.groupId = groupId;
        this.tripId = tripId;
        this.amount = amount;
        this.currency = currency;
        this.paidByUserId = paidByUserId;
    }

    public String getExpenseId() { return expenseId; }
    public String getGroupId() { return groupId; }
    public String getTripId() { return tripId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getPaidByUserId() { return paidByUserId; }
}
