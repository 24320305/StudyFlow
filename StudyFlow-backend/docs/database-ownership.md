# Database ownership and migration order

The migration chain is implemented through `V5`. New migrations must remain
additive and continue to depend on the existing order:

```text
sys_user -> study_plan -> check_in -> post -> comment/follow/post_like -> report
```

| Migration | Tables | Module owner | State |
|---|---|---|---|
| `V1` | `sys_user`, `study_plan` | Chen Yilei | Applied |
| `V2` | `token_revocation` | Chen Yilei | Applied |
| `V3` | enum check constraints | Chen Yilei | Applied |
| `V4` | `check_in` | Bao Yihan's boundary; minimal foundation added for community dependency | Applied |
| `V5` | `post`, `comment`, `follow`, `post_like` | Cheng Zekai | Applied |

Chen Yilei reviews only cross-table, security, naming, and destructive changes;
he is not the sole author of every module's SQL.

`V5` has user/check-in/post foreign keys, a unique constraint on non-null
`post.checkin_id`, and unique constraints on `(follower_id, following_id)` and
`(user_id, post_id)`. Do not alter `V1` through `V5` after another environment
has applied them; make a new migration instead.
