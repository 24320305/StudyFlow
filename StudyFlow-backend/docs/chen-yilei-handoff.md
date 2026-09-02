# Chen Yilei backend handoff

This document records the completed backend scope for Chen Yilei, the group
leader and backend lead.

## Completed code

- Spring Boot 3 project base, H2 local database profile, MySQL profile, and
  Flyway migrations.
- Shared API response format, request ID, parameter validation, and error
  responses.
- JWT login protection, logout token revocation, `USER`/`ADMIN` permission
  framework, and real-time disabled-account checks.
- Account APIs: register, login, logout, view profile, and edit profile.
- Study-plan APIs: create, list, view, update, change status, and delete.
- Ownership protection: a user can only view or edit their own plans.
- Database base tables: `sys_user`, `study_plan`, and `token_revocation`.

## Completion evidence

The original P0 suite passed with 10 tests on 2026-09-02. After the community
module was added, `mvn test` passed with 14 tests on 2026-09-02. The additional
tests cover completed-check-in publishing, repeated publishing without a second
post, private-post isolation, idempotent likes/follows, blank comments,
post/check-in deletion isolation, and restricted-account community writes.

## Other members' next steps

- Bao Yihan extends the current minimal `check_in` foundation with statistics
  and uploads.
- Cheng Zekai's community migrations/APIs are complete through `V5`; later
  work should use `docs/community-contract.md` as the implementation boundary.
- Chen Hanrui adds reporting, moderation, and administrator APIs.

They must add new Flyway migrations instead of editing `V1`, `V2`, or `V3`.
The shared API naming and response format are in `api-contract.md`; database
handoff order is in `database-ownership.md`.
