-- User subject stats table: per-subject accuracy for radar chart
CREATE TABLE IF NOT EXISTS user_subject_stats (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subject_id  INT NOT NULL REFERENCES subjects(id),
    solved      INT NOT NULL DEFAULT 0,
    attempted   INT NOT NULL DEFAULT 0,
    accuracy    FLOAT NOT NULL DEFAULT 0.0,
    updated_at  TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(user_id, subject_id)
);

CREATE INDEX idx_user_subject_stats_user ON user_subject_stats(user_id);
