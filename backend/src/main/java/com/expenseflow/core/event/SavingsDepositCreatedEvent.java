package com.expenseflow.core.event;

import java.math.BigDecimal;

public class SavingsDepositCreatedEvent extends ApplicationEvent {
    private final String depositId;
    private final String goalId;
    private final String goalTitle;
    private final String userId;
    private final BigDecimal amount;

    public SavingsDepositCreatedEvent(Object source, String depositId, String goalId, String goalTitle, String userId, BigDecimal amount) {
        super(source);
        this.depositId = depositId;
        this.goalId = goalId;
        this.goalTitle = goalTitle;
        this.userId = userId;
        this.amount = amount;
    }

    public String getDepositId() { return depositId; }
    public String getGoalId() { return goalId; }
    public String getGoalTitle() { return goalTitle; }
    public String getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
}
