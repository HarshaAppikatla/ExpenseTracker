-- Flyway Migration V2: Authentication & User Management (Hardened & Enterprise)

CREATE TABLE roles (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    updated_by VARCHAR(100) NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    is_deleted TINYINT(1) DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE users (
    id VARCHAR(36) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    avatar_url VARCHAR(255) NULL,
    login_provider VARCHAR(50) NOT NULL DEFAULT 'LOCAL',
    last_login_at TIMESTAMP NULL,
    last_login_ip VARCHAR(45) NULL,
    last_login_user_agent VARCHAR(255) NULL,
    last_password_changed_at TIMESTAMP NULL,
    failed_login_attempts INT DEFAULT 0,
    account_locked_until TIMESTAMP NULL,
    next_allowed_login_at TIMESTAMP NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    updated_by VARCHAR(100) NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    is_deleted TINYINT(1) DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_roles (
    user_id VARCHAR(36) NOT NULL,
    role_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE refresh_tokens (
    id VARCHAR(36) NOT NULL,
    token VARCHAR(64) NOT NULL UNIQUE, -- SHA-256 Hash
    token_family VARCHAR(36) NOT NULL, -- UUID String
    expiry_date TIMESTAMP NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(255) NULL,
    device_name VARCHAR(100) NULL,
    browser VARCHAR(50) NULL,
    os VARCHAR(50) NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    updated_by VARCHAR(100) NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    is_deleted TINYINT(1) DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE verification_tokens (
    id VARCHAR(36) NOT NULL,
    token VARCHAR(64) NOT NULL UNIQUE, -- SHA-256 Hash
    expiry_date TIMESTAMP NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    updated_by VARCHAR(100) NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    is_deleted TINYINT(1) DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_verification_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE password_reset_tokens (
    id VARCHAR(36) NOT NULL,
    token VARCHAR(64) NOT NULL UNIQUE, -- SHA-256 Hash
    expiry_date TIMESTAMP NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    updated_by VARCHAR(100) NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    is_deleted TINYINT(1) DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE password_history (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    algorithm VARCHAR(50) NOT NULL DEFAULT 'BCRYPT',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_password_history_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE security_events (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NULL,
    event_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO',
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(255) NULL,
    request_id VARCHAR(50) NULL,
    correlation_id VARCHAR(50) NULL,
    trace_id VARCHAR(50) NULL,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_security_events_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed default static system roles
INSERT INTO roles (id, name, created_by, updated_by) VALUES 
('550e8400-e29b-41d4-a716-446655440000', 'ROLE_USER', 'SYSTEM', 'SYSTEM'),
('550e8400-e29b-41d4-a716-446655440001', 'ROLE_ADMIN', 'SYSTEM', 'SYSTEM'),
('550e8400-e29b-41d4-a716-446655440002', 'ROLE_SUPER_ADMIN', 'SYSTEM', 'SYSTEM');
