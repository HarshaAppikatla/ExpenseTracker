package com.expenseflow.recurring.repository;

import com.expenseflow.recurring.entity.RecurringExecutionHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RecurringExecutionHistoryRepository extends JpaRepository<RecurringExecutionHistoryEntity, String> {
    Optional<RecurringExecutionHistoryEntity> findByRecurringTransactionIdAndExecutionDate(
            String recurringTransactionId, LocalDateTime executionDate);
}
