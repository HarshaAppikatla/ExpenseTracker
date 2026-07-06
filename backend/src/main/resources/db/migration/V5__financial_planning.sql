-- Flyway Migration V5: Financial Planning Subsystem (Hardened & Enterprise)

-- 1. ShedLock distributed lock metadata table
CREATE TABLE shedlock (
    name VARCHAR(64) NOT NULL,
    lock_until TIMESTAMP(3) NOT NULL,
    locked_at TIMESTAMP(3) NOT NULL,
    locked_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Budgets Table
CREATE TABLE budgets (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    category_id VARCHAR(36) NOT NULL,
    budget_year INT NOT NULL,
    budget_month INT NOT NULL,
    monthly_limit DECIMAL(19,2) NOT NULL,
    currency_code VARCHAR(10) NOT NULL,
    alert_percentage INT DEFAULT 80,
    active TINYINT(1) DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    updated_by VARCHAR(100) NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    is_deleted TINYINT(1) DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_budgets_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_budgets_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE,
    UNIQUE KEY uq_budgets_user_category_month (user_id, category_id, budget_year, budget_month, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Recurring Transaction Templates Table
CREATE TABLE recurring_transactions (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    transaction_type VARCHAR(10) NOT NULL,
    category_id VARCHAR(36) NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency_code VARCHAR(10) NOT NULL,
    merchant VARCHAR(100) NULL,
    description VARCHAR(255) NULL,
    recurrence_type VARCHAR(20) NOT NULL,
    recurrence_interval INT NOT NULL,
    start_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    next_execution TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_date TIMESTAMP NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    updated_by VARCHAR(100) NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    is_deleted TINYINT(1) DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_recurring_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_recurring_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Recurring Execution History Logs Table
CREATE TABLE recurring_execution_history (
    id VARCHAR(36) NOT NULL,
    recurring_transaction_id VARCHAR(36) NOT NULL,
    generated_transaction_id VARCHAR(36) NOT NULL,
    execution_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_status VARCHAR(20) NOT NULL,
    error_message VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_history_recurring FOREIGN KEY (recurring_transaction_id) REFERENCES recurring_transactions (id) ON DELETE CASCADE,
    UNIQUE KEY uq_recurring_execution (recurring_transaction_id, execution_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Savings Goals Table
CREATE TABLE savings_goals (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL,
    target_amount DECIMAL(19,2) NOT NULL,
    target_date TIMESTAMP NULL,
    completed TINYINT(1) DEFAULT 0,
    completed_at TIMESTAMP NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    updated_by VARCHAR(100) NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    is_deleted TINYINT(1) DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_savings_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Savings Deposits Table (Immutable)
CREATE TABLE savings_deposits (
    id VARCHAR(36) NOT NULL,
    goal_id VARCHAR(36) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    deposit_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_deposits_goal FOREIGN KEY (goal_id) REFERENCES savings_goals (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Notifications Table
CREATE TABLE notifications (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    message VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'UNREAD',
    priority VARCHAR(20) DEFAULT 'NORMAL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Indexes for performance
CREATE INDEX idx_budgets_user_date ON budgets(user_id, budget_year, budget_month);
CREATE INDEX idx_recurring_exec ON recurring_transactions(user_id, next_execution, status, is_deleted);
CREATE INDEX idx_savings_user ON savings_goals(user_id, is_deleted);
CREATE INDEX idx_notifications_user_status ON notifications(user_id, status);
