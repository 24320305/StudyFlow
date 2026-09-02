-- Purpose: minimal check-in records required by the community publishing flow.
-- Ownership: Bao Yihan extends this foundation with statistics and uploads.
-- Dependencies: V1__create_user_and_study_plan.sql.

CREATE TABLE check_in (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    check_date DATE NOT NULL,
    duration_minutes INT NOT NULL,
    completed BOOLEAN NOT NULL,
    note VARCHAR(500),
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_check_in_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_check_in_plan FOREIGN KEY (plan_id) REFERENCES study_plan(id),
    CONSTRAINT uk_check_in_user_plan_date UNIQUE (user_id, plan_id, check_date),
    CONSTRAINT ck_check_in_duration CHECK (duration_minutes >= 0)
);

CREATE INDEX idx_check_in_user_date ON check_in(user_id, check_date);
