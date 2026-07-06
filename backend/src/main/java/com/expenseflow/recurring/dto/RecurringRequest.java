package com.expenseflow.recurring.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecurringRequest(
    String transactionType,
    String categoryId,
    BigDecimal amount,
    String currencyCode,
    String merchant,
    String description,
    String recurrenceType,
    int recurrenceInterval,
    LocalDateTime startDate,
    LocalDateTime endDate
) {}
