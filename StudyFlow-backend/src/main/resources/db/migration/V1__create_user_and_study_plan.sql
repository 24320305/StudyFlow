-- Purpose: Chen Yilei's user and study-plan base schema.
-- Dependencies: none.
-- Rollback: drop study_plan before sys_user.

CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    nickname VARCHAR(80) NOT NULL,
    avatar_url VARCHAR(500),
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_user_email UNIQUE (email)
);

CREATE TABLE study_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    daily_target INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_study_plan_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT ck_study_plan_dates CHECK (end_date >= start_date),
    CONSTRAINT ck_study_plan_daily_target CHECK (daily_target > 0)
);

CREATE INDEX idx_study_plan_user_status ON study_plan(user_id, status);
