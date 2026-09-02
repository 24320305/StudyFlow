# StudyFlow Backend

This project implements the StudyFlow backend foundation and the completed
community module: common API contracts, JWT authentication, account/profile
APIs, study plans, the minimum check-in foundation, and community posts,
comments, likes, and follows.

## Run locally

The default profile uses an in-memory H2 database so the project can start
without MySQL:

```powershell
.\start-backend.ps1
```

The script starts the already packaged JAR with the installed JDK 17. It is the
recommended option for this computer. When it shows `Started
StudyFlowApplication`, open `http://localhost:8080/actuator/health`.

To rebuild after changing Java code, use IntelliJ's Maven tool window and run
the `package` lifecycle task. You can also use the bundled Maven executable:

```powershell
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2024.2.3\plugins\maven\lib\maven3\bin\mvn.cmd' `
  '-Dmaven.repo.local=C:\Users\unescolei\.m2\repository' `
  -f '.\pom.xml' package
```

The default H2 database is for local demonstration only. Its data is cleared
when the backend stops; use the MySQL profile below when you need persistent
data.

For MySQL 8, create a `studyflow` database and set the environment variables
below before starting with the MySQL profile:

```powershell
$env:STUDYFLOW_DB_URL = 'jdbc:mysql://localhost:3306/studyflow?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
$env:STUDYFLOW_DB_USERNAME = 'root'
$env:STUDYFLOW_DB_PASSWORD = 'your-password'
$env:STUDYFLOW_JWT_SECRET = 'a-base64-encoded-secret-with-at-least-32-bytes'
mvn spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

The development JWT secret in `application.yml` must not be used in a shared
or deployed environment.

## Implemented endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/me`
- `PATCH /api/me`
- `POST /api/plans`
- `GET /api/plans`
- `GET /api/plans/{id}`
- `PATCH /api/plans/{id}`
- `DELETE /api/plans/{id}`
- `PUT /api/plans/{planId}/check-ins/{checkDate}`
- `GET /api/plans/{planId}/check-ins`
- `POST /api/posts`
- `GET /api/posts`, `GET /api/posts/search`, `GET /api/posts/mine`
- `GET /api/posts/{id}`, `PATCH /api/posts/{id}`, `DELETE /api/posts/{id}`
- `POST /api/posts/{id}/likes`, `DELETE /api/posts/{id}/likes`
- `POST /api/posts/{id}/comments`, `GET /api/posts/{id}/comments`
- `DELETE /api/comments/{id}`
- `POST /api/users/{id}/follow`, `DELETE /api/users/{id}/follow`

Every JSON response uses `code`, `message`, `data`, and `requestId`.

Community publishing is intentionally based on a completed, owned check-in:
saving a check-in never creates a post automatically, and deleting a post never
deletes its check-in. See `docs/community-contract.md` for the precise request
and visibility rules. Statistics, uploads, reports, content moderation HTTP
endpoints, and administrator pages are still outside this completed module.

## Handoff notes

- The database migrations are append-only. `V1` creates user and plan tables,
  `V2` records logged-out JWTs, `V3` enforces the agreed role/status values,
  `V4` creates check-ins, and `V5` creates community tables.
- Do not put `userId`, `role`, or account status in normal user requests. The
  backend takes the current user from the JWT.
- A plan with check-in records cannot be physically deleted under the current
  foreign-key rules. Decide an archival rule before adding plan deletion to a
  production workflow.
