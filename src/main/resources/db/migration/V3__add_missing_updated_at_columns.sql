ALTER TABLE roles
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT now();

ALTER TABLE refresh_tokens
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT now();

ALTER TABLE verification_tokens
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT now();

ALTER TABLE password_reset_tokens
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT now();