package com.expenseflow.expense.dto;

import java.math.BigDecimal;

public record ExpenseSplitDto(
    String id,
    String userId,
    String userName,
    String userEmail,
    BigDecimal owedAmount,
    BigDecimal allocationValue
) {}
