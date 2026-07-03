package com.expenseflow.profile.entity;

import com.expenseflow.entity.BaseEntity;
import com.expenseflow.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Filter(name = "deletedFilter")
public class UserProfileEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserEntity user;

    @Column(name = "preferred_currency", length = 10, nullable = false)
    private String preferredCurrency;

    @Column(name = "opening_balance", precision = 19, scale = 2, nullable = false)
    private BigDecimal openingBalance;

    @Builder.Default
    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted = false;

    @Builder.Default
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;
}
