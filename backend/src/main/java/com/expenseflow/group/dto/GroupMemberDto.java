package com.expenseflow.group.dto;

import java.time.LocalDateTime;

public record GroupMemberDto(
    String id,
    String userId,
    String userName,
    String userEmail,
    String role,
    LocalDateTime joinedAt
) {}
