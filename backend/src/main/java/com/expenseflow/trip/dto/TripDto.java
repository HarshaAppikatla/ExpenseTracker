package com.expenseflow.trip.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TripDto(
    String id,
    String groupId,
    String title,
    String description,
    DestinationDto destination,
    String coverType,
    String coverImage,
    TripScheduleDto schedule,
    String organizerId,
    String organizerName,
    String status,
    TripSettingsDto settings,
    Long version,
    List<TripParticipantDto> participants,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
