package com.expenseflow.expense.dto;

import com.expenseflow.expense.domain.valueobject.ExpenseStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateExpenseStatusRequest(
    @NotNull(message = "Status is required")
    ExpenseStatus status
) {}
