package com.expenseflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_tokens")
@SQLDelete(sql = "UPDATE verification_tokens SET is_deleted = 1, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Filter(name = "deletedFilter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationTokenEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "token", length = 64, nullable = false, unique = true)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
    }
}
