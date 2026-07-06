package com.expenseflow.expense.domain.strategy;

import com.expenseflow.expense.domain.valueobject.CurrencyCode;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExactSplitCalculator implements SplitCalculator {
    @Override
    public Map<String, BigDecimal> calculate(BigDecimal totalAmount, Map<String, BigDecimal> allocationValues, CurrencyCode currency) {
        if (allocationValues == null || allocationValues.isEmpty()) {
            throw new IllegalArgumentException("No allocation values specified for exact split");
        }

        Map<String, BigDecimal> splits = new LinkedHashMap<>();
        BigDecimal sum = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : allocationValues.entrySet()) {
            BigDecimal val = entry.getValue() != null ? entry.getValue() : BigDecimal.ZERO;
            BigDecimal roundedVal = val.setScale(2, java.math.RoundingMode.HALF_UP);
            splits.put(entry.getKey(), roundedVal);
            sum = sum.add(roundedVal);
        }

        if (sum.compareTo(totalAmount.setScale(2, java.math.RoundingMode.HALF_UP)) != 0) {
            throw new IllegalArgumentException("The sum of exact splits (" + sum + ") does not equal the total amount (" + totalAmount + ")");
        }

        return splits;
    }
}
