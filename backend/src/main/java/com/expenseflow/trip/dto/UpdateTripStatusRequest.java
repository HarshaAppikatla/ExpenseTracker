package com.expenseflow.trip.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateTripStatusRequest(
    @NotBlank(message = "Status is required") String status
) {}
