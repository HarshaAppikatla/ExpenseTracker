package com.expenseflow.trip.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record TripActivityDto(
    String id,
    String tripId,
    String activityType,
    String actorUserId,
    String actorName,
    String targetUserId,
    String targetName,
    String message,
    Map<String, Object> metadataJson,
    LocalDateTime occurredAt
) {}
