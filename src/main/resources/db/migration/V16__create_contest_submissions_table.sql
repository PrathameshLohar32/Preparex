-- =============================================
-- V16: Create contest_submissions table
-- Per-question submissions within a contest
-- =============================================

CREATE TABLE contest_submissions (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contest_id        UUID         NOT NULL,
    user_id           UUID         NOT NULL,
    problem_id        UUID         NOT NULL,
    status            VARCHAR(20),
    submitted_answer  JSONB,
    marks_awarded     INT          DEFAULT 0,
    time_taken_secs   INT,
    submitted_at      TIMESTAMP    DEFAULT now(),

    CONSTRAINT fk_cs_contest FOREIGN KEY (contest_id)
        REFERENCES contests (id) ON DELETE CASCADE,
    CONSTRAINT fk_cs_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_cs_problem FOREIGN KEY (problem_id)
        REFERENCES problems (id) ON DELETE CASCADE,
    CONSTRAINT uq_cs_contest_user_problem UNIQUE (contest_id, user_id, problem_id)
);

CREATE INDEX idx_cs_contest_user ON contest_submissions (contest_id, user_id);
CREATE INDEX idx_cs_contest      ON contest_submissions (contest_id);

COMMENT ON TABLE contest_submissions IS 'Per-question contest submissions — one answer per problem per user per contest';
