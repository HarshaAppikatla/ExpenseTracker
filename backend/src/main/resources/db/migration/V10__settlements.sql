-- V10: Settlement Engine Schema
-- Depends on: V8 (expenses), V6 (groups), V7 (trips), V2 (users)

-- ─────────────────────────────────────────────────────────────────────────────
-- Table: settlements
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE settlements (
    id                VARCHAR(36)    NOT NULL PRIMARY KEY,
    group_id          VARCHAR(36)    NOT NULL,
    trip_id           VARCHAR(36)    NULL,
    from_user_id      VARCHAR(36)    NOT NULL,
    to_user_id        VARCHAR(36)    NOT NULL,
    amount            DECIMAL(19, 2) NOT NULL,
    currency          VARCHAR(3)     NOT NULL,
    status            VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    settled_at        TIMESTAMP      NULL,
    version           BIGINT         NOT NULL DEFAULT 0,
    is_deleted        TINYINT(1)     NOT NULL DEFAULT 0,
    deleted_at        TIMESTAMP      NULL,
    deleted_by        VARCHAR(36)    NULL,
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by        VARCHAR(100)   NULL,
    updated_by        VARCHAR(100)   NULL,
    CONSTRAINT fk_settlements_group    FOREIGN KEY (group_id)     REFERENCES `groups`(id),
    CONSTRAINT fk_settlements_trip     FOREIGN KEY (trip_id)      REFERENCES trips(id),
    -- Note: from_user_id and to_user_id are NOT FK-constrained to allow soft-deleted users
    CONSTRAINT uq_settlement_pair_group UNIQUE (group_id, trip_id, from_user_id, to_user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_settlements_group      ON settlements(group_id);
CREATE INDEX idx_settlements_trip       ON settlements(trip_id);
CREATE INDEX idx_settlements_from_to    ON settlements(from_user_id, to_user_id);
CREATE INDEX idx_settlements_status     ON settlements(status);
CREATE INDEX idx_settlements_group_status ON settlements(group_id, status);

-- ─────────────────────────────────────────────────────────────────────────────
-- Table: settlement_history
-- Append-only log of all status transitions. Never updated, never deleted.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE settlement_history (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    settlement_id   VARCHAR(36)  NOT NULL,
    from_status     VARCHAR(20)  NULL,
    to_status       VARCHAR(20)  NOT NULL,
    changed_by      VARCHAR(36)  NOT NULL,
    note            VARCHAR(500) NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_settlement_history_settlement FOREIGN KEY (settlement_id) REFERENCES settlements(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_settlement_history_settlement ON settlement_history(settlement_id);
CREATE INDEX idx_settlement_history_date       ON settlement_history(created_at);
