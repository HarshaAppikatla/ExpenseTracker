-- V11: Notification Engine Schema
-- Depends on: V2 (users)

-- ─────────────────────────────────────────────────────────────────────────────
-- Table: notifications
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE notifications (
    id                VARCHAR(36)    NOT NULL PRIMARY KEY,
    user_id           VARCHAR(36)    NOT NULL,
    title             VARCHAR(255)   NOT NULL,
    message           VARCHAR(500)   NOT NULL,
    status            VARCHAR(20)    NOT NULL DEFAULT 'UNREAD',
    priority          VARCHAR(20)    NOT NULL DEFAULT 'NORMAL',
    category          VARCHAR(20)    NOT NULL DEFAULT 'SYSTEM',
    payload           TEXT           NULL,
    is_deleted        TINYINT(1)     NOT NULL DEFAULT 0,
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by        VARCHAR(100)   NULL,
    updated_by        VARCHAR(100)   NULL,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notifications_user_status_created ON notifications(user_id, status, created_at DESC);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
