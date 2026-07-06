-- Flyway Migration V7: Trip Management Collaborative Travel Subsystem

-- 1. Create Trips Table
CREATE TABLE trips (
    id VARCHAR(36) NOT NULL,
    group_id VARCHAR(36) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL,
    dest_city VARCHAR(100) NOT NULL,
    dest_country VARCHAR(100) NOT NULL,
    dest_display_name VARCHAR(255) NOT NULL,
    cover_type VARCHAR(20) NOT NULL DEFAULT 'PRESET',
    cover_image VARCHAR(255) NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    organizer_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNING',
    visibility VARCHAR(20) NOT NULL DEFAULT 'GROUP',
    allow_invites TINYINT(1) NOT NULL DEFAULT 1,
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
    CONSTRAINT fk_trips_group FOREIGN KEY (group_id) REFERENCES `groups` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Create Trip Participants Table
CREATE TABLE trip_participants (
    id VARCHAR(36) NOT NULL,
    trip_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'JOINED',
    joined_at TIMESTAMP NULL,
    left_at TIMESTAMP NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    updated_by VARCHAR(100) NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    is_deleted TINYINT(1) DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_trip_participants_trip FOREIGN KEY (trip_id) REFERENCES trips (id),
    CONSTRAINT uq_trip_participants_trip_user UNIQUE (trip_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Create Trip Activities Table (Append-Only)
CREATE TABLE trip_activities (
    id VARCHAR(36) NOT NULL,
    trip_id VARCHAR(36) NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    actor_user_id VARCHAR(36) NOT NULL,
    target_user_id VARCHAR(36) NULL,
    message VARCHAR(255) NOT NULL,
    metadata_json JSON NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_trip_activities_trip FOREIGN KEY (trip_id) REFERENCES trips (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Create Indexes
CREATE INDEX idx_trips_group_id ON trips (group_id);
CREATE INDEX idx_trips_status ON trips (status);
CREATE INDEX idx_trips_start_date ON trips (start_date);
CREATE INDEX idx_trips_organizer_id ON trips (organizer_id);
CREATE INDEX idx_trip_participants_trip_user ON trip_participants (trip_id, user_id);
CREATE INDEX idx_trip_participants_user_id ON trip_participants (user_id);
CREATE INDEX idx_trip_activities_trip_date ON trip_activities (trip_id, occurred_at);
