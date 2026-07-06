package com.expenseflow.expense.domain.event;

import com.expenseflow.core.event.ApplicationEvent;

public class ExpenseDeletedEvent extends ApplicationEvent {
    private final String expenseId;
    private final String groupId;
    private final String userId;

    public ExpenseDeletedEvent(Object source, String expenseId, String groupId, String userId) {
        super(source);
        this.expenseId = expenseId;
        this.groupId = groupId;
        this.userId = userId;
    }

    public String getExpenseId() { return expenseId; }
    public String getGroupId() { return groupId; }
    public String getUserId() { return userId; }
}
