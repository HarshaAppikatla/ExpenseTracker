package com.expenseflow.income.entity;

import com.expenseflow.entity.BaseEntity;
import com.expenseflow.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "income")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Filter(name = "deletedFilter")
public class IncomeEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency_code", length = 10, nullable = false)
    private String currencyCode;

    @Column(name = "source", length = 100, nullable = false)
    private String source;

    @Column(name = "income_date", nullable = false)
    private LocalDateTime incomeDate;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;
}
