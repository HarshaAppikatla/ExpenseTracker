package com.expenseflow.dto.auth;

import com.expenseflow.dto.user.UserDto;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn; // Milliseconds
    private UserDto user;
    private Set<String> roles;
    private Set<String> permissions;
}
