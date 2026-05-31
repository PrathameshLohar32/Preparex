-- =============================================
-- V7: Create problem_sets table
-- Curated problem collections (e.g. Mains 300, BITSAT 250)
-- =============================================

CREATE TABLE problem_sets (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug            VARCHAR(200)  UNIQUE NOT NULL,
    title           VARCHAR(255)  NOT NULL,
    description     TEXT,
    exam_id         VARCHAR(50),
    is_premium      BOOLEAN       DEFAULT false,
    display_order   INT           DEFAULT 0,
    is_active       BOOLEAN       DEFAULT true,
    created_at      TIMESTAMP     DEFAULT now(),
    updated_at      TIMESTAMP     DEFAULT now()
);

CREATE INDEX idx_problem_sets_slug      ON problem_sets (slug);
CREATE INDEX idx_problem_sets_is_active ON problem_sets (is_active);

COMMENT ON TABLE problem_sets IS 'Curated problem collections (e.g. Mains 300, BITSAT 250)';
