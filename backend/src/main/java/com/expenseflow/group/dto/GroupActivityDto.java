package com.expenseflow.group.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record GroupActivityDto(
    String id,
    String actionType,
    Map<String, Object> metadata,
    LocalDateTime createdAt,
    String createdBy
) {}
