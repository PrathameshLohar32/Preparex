-- =============================================
-- V14: Create contest_problems join table
-- Links problems to contests with position, marks, and section
-- =============================================

CREATE TABLE contest_problems (
    id              SERIAL PRIMARY KEY,
    contest_id      UUID         NOT NULL,
    problem_id      UUID         NOT NULL,
    position        INT          DEFAULT 0,
    marks           INT          DEFAULT 4,
    negative_marks  INT          DEFAULT 1,
    section         VARCHAR(50),

    CONSTRAINT fk_cp_contest FOREIGN KEY (contest_id)
        REFERENCES contests (id) ON DELETE CASCADE,
    CONSTRAINT fk_cp_problem FOREIGN KEY (problem_id)
        REFERENCES problems (id) ON DELETE CASCADE,
    CONSTRAINT uq_cp_contest_problem UNIQUE (contest_id, problem_id)
);

CREATE INDEX idx_cp_contest_id ON contest_problems (contest_id);

COMMENT ON TABLE contest_problems IS 'Problems attached to a contest with per-problem marking';
