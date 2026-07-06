package com.expenseflow.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TransactionDto(
    String id,
    String type, // "EXPENSE" or "INCOME"
    BigDecimal amount,
    String currencyCode,
    LocalDateTime date,
    String categoryName,
    String categoryColor,
    String sourceOrMerchant,
    String description,
    List<String> tags
) {}
