# StudyFlow API contract (v0.1)

This file is the handoff contract for the frontend and the other backend
modules. Java/JSON uses `camelCase`; database names use `snake_case`.

## Common response

```json
{
  "code": "OK",
  "message": "success",
  "data": {},
  "requestId": "trace-id"
}
```

Errors keep the same shape. The HTTP status and `code` are stable; clients must
not match Chinese or English message text.

| HTTP | Code | Meaning |
|---:|---|---|
| 400 | `VALIDATION_FAILED`, `INVALID_REQUEST`, `INVALID_PAGE`, `EMPTY_PLAN_UPDATE`, `EMPTY_POST_UPDATE`, `INVALID_PLAN_DATES`, `INVALID_PLAN_NAME`, `CHECK_IN_DATE_OUT_OF_RANGE`, `CHECK_IN_NOT_COMPLETED`, `FOLLOW_SELF`, `PASSWORD_TOO_LONG` | Request data is invalid |
| 401 | `AUTHENTICATION_REQUIRED`, `INVALID_ACCESS_TOKEN`, `REVOKED_ACCESS_TOKEN`, `INVALID_CREDENTIALS` | Login is required or credentials are invalid |
| 403 | `ACCESS_DENIED`, `ACCOUNT_DISABLED`, `ACCOUNT_RESTRICTED` | The current user cannot perform the action |
| 404 | `USER_NOT_FOUND`, `PLAN_NOT_FOUND`, `CHECK_IN_NOT_FOUND`, `POST_NOT_FOUND`, `COMMENT_NOT_FOUND` | Resource is absent, not owned by, or not visible to the caller |
| 409 | `EMAIL_ALREADY_EXISTS`, `INVALID_PLAN_STATUS_TRANSITION`, `PLAN_NOT_ACTIVE` | Conflict or invalid state change |
| 415 | `UNSUPPORTED_MEDIA_TYPE` | The request content type is not supported |

## Authentication

- `POST /api/auth/register`: `{ email, password, nickname }`; the server always
  creates a `USER` with `NORMAL` status.
- `POST /api/auth/login`: `{ email, password }`.
- `POST /api/auth/logout`: requires `Authorization: Bearer <token>` and revokes
  the current token.
- `GET /api/me`: returns the current profile.
- `PATCH /api/me`: accepts one or both of `{ nickname, avatarUrl }`.

## Study plans

- `POST /api/plans`: `{ name, startDate, endDate, dailyTarget }`.
- `GET /api/plans?page=1&pageSize=20`: returns
  `{ items, page, pageSize, total }`.
- `GET /api/plans/{id}`: returns a plan owned by the current user.
- `PATCH /api/plans/{id}`: any non-empty subset of
  `{ name, startDate, endDate, dailyTarget, status }`.
- `DELETE /api/plans/{id}`: deletes only the current user's plan.

`startDate` and `endDate` are `YYYY-MM-DD`; `dailyTarget` is minutes and must
be positive. Plan statuses are `ACTIVE`, `PAUSED`, and `COMPLETED`.

## Check-ins

- `PUT /api/plans/{planId}/check-ins/{checkDate}`: creates or updates the
  current user's check-in for that plan/date. Body:
  `{ durationMinutes, completed, note }`.
- `GET /api/plans/{planId}/check-ins`: lists the current user's check-ins for
  that plan.

`checkDate` is `YYYY-MM-DD`. The plan must belong to the caller, be `ACTIVE`,
and contain the supplied date. The database allows only one record per
`userId + planId + checkDate`. Saving a check-in never creates a community post.

## Community

All community routes require a JWT. A `RESTRICTED` user may still use plans and
check-ins, but cannot create, edit, delete, like, follow, or comment in the
community. A `DISABLED` user is rejected by the authentication filter.

- `POST /api/posts`: `{ checkInId, content, visibility }`. The check-in must
  belong to the caller and have `completed=true`; `visibility` is `PUBLIC` or
  `PRIVATE`. The first request returns `201`; a repeated request for the same
  check-in returns its existing post with `200`.
- `GET /api/posts?page=1&pageSize=20&keyword=`: public discovery list. The
  optional `keyword` filters post content.
- `GET /api/posts/search?keyword=...&page=1&pageSize=20`: public search list.
- `GET /api/posts/mine?page=1&pageSize=20`: the caller's non-deleted posts.
- `GET /api/posts/{id}`: an author can view their own non-deleted post; other
  users can only view publicly visible posts.
- `PATCH /api/posts/{id}`: author-only; accepts a non-empty subset of
  `{ content, visibility }`.
- `DELETE /api/posts/{id}`: author-only soft deletion.
- `POST` / `DELETE /api/posts/{id}/likes`: idempotent like or unlike.
- `POST /api/posts/{id}/comments`: `{ content }`; blank content is rejected.
- `GET /api/posts/{id}/comments`, `DELETE /api/comments/{id}`: list visible
  comments or soft-delete the caller's own comment.
- `POST` / `DELETE /api/users/{id}/follow`: idempotent follow or unfollow.

Public discovery, search, and non-author detail access always require
`visibility=PUBLIC`, `status=VISIBLE`, and an author whose status is `NORMAL`.
Private, hidden, deleted, or restricted-author posts appear as `POST_NOT_FOUND`
to other users. Posts and comments are soft-deleted; a post never deletes its
underlying check-in.

The current user is always taken from the JWT. Clients must not send a trusted
`userId`, `role`, or `status` field to claim ownership or elevate privileges.

## Ownership handoff

Chen Yilei's completed P0 backend boundary is authentication, account profile,
permissions, and study plans. Cheng Zekai's community backend is now also
implemented in this repository. The following items remain owned by their
assigned members and are intentionally not included:

- Statistics and uploads: Bao Yihan. The minimal check-in storage/API exists
  only because a community post must reference a real check-in.
- Reports, content moderation, and administrator operations: Chen Hanrui.

When another module needs to reference a plan, it must use the existing
`study_plan.id` and add a new Flyway migration rather than editing the already
applied migrations above.
