package com.expenseflow.group.dto;

import java.time.LocalDateTime;

public record GroupMemberDto(
    String userId,
    String name,
    String email,
    String role,
    LocalDateTime joinedAt
) {}
