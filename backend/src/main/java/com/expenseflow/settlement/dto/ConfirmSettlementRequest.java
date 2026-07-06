package com.expenseflow.settlement.dto;

import jakarta.validation.constraints.Size;

/**
 * Request body for confirming a settlement payment.
 */
public record ConfirmSettlementRequest(
        @Size(max = 255, message = "Note cannot exceed 255 characters")
        String note
) {}
