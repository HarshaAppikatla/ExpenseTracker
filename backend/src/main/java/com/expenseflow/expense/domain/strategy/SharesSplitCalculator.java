package com.expenseflow.expense.domain.strategy;

import com.expenseflow.expense.domain.valueobject.CurrencyCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

public class SharesSplitCalculator implements SplitCalculator {
    @Override
    public Map<String, BigDecimal> calculate(BigDecimal totalAmount, Map<String, BigDecimal> allocationValues, CurrencyCode currency) {
        if (allocationValues == null || allocationValues.isEmpty()) {
            throw new IllegalArgumentException("No shares specified for shares split");
        }

        BigDecimal sumShares = BigDecimal.ZERO;
        for (BigDecimal shares : allocationValues.values()) {
            if (shares == null || shares.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Shares must be greater than zero");
            }
            sumShares = sumShares.add(shares);
        }

        if (sumShares.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total shares must be greater than zero");
        }

        Map<String, BigDecimal> splits = new LinkedHashMap<>();
        for (Map.Entry<String, BigDecimal> entry : allocationValues.entrySet()) {
            BigDecimal shares = entry.getValue();
            BigDecimal share = totalAmount.multiply(shares).divide(sumShares, 4, RoundingMode.HALF_UP)
                    .setScale(2, RoundingMode.HALF_UP);
            splits.put(entry.getKey(), share);
        }

        return SplitCalculator.adjustRoundingError(totalAmount, splits);
    }
}
