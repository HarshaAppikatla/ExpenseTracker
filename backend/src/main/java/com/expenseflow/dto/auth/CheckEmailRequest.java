package com.expenseflow.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckEmailRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
