-- Flyway Migration V6: Group Management Collaboration Subsystem (Hardened & Enterprise)

-- 1. Create Groups Table
CREATE TABLE `groups` (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL,
    currency VARCHAR(10) NOT NULL,
    group_code VARCHAR(20) NOT NULL UNIQUE,
    owner_id VARCHAR(36) NOT NULL,
    allow_join_code TINYINT(1) NOT NULL DEFAULT 1,
    allow_join_link TINYINT(1) NOT NULL DEFAULT 1,
    archived TINYINT(1) NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    updated_by VARCHAR(100) NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    is_deleted TINYINT(1) DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_groups_owner FOREIGN KEY (owner_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Create Group Members Table
CREATE TABLE group_members (
    id VARCHAR(36) NOT NULL,
    group_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    updated_by VARCHAR(100) NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    is_deleted TINYINT(1) DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_group_members_group FOREIGN KEY (group_id) REFERENCES `groups` (id),
    CONSTRAINT fk_group_members_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_group_members_group_user UNIQUE (group_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Create Group Activity Table
CREATE TABLE group_activity (
    id VARCHAR(36) NOT NULL,
    group_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NULL,
    action_type VARCHAR(50) NOT NULL,
    metadata JSON NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_group_activity_group FOREIGN KEY (group_id) REFERENCES `groups` (id),
    CONSTRAINT fk_group_activity_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Create Indexes
CREATE INDEX idx_groups_owner ON `groups` (owner_id);
CREATE INDEX idx_groups_code ON `groups` (group_code);
CREATE INDEX idx_groups_is_deleted ON `groups` (is_deleted);
CREATE INDEX idx_groups_archived ON `groups` (archived);
CREATE INDEX idx_group_members_group_user ON group_members (group_id, user_id);
CREATE INDEX idx_group_members_group_role ON group_members (group_id, role);
CREATE INDEX idx_group_members_user ON group_members (user_id);
CREATE INDEX idx_group_activity_group_date ON group_activity (group_id, created_at);
CREATE INDEX idx_group_activity_created_at ON group_activity (created_at);
