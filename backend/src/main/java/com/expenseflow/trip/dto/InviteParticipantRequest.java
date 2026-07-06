package com.expenseflow.trip.dto;

import jakarta.validation.constraints.NotBlank;

public record InviteParticipantRequest(
    @NotBlank(message = "UserId is required") String userId
) {}
