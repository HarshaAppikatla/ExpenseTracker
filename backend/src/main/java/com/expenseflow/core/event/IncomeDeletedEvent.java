package com.expenseflow.core.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class IncomeDeletedEvent extends ApplicationEvent {
    private final String incomeId;
    private final String userId;
    private final String source;
    private final BigDecimal amount;
    private final String currencyCode;
    private final LocalDateTime incomeDate;

    public IncomeDeletedEvent(Object source, String incomeId, String userId, String sourceName, BigDecimal amount, String currencyCode, LocalDateTime incomeDate) {
        super(source);
        this.incomeId = incomeId;
        this.userId = userId;
        this.source = sourceName;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.incomeDate = incomeDate;
    }

    public String getIncomeId() { return incomeId; }
    public String getUserId() { return userId; }
    public String getSource() { return source; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrencyCode() { return currencyCode; }
    public LocalDateTime getIncomeDate() { return incomeDate; }
}
