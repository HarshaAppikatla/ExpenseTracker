package com.expenseflow.group.dto;

import jakarta.validation.constraints.NotBlank;

public record TransferOwnershipRequest(
    @NotBlank(message = "New owner ID is required")
    String newOwnerId
) {}
