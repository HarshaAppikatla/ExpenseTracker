package com.expenseflow.savings.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SavingsDepositDto(
    String id,
    String goalId,
    BigDecimal amount,
    LocalDateTime depositDate,
    String notes
) {}
