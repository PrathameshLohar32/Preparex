-- User sprint stats table: aggregate sprint performance metrics
CREATE TABLE IF NOT EXISTS user_sprint_stats (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    total_sprints   INT NOT NULL DEFAULT 0,
    total_points    INT NOT NULL DEFAULT 0,
    best_weekly_rank INT,
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);
