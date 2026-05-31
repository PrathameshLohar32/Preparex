-- =============================================
-- V10: Create daily_problems table
-- Stores the 3 daily challenge problems (one per subject) per day
-- =============================================

CREATE TABLE daily_problems (
    id              SERIAL PRIMARY KEY,
    problem_id      UUID         NOT NULL,
    subject_id      INT          NOT NULL,
    scheduled_date  DATE         NOT NULL,
    is_active       BOOLEAN      DEFAULT true,
    created_at      TIMESTAMP    DEFAULT now(),

    CONSTRAINT fk_dp_problem FOREIGN KEY (problem_id)
        REFERENCES problems (id) ON DELETE CASCADE,
    CONSTRAINT fk_dp_subject FOREIGN KEY (subject_id)
        REFERENCES subjects (id) ON DELETE CASCADE
);

CREATE INDEX idx_daily_problems_date ON daily_problems (scheduled_date);

COMMENT ON TABLE daily_problems IS 'Daily challenge — 3 problems per day (one per subject)';
