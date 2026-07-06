package com.expenseflow.dto.user;

import com.expenseflow.entity.UserStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private String id;
    private String fullName;
    private String email;
    private UserStatus status;
    private String avatarUrl;
    private String loginProvider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<String> roles;
}
