package com.expenseflow.settlement.repository;

import com.expenseflow.settlement.domain.entity.SettlementEntity;
import com.expenseflow.settlement.domain.valueobject.SettlementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the Settlement aggregate root.
 *
 * One repository per aggregate root (ADR-005 §14 Repository Ownership Rules).
 * SettlementHistoryEntity has no standalone repository — it is accessed only
 * through the SettlementEntity cascade.
 */
@Repository
public interface SettlementRepository extends JpaRepository<SettlementEntity, String> {

    Optional<SettlementEntity> findByIdAndIsDeletedFalse(String id);

    /** All non-deleted settlements for a group (all statuses). */
    Page<SettlementEntity> findByGroupIdAndIsDeletedFalse(String groupId, Pageable pageable);

    /** All non-deleted settlements for a specific trip. */
    Page<SettlementEntity> findByGroupIdAndTripIdAndIsDeletedFalse(String groupId, String tripId, Pageable pageable);

    /** All settlements where the current user is debtor or creditor. */
    @Query("SELECT s FROM SettlementEntity s WHERE s.isDeleted = false " +
           "AND s.groupId = :groupId " +
           "AND (s.fromUserId = :userId OR s.toUserId = :userId)")
    List<SettlementEntity> findByGroupIdAndUserId(
            @Param("groupId") String groupId,
            @Param("userId") String userId);

    /**
     * Idempotency check — finds an existing PENDING settlement between the
     * same debtor and creditor pair in a given group scope.
     * Used by SettlementCommandService to upsert rather than duplicate.
     */
    @Query("SELECT s FROM SettlementEntity s WHERE s.isDeleted = false " +
           "AND s.groupId = :groupId " +
           "AND (:tripId IS NULL AND s.tripId IS NULL OR s.tripId = :tripId) " +
           "AND s.fromUserId = :fromUserId " +
           "AND s.toUserId = :toUserId " +
           "AND s.status = :status")
    Optional<SettlementEntity> findPendingByPair(
            @Param("groupId") String groupId,
            @Param("tripId") String tripId,
            @Param("fromUserId") String fromUserId,
            @Param("toUserId") String toUserId,
            @Param("status") SettlementStatus status);

    /** All PENDING settlements for a group (used by summary views). */
    List<SettlementEntity> findByGroupIdAndStatusAndIsDeletedFalse(String groupId, SettlementStatus status);
}
