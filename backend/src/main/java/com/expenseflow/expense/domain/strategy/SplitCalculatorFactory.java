package com.expenseflow.expense.domain.strategy;

import com.expenseflow.expense.domain.valueobject.SplitType;
import org.springframework.stereotype.Component;

@Component
public class SplitCalculatorFactory {

    private final SplitCalculator equalSplitCalculator = new EqualSplitCalculator();
    private final SplitCalculator exactSplitCalculator = new ExactSplitCalculator();
    private final SplitCalculator percentageSplitCalculator = new PercentageSplitCalculator();
    private final SplitCalculator sharesSplitCalculator = new SharesSplitCalculator();

    public SplitCalculator get(SplitType splitType) {
        if (splitType == null) {
            throw new IllegalArgumentException("SplitType cannot be null");
        }
        return switch (splitType) {
            case EQUAL -> equalSplitCalculator;
            case EXACT -> exactSplitCalculator;
            case PERCENTAGE -> percentageSplitCalculator;
            case SHARES -> sharesSplitCalculator;
        };
    }
}
