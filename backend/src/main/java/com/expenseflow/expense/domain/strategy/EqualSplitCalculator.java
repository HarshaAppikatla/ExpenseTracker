package com.expenseflow.expense.domain.strategy;

import com.expenseflow.expense.domain.valueobject.CurrencyCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

public class EqualSplitCalculator implements SplitCalculator {
    @Override
    public Map<String, BigDecimal> calculate(BigDecimal totalAmount, Map<String, BigDecimal> allocationValues, CurrencyCode currency) {
        if (allocationValues == null || allocationValues.isEmpty()) {
            throw new IllegalArgumentException("No participants specified for equal split");
        }

        int count = allocationValues.size();
        BigDecimal countDec = new BigDecimal(count);
        BigDecimal equalShare = totalAmount.divide(countDec, 4, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);

        Map<String, BigDecimal> splits = new LinkedHashMap<>();
        for (String userId : allocationValues.keySet()) {
            splits.put(userId, equalShare);
        }

        return SplitCalculator.adjustRoundingError(totalAmount, splits);
    }
}
