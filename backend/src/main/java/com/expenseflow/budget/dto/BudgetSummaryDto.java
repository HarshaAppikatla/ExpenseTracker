package com.expenseflow.budget.dto;

import java.math.BigDecimal;

public record BudgetSummaryDto(
    BigDecimal totalLimits,
    BigDecimal totalSpent,
    double overallUtilizationPercentage
) {}
