-- User solved stats table: denormalised solved counts per difficulty
CREATE TABLE IF NOT EXISTS user_solved_stats (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    total       INT NOT NULL DEFAULT 0,
    easy        INT NOT NULL DEFAULT 0,
    medium      INT NOT NULL DEFAULT 0,
    hard        INT NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP NOT NULL DEFAULT now()
);
