package com.expenseflow.dashboard.dto;

import java.math.BigDecimal;

public record CategoryBreakdownDto(
    String name,
    String color,
    BigDecimal amount
) {}
