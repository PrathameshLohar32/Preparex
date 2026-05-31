-- =============================================
-- V6: Create problems table
-- Core problem table supporting MCQ, Numerical, Paragraph, Assertion types
-- Uses JSONB for flexible options/answer/hints storage
-- =============================================

CREATE TABLE problems (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug            VARCHAR(200)  UNIQUE NOT NULL,
    title           VARCHAR(500)  NOT NULL,
    body_text       TEXT          NOT NULL,
    figure_url      VARCHAR(500),
    question_type   VARCHAR(30)   NOT NULL,
    difficulty      VARCHAR(10)   NOT NULL,
    options         JSONB,
    answer_key      JSONB         NOT NULL,
    solution_text   TEXT,
    hints           JSONB,
    subject_id      INT,
    topic_id        INT,
    exam_id         VARCHAR(50)   NOT NULL,
    pyq_year        INT,
    parent_id       UUID,
    is_active       BOOLEAN       DEFAULT true,
    is_premium      BOOLEAN       DEFAULT false,
    attempt_count   INT           DEFAULT 0,
    correct_count   INT           DEFAULT 0,
    created_at      TIMESTAMP     DEFAULT now(),
    updated_at      TIMESTAMP     DEFAULT now(),

    CONSTRAINT fk_problems_subject FOREIGN KEY (subject_id)
        REFERENCES subjects (id) ON DELETE SET NULL,
    CONSTRAINT fk_problems_topic FOREIGN KEY (topic_id)
        REFERENCES topics (id) ON DELETE SET NULL,
    CONSTRAINT fk_problems_parent FOREIGN KEY (parent_id)
        REFERENCES problems (id) ON DELETE SET NULL
);

-- Performance indexes for common query patterns
CREATE INDEX idx_problems_topic_difficulty   ON problems (topic_id, difficulty);
CREATE INDEX idx_problems_subject_difficulty ON problems (subject_id, difficulty);
CREATE INDEX idx_problems_is_active          ON problems (is_active);
CREATE INDEX idx_problems_exam_id            ON problems (exam_id);
CREATE INDEX idx_problems_parent_id          ON problems (parent_id);
CREATE INDEX idx_problems_slug               ON problems (slug);

COMMENT ON TABLE problems IS 'Core problem bank — supports MCQ_SINGLE, MCQ_MULTIPLE, NUMERICAL, PARAGRAPH, ASSERTION';
