package com.expenseflow.trip.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTripRequest(
    @NotBlank(message = "GroupId is required") String groupId,
    @NotBlank(message = "Title is required") String title,
    String description,
    @NotNull(message = "Destination is required") @Valid DestinationDto destination,
    @NotNull(message = "Schedule is required") @Valid TripScheduleDto schedule,
    @Valid TripSettingsDto settings,
    String coverType,
    String coverImage
) {}
