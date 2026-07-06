package com.expenseflow.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
    @NotBlank(message = "Category name is required")
    @Size(min = 1, max = 50, message = "Category name must not exceed 50 characters")
    String name,

    @NotBlank(message = "Icon name is required")
    @Size(max = 50, message = "Icon name must not exceed 50 characters")
    String icon,

    @NotBlank(message = "Color HEX code is required")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Invalid Hex color format")
    String color
) {}
