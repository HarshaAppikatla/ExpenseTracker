package com.expenseflow.settlement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for disputing a settlement.
 */
public record DisputeSettlementRequest(
        @NotBlank(message = "Dispute reason cannot be blank")
        @Size(max = 255, message = "Dispute reason cannot exceed 255 characters")
        String reason
) {}
