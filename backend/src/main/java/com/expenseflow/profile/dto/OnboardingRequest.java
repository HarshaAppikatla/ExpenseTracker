package com.expenseflow.profile.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record OnboardingRequest(
    @NotBlank(message = "Preferred currency is required")
    @Size(min = 3, max = 10, message = "Preferred currency code must be between 3 and 10 characters")
    String preferredCurrency,

    @NotNull(message = "Opening balance cannot be null")
    @DecimalMin(value = "0.00", message = "Opening balance cannot be negative")
    BigDecimal openingBalance,

    @DecimalMin(value = "0.00", message = "Initial monthly income cannot be negative")
    BigDecimal initialMonthlyIncome
) {}
