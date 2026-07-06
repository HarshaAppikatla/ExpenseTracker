package com.expenseflow.expense.dto;

import com.expenseflow.expense.domain.valueobject.ExpenseCategory;
import com.expenseflow.expense.domain.valueobject.SplitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record UpdateExpenseRequest(
    @NotBlank(message = "Description is required")
    @Size(max = 255)
    String description,

    @NotNull(message = "Category is required")
    ExpenseCategory category,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    BigDecimal amount,

    @NotBlank(message = "Currency is required")
    String currency,

    @NotBlank(message = "PaidByUserId is required")
    String paidByUserId,

    @NotNull(message = "ExpenseDate is required")
    LocalDate expenseDate,

    @NotNull(message = "SplitType is required")
    SplitType splitType,

    @NotEmpty(message = "Allocation values cannot be empty")
    Map<String, BigDecimal> allocationValues
) {}
