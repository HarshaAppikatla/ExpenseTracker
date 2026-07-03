package com.expenseflow.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinGroupRequest(
    @NotBlank(message = "Room code is required")
    @Size(max = 20, message = "Room code must not exceed 20 characters")
    String roomCode
) {}
