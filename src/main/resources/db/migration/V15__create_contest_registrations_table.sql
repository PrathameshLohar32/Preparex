-- =============================================
-- V15: Create contest_registrations table
-- Student registration with attempt tracking
-- =============================================

CREATE TABLE contest_registrations (
    id                  BIGSERIAL PRIMARY KEY,
    contest_id          UUID         NOT NULL,
    user_id             UUID         NOT NULL,
    started             BOOLEAN      DEFAULT false,
    started_at          TIMESTAMP,
    final_submitted_at  TIMESTAMP,
    registered_at       TIMESTAMP    DEFAULT now(),

    CONSTRAINT fk_cr_contest FOREIGN KEY (contest_id)
        REFERENCES contests (id) ON DELETE CASCADE,
    CONSTRAINT fk_cr_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_cr_contest_user UNIQUE (contest_id, user_id)
);

CREATE INDEX idx_cr_contest_id ON contest_registrations (contest_id);
CREATE INDEX idx_cr_user_id    ON contest_registrations (user_id);

COMMENT ON TABLE contest_registrations IS 'Student contest registration with attempt tracking';
