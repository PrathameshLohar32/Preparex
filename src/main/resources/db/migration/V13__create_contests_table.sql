-- =============================================
-- V13: Create contests table
-- Core contest entity with state machine and marking scheme
-- =============================================

CREATE TABLE contests (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title            VARCHAR(255) NOT NULL,
    description      TEXT,
    type             VARCHAR(30)  NOT NULL,
    status           VARCHAR(25)  DEFAULT 'DRAFT',
    exam_id          VARCHAR(50),
    starts_at        TIMESTAMP,
    ends_at          TIMESTAMP,
    duration_mins    INT,
    marking_scheme   JSONB        NOT NULL DEFAULT '{"correct":4,"wrong":-1,"unattempted":0}',
    access_type      VARCHAR(20)  DEFAULT 'FREE',
    paid_amount_inr  NUMERIC(8,2),
    max_participants INT,
    created_at       TIMESTAMP    DEFAULT now(),
    updated_at       TIMESTAMP    DEFAULT now()
);

CREATE INDEX idx_contests_status    ON contests (status);
CREATE INDEX idx_contests_starts_at ON contests (starts_at);

COMMENT ON TABLE contests IS 'Contest state machine: DRAFT→SCHEDULED→LIVE→ENDED→RESULTS_PUBLISHED';
