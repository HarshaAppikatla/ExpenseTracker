package com.expenseflow.savings.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SavingsGoalRequest(
    String title,
    String description,
    BigDecimal targetAmount,
    LocalDateTime targetDate
) {}
