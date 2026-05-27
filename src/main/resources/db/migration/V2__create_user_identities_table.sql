-- V2: Create user_identities table
CREATE TABLE IF NOT EXISTS user_identities (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider         VARCHAR(50)  NOT NULL,
    provider_user_id VARCHAR(255),
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_provider_provider_user_id UNIQUE (provider, provider_user_id)
);

CREATE INDEX IF NOT EXISTS idx_user_identities_user_id  ON user_identities (user_id);
CREATE INDEX IF NOT EXISTS idx_user_identities_provider ON user_identities (provider, provider_user_id);

COMMENT ON TABLE  user_identities IS 'Links users to their OAuth or local identity providers';
COMMENT ON COLUMN user_identities.provider         IS 'Auth provider: LOCAL, GOOGLE, GITHUB, FACEBOOK';
COMMENT ON COLUMN user_identities.provider_user_id IS 'Unique identifier from the external provider (e.g. Google sub claim). NULL for LOCAL.';
