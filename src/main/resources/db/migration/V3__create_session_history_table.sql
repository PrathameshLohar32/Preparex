-- V3: Create session_history table (audit log, not active session store)
CREATE TABLE IF NOT EXISTS session_history (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_id    VARCHAR(255) NOT NULL,
    device_info   VARCHAR(255),
    ip_address    VARCHAR(64),
    user_agent    VARCHAR(500),
    logged_in_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    logged_out_at TIMESTAMP,
    logout_reason VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_session_history_user_id   ON session_history (user_id);
CREATE INDEX IF NOT EXISTS idx_session_history_session_id ON session_history (session_id);

COMMENT ON TABLE  session_history IS 'Persistent audit log of user sessions. Active session state is managed in Redis.';
COMMENT ON COLUMN session_history.logged_out_at  IS 'NULL if session expired via Redis TTL without explicit logout';
COMMENT ON COLUMN session_history.logout_reason  IS 'USER_LOGOUT, LOGOUT_ALL, SESSION_LIMIT_EXCEEDED, ADMIN_FORCED, TOKEN_EXPIRED';
