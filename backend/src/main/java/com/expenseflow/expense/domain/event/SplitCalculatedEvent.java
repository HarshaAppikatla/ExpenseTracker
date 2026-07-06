package com.expenseflow.expense.domain.event;

import com.expenseflow.core.event.ApplicationEvent;
import com.expenseflow.expense.domain.valueobject.SplitType;
import java.math.BigDecimal;
import java.util.Map;

public class SplitCalculatedEvent extends ApplicationEvent {
    private final String expenseId;
    private final SplitType splitType;
    private final Map<String, BigDecimal> owedAmounts;

    public SplitCalculatedEvent(Object source, String expenseId, SplitType splitType, Map<String, BigDecimal> owedAmounts) {
        super(source);
        this.expenseId = expenseId;
        this.splitType = splitType;
        this.owedAmounts = owedAmounts;
    }

    public String getExpenseId() { return expenseId; }
    public SplitType getSplitType() { return splitType; }
    public Map<String, BigDecimal> getOwedAmounts() { return owedAmounts; }
}
