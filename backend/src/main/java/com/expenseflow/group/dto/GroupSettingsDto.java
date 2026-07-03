package com.expenseflow.group.dto;

public record GroupSettingsDto(
    boolean allowJoinByCode,
    boolean allowJoinByLink,
    boolean archived
) {}
