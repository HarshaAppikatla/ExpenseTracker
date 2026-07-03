package com.expenseflow.group.dto;

import jakarta.validation.constraints.Size;

public record UpdateGroupRequest(
    @Size(max = 100, message = "Group name cannot exceed 100 characters")
    String name,
    
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    String description,
    
    GroupSettingsDto settings
) {}
