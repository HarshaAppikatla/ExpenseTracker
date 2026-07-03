package com.expenseflow.group.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateMemberRoleRequest(
    @NotBlank(message = "Role is required")
    String role
) {}
