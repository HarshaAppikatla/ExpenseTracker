package com.expenseflow.budget.dto;

import java.math.BigDecimal;

public record BudgetRequest(
    String categoryId,
    int year,
    int month,
    BigDecimal monthlyLimit,
    String currencyCode,
    Integer alertPercentage
) {}
