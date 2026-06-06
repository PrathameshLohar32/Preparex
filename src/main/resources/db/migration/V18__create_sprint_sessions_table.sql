-- Sprint sessions table: tracks timed 30-minute sprint blitz sessions
CREATE TABLE IF NOT EXISTS sprint_sessions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    subject_filter  VARCHAR(50),
    difficulty_filter VARCHAR(10),
    total_questions_attempted INT NOT NULL DEFAULT 0,
    total_correct   INT NOT NULL DEFAULT 0,
    total_wrong     INT NOT NULL DEFAULT 0,
    total_skipped   INT NOT NULL DEFAULT 0,
    sprint_points   INT NOT NULL DEFAULT 0,
    started_at      TIMESTAMP NOT NULL DEFAULT now(),
    ended_at        TIMESTAMP,
    exam_id         VARCHAR(50),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_sprint_sessions_user_status ON sprint_sessions(user_id, status);
CREATE INDEX idx_sprint_sessions_started_at ON sprint_sessions(started_at);
