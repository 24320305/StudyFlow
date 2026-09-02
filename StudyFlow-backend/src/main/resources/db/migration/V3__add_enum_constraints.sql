-- Purpose: keep stored role and status values aligned with the API contract.
-- Dependencies: V1__create_user_and_study_plan.sql.

ALTER TABLE sys_user
    ADD CONSTRAINT ck_sys_user_role CHECK (role IN ('USER', 'ADMIN'));

ALTER TABLE sys_user
    ADD CONSTRAINT ck_sys_user_status CHECK (status IN ('NORMAL', 'RESTRICTED', 'DISABLED'));

ALTER TABLE study_plan
    ADD CONSTRAINT ck_study_plan_status CHECK (status IN ('ACTIVE', 'PAUSED', 'COMPLETED'));
