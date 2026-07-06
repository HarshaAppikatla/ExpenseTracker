package com.expenseflow.trip.dto;

import java.time.LocalDateTime;

public record TripParticipantDto(
    String id,
    String userId,
    String userName,
    String userEmail,
    String status,
    LocalDateTime joinedAt,
    LocalDateTime leftAt
) {}
