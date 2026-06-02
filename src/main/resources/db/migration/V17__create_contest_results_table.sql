-- =============================================
-- V17: Create contest_results table
-- Final computed results after contest ends
-- =============================================

CREATE TABLE contest_results (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contest_id          UUID         NOT NULL,
    user_id             UUID         NOT NULL,
    total_score         INT          DEFAULT 0,
    rank                INT,
    percentile          FLOAT,
    correct_count       INT          DEFAULT 0,
    wrong_count         INT          DEFAULT 0,
    unattempted_count   INT          DEFAULT 0,
    time_taken_secs     INT          DEFAULT 0,
    subject_breakdown   JSONB,
    finalized_at        TIMESTAMP    DEFAULT now(),

    CONSTRAINT fk_cres_contest FOREIGN KEY (contest_id)
        REFERENCES contests (id) ON DELETE CASCADE,
    CONSTRAINT fk_cres_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_cres_contest_user UNIQUE (contest_id, user_id)
);

CREATE INDEX idx_cres_contest_rank ON contest_results (contest_id, rank);

COMMENT ON TABLE contest_results IS 'Final contest results with rank, percentile, and subject breakdown';
