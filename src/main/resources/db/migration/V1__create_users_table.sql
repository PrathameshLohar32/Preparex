-- V1: Create users table
CREATE TABLE IF NOT EXISTS users (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name              VARCHAR(100)    NOT NULL,
    username          VARCHAR(50)     NOT NULL UNIQUE,
    email             VARCHAR(255)    UNIQUE,
    phone             VARCHAR(20)     UNIQUE,
    password_hash     TEXT,
    is_email_verified BOOLEAN         NOT NULL DEFAULT FALSE,
    is_phone_verified BOOLEAN         NOT NULL DEFAULT FALSE,
    is_active         BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email    ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_phone    ON users (phone);
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);

COMMENT ON TABLE  users IS 'Core user accounts for the PreparEx platform';
COMMENT ON COLUMN users.password_hash IS 'Argon2id hash of the user password. NULL for OAuth-only users.';
