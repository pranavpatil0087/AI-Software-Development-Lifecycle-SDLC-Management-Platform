ALTER TABLE users
    ADD COLUMN job_title VARCHAR(150);

CREATE TABLE user_skills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    skill_name VARCHAR(100) NOT NULL,
    proficiency VARCHAR(20) NOT NULL DEFAULT 'INTERMEDIATE'
        CHECK (proficiency IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_skill UNIQUE (user_id, skill_name)
);

CREATE INDEX idx_user_skills_user_id ON user_skills (user_id);