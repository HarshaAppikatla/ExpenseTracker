package com.expenseflow.trip.dto;

import jakarta.validation.constraints.NotBlank;

public record DestinationDto(
    @NotBlank(message = "City is required") String city,
    @NotBlank(message = "Country is required") String country,
    @NotBlank(message = "Display name is required") String displayName
) {}
