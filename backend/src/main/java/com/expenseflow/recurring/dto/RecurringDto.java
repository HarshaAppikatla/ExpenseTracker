package com.expenseflow.recurring.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecurringDto(
    String id,
    String userId,
    String transactionType,
    String categoryId,
    String categoryName,
    BigDecimal amount,
    String currencyCode,
    String merchant,
    String description,
    String recurrenceType,
    int recurrenceInterval,
    LocalDateTime startDate,
    LocalDateTime nextExecution,
    LocalDateTime endDate,
    String status
) {}
