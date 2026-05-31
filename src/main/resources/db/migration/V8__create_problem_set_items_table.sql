-- =============================================
-- V8: Create problem_set_items join table
-- Links problems to problem sets with ordering
-- =============================================

CREATE TABLE problem_set_items (
    id          SERIAL PRIMARY KEY,
    set_id      UUID    NOT NULL,
    problem_id  UUID    NOT NULL,
    position    INT     DEFAULT 0,

    CONSTRAINT fk_psi_problem_set FOREIGN KEY (set_id)
        REFERENCES problem_sets (id) ON DELETE CASCADE,
    CONSTRAINT fk_psi_problem FOREIGN KEY (problem_id)
        REFERENCES problems (id) ON DELETE CASCADE,
    CONSTRAINT uq_psi_set_problem UNIQUE (set_id, problem_id)
);

CREATE INDEX idx_psi_set_id ON problem_set_items (set_id);

COMMENT ON TABLE problem_set_items IS 'Join table linking problems to curated problem sets with ordering';
