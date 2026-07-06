package com.expenseflow.savings.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SavingsGoalDto(
    String id,
    String userId,
    String title,
    String description,
    BigDecimal targetAmount,
    LocalDateTime targetDate,
    BigDecimal currentAmount,
    double progressPercentage,
    boolean completed,
    LocalDateTime completedAt
) {}
