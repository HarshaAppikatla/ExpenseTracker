package com.expenseflow.recurring.dto;

import java.time.LocalDateTime;

public record RecurringExecutionDto(
    String id,
    String recurringTransactionId,
    String generatedTransactionId,
    LocalDateTime executionDate,
    String executionStatus,
    String errorMessage,
    LocalDateTime createdAt
) {}
