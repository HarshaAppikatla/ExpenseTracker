package com.expenseflow.settlement.repository;

import com.expenseflow.settlement.domain.entity.SettlementHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for SettlementHistoryEntity.
 * Resides in the repository layer to facilitate auditing and historical logging.
 */
@Repository
public interface SettlementHistoryRepository extends JpaRepository<SettlementHistoryEntity, String> {

    /**
     * Find all history logs for a specific settlement, sorted by creation date descending.
     */
    List<SettlementHistoryEntity> findBySettlementIdOrderByCreatedAtDesc(String settlementId);
}
