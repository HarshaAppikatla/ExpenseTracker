package com.expenseflow.core.event;

import java.math.BigDecimal;

public class SavingsGoalCompletedEvent extends ApplicationEvent {
    private final String goalId;
    private final String userId;
    private final String title;
    private final BigDecimal targetAmount;

    public SavingsGoalCompletedEvent(Object source, String goalId, String userId, String title, BigDecimal targetAmount) {
        super(source);
        this.goalId = goalId;
        this.userId = userId;
        this.title = title;
        this.targetAmount = targetAmount;
    }

    public String getGoalId() { return goalId; }
    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public BigDecimal getTargetAmount() { return targetAmount; }
}
