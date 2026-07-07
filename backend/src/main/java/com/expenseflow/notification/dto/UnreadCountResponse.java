package com.expenseflow.notification.dto;

/**
 * Lightweight summary returned by the notification bell count endpoint.
 */
public record UnreadCountResponse(long unreadCount) {}
