package com.expenseflow.trip.dto;

public record TripSettingsDto(
    String visibility,
    Boolean allowInvites,
    Boolean archived
) {}
