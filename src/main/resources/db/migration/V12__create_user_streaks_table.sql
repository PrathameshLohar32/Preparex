-- =============================================
-- V12: Create user_streaks table
-- Tracks current and longest streak per user for daily challenges
-- =============================================

CREATE TABLE user_streaks (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID         NOT NULL UNIQUE,
    current_streak   INT          DEFAULT 0,
    longest_streak   INT          DEFAULT 0,
    last_active_date DATE,
    updated_at       TIMESTAMP    DEFAULT now(),

    CONSTRAINT fk_us_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

COMMENT ON TABLE user_streaks IS 'User streak tracking — current, longest, and last active date';
