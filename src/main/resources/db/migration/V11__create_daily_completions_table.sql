-- =============================================
-- V11: Create daily_completions table
-- Tracks which daily problems each user has completed
-- =============================================

CREATE TABLE daily_completions (
    id                BIGSERIAL PRIMARY KEY,
    user_id           UUID         NOT NULL,
    daily_problem_id  INT          NOT NULL,
    submission_id     UUID,
    completed_date    DATE         NOT NULL,
    completed_at      TIMESTAMP    DEFAULT now(),

    CONSTRAINT fk_dc_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_dc_daily_problem FOREIGN KEY (daily_problem_id)
        REFERENCES daily_problems (id) ON DELETE CASCADE,
    CONSTRAINT fk_dc_submission FOREIGN KEY (submission_id)
        REFERENCES submissions (id) ON DELETE SET NULL,
    CONSTRAINT uq_dc_user_daily_problem UNIQUE (user_id, daily_problem_id)
);

CREATE INDEX idx_dc_user_completed_date ON daily_completions (user_id, completed_date);

COMMENT ON TABLE daily_completions IS 'Tracks user completion of daily challenge problems';
