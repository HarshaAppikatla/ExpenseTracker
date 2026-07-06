package com.expenseflow.recurring.entity;

import com.expenseflow.category.entity.CategoryEntity;
import com.expenseflow.entity.BaseEntity;
import com.expenseflow.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recurring_transactions")
@SQLDelete(sql = "UPDATE recurring_transactions SET is_deleted = 1, deleted_at = CURRENT_TIMESTAMP WHERE id = ? AND version = ?")
@Where(clause = "is_deleted = 0")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTransactionEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "transaction_type", nullable = false, length = 10)
    private String transactionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 10)
    private String currencyCode;

    @Column(name = "merchant", length = 100)
    private String merchant;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "recurrence_type", nullable = false, length = 20)
    private String recurrenceType;

    @Column(name = "recurrence_interval", nullable = false)
    private int recurrenceInterval;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "next_execution", nullable = false)
    private LocalDateTime nextExecution;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecurringStatus status;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
