-- =============================================
-- V4: Create subjects table
-- Stores subject categories (Physics, Chemistry, Maths) per exam
-- =============================================

CREATE TABLE subjects (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    exam_id     VARCHAR(50)  NOT NULL,
    display_order INT DEFAULT 0,
    created_at  TIMESTAMP DEFAULT now(),
    updated_at  TIMESTAMP DEFAULT now(),

    CONSTRAINT uq_subjects_name_exam UNIQUE (name, exam_id)
);

COMMENT ON TABLE subjects IS 'Subject categories per exam (e.g. Physics / JEE)';
