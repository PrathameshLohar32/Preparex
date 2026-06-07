-- User profiles table: one-to-one extension of users table for profile data
CREATE TABLE IF NOT EXISTS user_profiles (
    id                  UUID PRIMARY KEY,
    bio                 TEXT,
    gender              VARCHAR(20),
    location            VARCHAR(100),
    date_of_birth       DATE,
    twitter_url         VARCHAR(255),
    linkedin_url        VARCHAR(255),
    github_url          VARCHAR(255),
    instagram_url       VARCHAR(255),
    theme               VARCHAR(10) NOT NULL DEFAULT 'LIGHT',
    email_notifications BOOLEAN NOT NULL DEFAULT true,
    push_notifications  BOOLEAN NOT NULL DEFAULT true,
    daily_reminder_time VARCHAR(5) NOT NULL DEFAULT '08:00',
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_profile_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);
