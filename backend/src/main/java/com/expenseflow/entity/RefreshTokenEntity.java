package com.expenseflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@SQLDelete(sql = "UPDATE refresh_tokens SET is_deleted = 1, deleted_at = CURRENT_TIMESTAMP WHERE id = ? AND version = ?")
@Filter(name = "deletedFilter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "token", length = 64, nullable = false, unique = true)
    private String token;

    @Transient
    private String plainToken;

    @Column(name = "token_family", length = 36, nullable = false)
    private String tokenFamily;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "browser", length = 50)
    private String browser;

    @Column(name = "os", length = 50)
    private String os;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
    }
}
