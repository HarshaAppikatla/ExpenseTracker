package com.expenseflow.settlement.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Append-only history log for a Settlement's status transitions.
 *
 * Records are created by SettlementEntity.transitionTo() and are never
 * updated or deleted. This preserves a complete audit trail.
 *
 * Owned by the Settlement aggregate root — no standalone repository exists
 * for this entity (ADR-005 §14 Repository Ownership Rules).
 */
@Entity
@Table(name = "settlement_history")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementHistoryEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false)
    private SettlementEntity settlement;

    /** The status before this transition (null for the initial PENDING creation) */
    @Column(name = "from_status", length = 20)
    private String fromStatus;

    @Column(name = "to_status", length = 20, nullable = false)
    private String toStatus;

    /** User ID of the actor who triggered this transition */
    @Column(name = "changed_by", length = 36, nullable = false)
    private String changedBy;

    /** Optional note — used for dispute reasons, resolution notes, etc. */
    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
