package com.expenseflow.expense.domain.strategy;

import com.expenseflow.expense.domain.valueobject.CurrencyCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

public class PercentageSplitCalculator implements SplitCalculator {
    @Override
    public Map<String, BigDecimal> calculate(BigDecimal totalAmount, Map<String, BigDecimal> allocationValues, CurrencyCode currency) {
        if (allocationValues == null || allocationValues.isEmpty()) {
            throw new IllegalArgumentException("No percentages specified for percentage split");
        }

        BigDecimal sumPercentage = BigDecimal.ZERO;
        for (BigDecimal pct : allocationValues.values()) {
            if (pct != null) {
                sumPercentage = sumPercentage.add(pct);
            }
        }

        if (sumPercentage.setScale(2, RoundingMode.HALF_UP).compareTo(new BigDecimal("100.00")) != 0) {
            throw new IllegalArgumentException("The sum of percentages (" + sumPercentage + ") must equal 100.00");
        }

        Map<String, BigDecimal> splits = new LinkedHashMap<>();
        BigDecimal hundred = new BigDecimal("100");
        for (Map.Entry<String, BigDecimal> entry : allocationValues.entrySet()) {
            BigDecimal pct = entry.getValue() != null ? entry.getValue() : BigDecimal.ZERO;
            BigDecimal share = totalAmount.multiply(pct).divide(hundred, 4, RoundingMode.HALF_UP)
                    .setScale(2, RoundingMode.HALF_UP);
            splits.put(entry.getKey(), share);
        }

        return SplitCalculator.adjustRoundingError(totalAmount, splits);
    }
}
