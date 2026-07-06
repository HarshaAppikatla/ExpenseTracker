package com.expenseflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET is_deleted = 1, deleted_at = CURRENT_TIMESTAMP WHERE id = ? AND version = ?")
@Filter(name = "deletedFilter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "login_provider", length = 50, nullable = false)
    @Builder.Default
    private String loginProvider = "LOCAL";

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "last_login_user_agent", length = 255)
    private String lastLoginUserAgent;

    @Column(name = "last_password_changed_at")
    private LocalDateTime lastPasswordChangedAt;

    /**
     * Stamped every time the user explicitly logs out.
     * Any JWT whose issuedAt is before this timestamp is treated as expired,
     * effectively invalidating all access tokens issued before the last logout.
     */
    @Column(name = "last_logout_at")
    private LocalDateTime lastLogoutAt;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Column(name = "next_allowed_login_at")
    private LocalDateTime nextAllowedLoginAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<RoleEntity> roles = new HashSet<>();

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
    }
}
