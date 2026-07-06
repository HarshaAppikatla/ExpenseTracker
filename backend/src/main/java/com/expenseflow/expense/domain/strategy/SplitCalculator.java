package com.expenseflow.expense.domain.strategy;

import com.expenseflow.expense.domain.valueobject.CurrencyCode;
import java.math.BigDecimal;
import java.util.Map;

public interface SplitCalculator {
    Map<String, BigDecimal> calculate(BigDecimal totalAmount, Map<String, BigDecimal> allocationValues, CurrencyCode currency);

    static Map<String, BigDecimal> adjustRoundingError(BigDecimal totalAmount, Map<String, BigDecimal> roundedSplits) {
        if (roundedSplits == null || roundedSplits.isEmpty()) {
            return roundedSplits;
        }
        BigDecimal sum = roundedSplits.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remainder = totalAmount.subtract(sum);

        if (remainder.compareTo(BigDecimal.ZERO) != 0) {
            String firstKey = roundedSplits.keySet().iterator().next();
            BigDecimal adjustedVal = roundedSplits.get(firstKey).add(remainder);
            roundedSplits.put(firstKey, adjustedVal);
        }
        return roundedSplits;
    }
}
