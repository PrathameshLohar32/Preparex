-- User badges table: earned badges with idempotent unique constraint
CREATE TABLE IF NOT EXISTS user_badges (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    badge_type  VARCHAR(50) NOT NULL,
    context     VARCHAR(255),
    awarded_at  TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(user_id, badge_type)
);

CREATE INDEX idx_user_badges_user ON user_badges(user_id);
