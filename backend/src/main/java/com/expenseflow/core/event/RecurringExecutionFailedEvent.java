package com.expenseflow.core.event;

public class RecurringExecutionFailedEvent extends ApplicationEvent {
    private final String recurringId;
    private final String userId;
    private final String errorMessage;

    public RecurringExecutionFailedEvent(Object source, String recurringId, String userId, String errorMessage) {
        super(source);
        this.recurringId = recurringId;
        this.userId = userId;
        this.errorMessage = errorMessage;
    }

    public String getRecurringId() { return recurringId; }
    public String getUserId() { return userId; }
    public String getErrorMessage() { return errorMessage; }
}
