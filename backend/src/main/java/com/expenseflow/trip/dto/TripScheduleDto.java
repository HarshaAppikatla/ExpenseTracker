package com.expenseflow.trip.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record TripScheduleDto(
    @NotNull(message = "Start date is required") LocalDate startDate,
    @NotNull(message = "End date is required") LocalDate endDate
) {}
