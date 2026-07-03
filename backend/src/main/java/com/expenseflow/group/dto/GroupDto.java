package com.expenseflow.group.dto;

import java.time.LocalDateTime;

public record GroupDto(
    String id,
    String name,
    String description,
    String currency,
    String groupCode,
    boolean isOwner,
    String role,
    GroupSettingsDto settings,
    LocalDateTime createdAt
) {}
