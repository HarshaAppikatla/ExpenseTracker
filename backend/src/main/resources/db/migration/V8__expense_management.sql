-- V8: Shared Expense Management Schema

-- Drop legacy expense-related tables to avoid conflicts
DROP TABLE IF EXISTS expense_tags;
DROP TABLE IF EXISTS receipts;
DROP TABLE IF EXISTS expenses;

CREATE TABLE expenses (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    group_id VARCHAR(36) NOT NULL,
    trip_id VARCHAR(36) NULL,
    description VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    category_type VARCHAR(20) NOT NULL DEFAULT 'SYSTEM',
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    paid_by_user_id VARCHAR(36) NOT NULL,
    created_by_user_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    expense_date DATE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(36) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    updated_by VARCHAR(100) NULL,
    CONSTRAINT fk_expenses_group FOREIGN KEY (group_id) REFERENCES `groups`(id),
    CONSTRAINT fk_expenses_trip FOREIGN KEY (trip_id) REFERENCES trips(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_expenses_group ON expenses(group_id);
CREATE INDEX idx_expenses_trip ON expenses(trip_id);
CREATE INDEX idx_expenses_date ON expenses(expense_date);

CREATE TABLE expense_participants (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    expense_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(36) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    updated_by VARCHAR(100) NULL,
    CONSTRAINT fk_expense_participants_expense FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE,
    CONSTRAINT uq_expense_participants_exp_user UNIQUE (expense_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_expense_participants_exp ON expense_participants(expense_id);
CREATE INDEX idx_expense_participants_user ON expense_participants(user_id);

CREATE TABLE expense_splits (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    expense_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    owed_amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    allocation_value DECIMAL(19, 4) NULL,
    version BIGINT NOT NULL DEFAULT 0,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(36) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    updated_by VARCHAR(100) NULL,
    CONSTRAINT fk_expense_splits_expense FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE,
    CONSTRAINT uq_expense_splits_exp_user UNIQUE (expense_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_expense_splits_exp ON expense_splits(expense_id);
CREATE INDEX idx_expense_splits_user ON expense_splits(user_id);

CREATE TABLE expense_attachments (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    expense_id VARCHAR(36) NOT NULL,
    url VARCHAR(255) NOT NULL,
    file_name VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    storage_provider VARCHAR(50) NOT NULL DEFAULT 'LOCAL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    CONSTRAINT fk_expense_attachments_expense FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_expense_attachments_exp ON expense_attachments(expense_id);
