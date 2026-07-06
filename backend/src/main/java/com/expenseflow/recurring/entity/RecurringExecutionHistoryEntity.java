package com.expenseflow.recurring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recurring_execution_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringExecutionHistoryEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_transaction_id", nullable = false)
    private RecurringTransactionEntity recurringTransaction;

    @Column(name = "generated_transaction_id", nullable = false, length = 36)
    private String generatedTransactionId;

    @Column(name = "execution_date", nullable = false)
    private LocalDateTime executionDate;

    @Column(name = "execution_status", nullable = false, length = 20)
    private String executionStatus;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
