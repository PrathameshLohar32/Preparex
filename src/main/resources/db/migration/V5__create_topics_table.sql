-- =============================================
-- V5: Create topics table
-- Stores topics within a subject (e.g. Kinematics under Physics)
-- =============================================

CREATE TABLE topics (
    id          SERIAL PRIMARY KEY,
    subject_id  INT          NOT NULL,
    name        VARCHAR(150) NOT NULL,
    display_order INT DEFAULT 0,
    created_at  TIMESTAMP DEFAULT now(),
    updated_at  TIMESTAMP DEFAULT now(),

    CONSTRAINT fk_topics_subject FOREIGN KEY (subject_id)
        REFERENCES subjects (id) ON DELETE CASCADE
);

CREATE INDEX idx_topics_subject_id ON topics (subject_id);

COMMENT ON TABLE topics IS 'Topics within a subject (e.g. Kinematics under Physics)';
