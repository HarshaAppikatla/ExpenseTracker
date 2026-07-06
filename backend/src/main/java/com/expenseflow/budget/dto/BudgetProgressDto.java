package com.expenseflow.budget.dto;

import java.math.BigDecimal;

public record BudgetProgressDto(
    BudgetDto budget,
    BigDecimal currentSpent,
    BigDecimal remaining,
    double utilizationPercentage
) {}
