package com.expenseflow.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGroupRequest(
    @NotBlank(message = "Group name cannot be blank")
    @Size(max = 100, message = "Group name cannot exceed 100 characters")
    String name,
    
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    String description,
    
    @NotBlank(message = "Currency cannot be blank")
    @Size(max = 10, message = "Currency code must not exceed 10 characters")
    String currency
) {}
