-- Flyway Migration V4: Expense Management & Financial Subsystem (Hardened & Enterprise)

-- 1. Create User Profiles Table
CREATE TABLE user_profiles (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    preferred_currency VARCHAR(10) NOT NULL,
    opening_balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    onboarding_completed TINYINT(1) NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    updated_by VARCHAR(100) NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    is_deleted TINYINT(1) DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Create Categories Table
CREATE TABLE categories (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NULL, -- NULL indicates system-defined category
    name VARCHAR(50) NOT NULL,
    icon VARCHAR(50) NOT NULL, -- Material Icons string representation
    color VARCHAR(20) NOT NULL,
    system_category TINYINT(1) NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    updated_by VARCHAR(100) NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    is_deleted TINYINT(1) DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    UNIQUE KEY uq_categories_user_name (user_id, name, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed Default System Categories
INSERT INTO categories (id, user_id, name, icon, color, system_category, created_by) VALUES
('cat-food-00000000000000000000000001', NULL, 'Food', 'restaurant', '#FF5733', 1, 'SYSTEM'),
('cat-shop-00000000000000000000000002', NULL, 'Shopping', 'shopping_bag', '#E0115F', 1, 'SYSTEM'),
('cat-tran-00000000000000000000000003', NULL, 'Transport', 'directions_car', '#1F75FE', 1, 'SYSTEM'),
('cat-bill-00000000000000000000000004', NULL, 'Bills', 'receipt_long', '#F4D03F', 1, 'SYSTEM'),
('cat-heal-00000000000000000000000005', NULL, 'Health', 'medical_services', '#2ECC71', 1, 'SYSTEM'),
('cat-educ-00000000000000000000000006', NULL, 'Education', 'school', '#9B59B6', 1, 'SYSTEM'),
('cat-entm-00000000000000000000000007', NULL, 'Entertainment', 'sports_esports', '#E67E22', 1, 'SYSTEM'),
('cat-trav-00000000000000000000000008', NULL, 'Travel', 'flight', '#1ABC9C', 1, 'SYSTEM'),
('cat-salr-00000000000000000000000009', NULL, 'Salary', 'payments', '#27AE60', 1, 'SYSTEM'),
('cat-othr-00000000000000000000000010', NULL, 'Other', 'more_horiz', '#7F8C8D', 1, 'SYSTEM');

-- 3. Create Expenses Table
CREATE TABLE expenses (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    group_id VARCHAR(36) NULL, -- Forward compatible for Sprint 04 Group features
    category_id VARCHAR(36) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency_code VARCHAR(10) NOT NULL DEFAULT 'USD',
    expense_date TIMESTAMP NOT NULL,
    merchant VARCHAR(100) NULL,
    merchant_normalized VARCHAR(100) NULL,
    merchant_hash VARCHAR(64) NULL,
    description VARCHAR(255) NULL,
    notes TEXT NULL,
    latitude DOUBLE NULL,
    longitude DOUBLE NULL,
    location_name VARCHAR(100) NULL,
    address VARCHAR(255) NULL,
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
    CONSTRAINT fk_expenses_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_expenses_category FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Create Expense Tags Table (Normalized Many-to-Many Tag Mapping)
CREATE TABLE expense_tags (
    expense_id VARCHAR(36) NOT NULL,
    tag VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (expense_id, tag),
    CONSTRAINT fk_expense_tags_expense FOREIGN KEY (expense_id) REFERENCES expenses (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Create Income Table
CREATE TABLE income (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency_code VARCHAR(10) NOT NULL DEFAULT 'USD',
    source VARCHAR(100) NOT NULL,
    income_date TIMESTAMP NOT NULL,
    description VARCHAR(255) NULL,
    notes TEXT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    updated_by VARCHAR(100) NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    is_deleted TINYINT(1) DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_income_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Create Receipts Table
CREATE TABLE receipts (
    id VARCHAR(36) NOT NULL,
    expense_id VARCHAR(36) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_receipts_expense FOREIGN KEY (expense_id) REFERENCES expenses (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Add Performance & Search Indexes
CREATE INDEX idx_expenses_user_date ON expenses (user_id, expense_date);
CREATE INDEX idx_expenses_user_category ON expenses (user_id, category_id);
CREATE INDEX idx_expenses_user_merchant ON expenses (user_id, merchant_normalized);
CREATE INDEX idx_expenses_group ON expenses (group_id);
CREATE INDEX idx_income_user_date ON income (user_id, income_date);
CREATE INDEX idx_categories_user_name ON categories (user_id, name);
