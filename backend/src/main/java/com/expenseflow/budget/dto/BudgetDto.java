package com.expenseflow.budget.dto;

import java.math.BigDecimal;

public record BudgetDto(
    String id,
    String userId,
    String categoryId,
    String categoryName,
    int year,
    int month,
    BigDecimal monthlyLimit,
    String currencyCode,
    int alertPercentage,
    boolean active
) {}
