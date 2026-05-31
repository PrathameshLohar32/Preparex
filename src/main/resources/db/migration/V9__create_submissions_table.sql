-- =============================================
-- V9: Create submissions table
-- Tracks user attempts on problems with scoring results
-- =============================================

CREATE TABLE submissions (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID         NOT NULL,
    problem_id        UUID         NOT NULL,
    status            VARCHAR(20)  NOT NULL,
    submitted_answer  JSONB        NOT NULL,
    marks_awarded     INT          DEFAULT 0,
    time_taken_secs   INT,
    source            VARCHAR(20)  NOT NULL,
    submitted_at      TIMESTAMP    DEFAULT now(),

    CONSTRAINT fk_submissions_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_submissions_problem FOREIGN KEY (problem_id)
        REFERENCES problems (id) ON DELETE CASCADE
);

CREATE INDEX idx_submissions_user_status     ON submissions (user_id, status);
CREATE INDEX idx_submissions_user_problem    ON submissions (user_id, problem_id);
CREATE INDEX idx_submissions_problem_source  ON submissions (problem_id, source);

COMMENT ON TABLE submissions IS 'User problem attempts — scored via strategy pattern';
