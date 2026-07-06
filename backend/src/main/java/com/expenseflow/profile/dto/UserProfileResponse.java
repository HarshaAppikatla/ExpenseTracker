package com.expenseflow.profile.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserProfileResponse(
    String id,
    String userId,
    String preferredCurrency,
    BigDecimal openingBalance,
    boolean onboardingCompleted,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
