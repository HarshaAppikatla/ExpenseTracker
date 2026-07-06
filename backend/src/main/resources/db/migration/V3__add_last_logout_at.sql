-- V3: Add last_logout_at column to users table
-- This column is stamped every time a user explicitly logs out.
-- The JwtAuthenticationFilter compares a JWT's issuedAt against this timestamp
-- to immediately invalidate any access token issued before the last logout,
-- closing the post-logout stolen-token window without a Redis blacklist.

ALTER TABLE users
    ADD COLUMN last_logout_at DATETIME NULL DEFAULT NULL
        COMMENT 'Timestamp of the user''s most recent explicit logout. JWTs issued before this time are rejected.';
