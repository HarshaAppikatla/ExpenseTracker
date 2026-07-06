package com.expenseflow.income.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IncomeRequest(
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    BigDecimal amount,

    @Size(max = 10, message = "Currency code must not exceed 10 characters")
    String currencyCode,

    @NotBlank(message = "Source is required")
    @Size(max = 100, message = "Source must not exceed 100 characters")
    String source,

    @NotNull(message = "Income date is required")
    LocalDateTime incomeDate,

    @Size(max = 255, message = "Description must not exceed 255 characters")
    String description,

    String notes
) {}
