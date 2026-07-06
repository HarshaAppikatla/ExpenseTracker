package com.expenseflow.income.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IncomeResponse(
    String id,
    String userId,
    BigDecimal amount,
    String currencyCode,
    String source,
    LocalDateTime incomeDate,
    String description,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
