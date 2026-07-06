package com.expenseflow.expense.dto;

public record ExpenseParticipantDto(
    String id,
    String userId,
    String userName,
    String userEmail
) {}
