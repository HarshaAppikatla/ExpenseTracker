package com.expenseflow.savings.dto;

import java.math.BigDecimal;

public record SavingsDepositRequest(
    BigDecimal amount,
    String notes
) {}
