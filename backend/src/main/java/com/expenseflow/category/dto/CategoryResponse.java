package com.expenseflow.category.dto;

public record CategoryResponse(
    String id,
    String name,
    String icon,
    String color,
    boolean systemCategory
) {}
