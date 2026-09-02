-- Purpose: make POST /api/auth/logout effective for issued JWTs.
-- Dependencies: V1__create_user_and_study_plan.sql.
-- Rollback: drop token_revocation.

CREATE TABLE token_revocation (
    jti VARCHAR(64) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_token_revocation_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

CREATE INDEX idx_token_revocation_expires_at ON token_revocation(expires_at);
