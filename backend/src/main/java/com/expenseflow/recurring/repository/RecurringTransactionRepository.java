package com.expenseflow.recurring.repository;

import com.expenseflow.recurring.entity.RecurringTransactionEntity;
import com.expenseflow.recurring.entity.RecurringStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransactionEntity, String>, JpaSpecificationExecutor<RecurringTransactionEntity> {
    List<RecurringTransactionEntity> findByUserIdAndIsDeletedFalse(String userId);

    List<RecurringTransactionEntity> findByStatusAndNextExecutionBeforeAndIsDeletedFalse(
            RecurringStatus status, LocalDateTime dateTime);
}
