# StudyFlow 状态与错误码

版本：v1.0  
维护规则：新增状态或错误码需同步接口、测试和报告。

## 社区状态

| 对象 | 状态值 | 说明 |
|---|---|---|
| Post.visibility | PUBLIC | 可进入公开发现、搜索和非作者详情 |
| Post.visibility | PRIVATE | 仅作者可见 |
| Post.status | PENDING | P2 自动审核预留，P0 不主动产生 |
| Post.status | VISIBLE | 正常可见 |
| Post.status | HIDDEN | 管理员隐藏 |
| Post.status | DELETED | 作者软删除 |
| Comment.status | VISIBLE | 正常可见 |
| Comment.status | HIDDEN | 管理员隐藏 |
| Comment.status | DELETED | 作者软删除 |

## 社区错误码

| HTTP | code | 触发场景 |
|---:|---|---|
| 400 | VALIDATION_FAILED | 请求字段为空、长度超限或枚举非法 |
| 400 | INVALID_PAGE | `page < 1`、`pageSize < 1` 或 `pageSize > 100` |
| 400 | EMPTY_POST_UPDATE | 编辑动态时未提交 `content` 或 `visibility` |
| 400 | CHECK_IN_NOT_COMPLETED | 未完成打卡不能发布动态 |
| 400 | FOLLOW_SELF | 用户尝试关注自己 |
| 401 | AUTHENTICATION_REQUIRED | 未携带有效 JWT |
| 403 | ACCOUNT_RESTRICTED | 受限账号尝试发布、编辑、删除、点赞、评论或关注 |
| 404 | POST_NOT_FOUND | 动态不存在、非作者不可见、已删除、私密或作者非 NORMAL |
| 404 | COMMENT_NOT_FOUND | 评论不存在、非作者或已删除 |
| 404 | CHECK_IN_NOT_FOUND | 发布动态时打卡不存在或不属于当前用户 |
| 404 | USER_NOT_FOUND | 关注目标不存在或不可见 |
| 409 | DATA_CONFLICT | 数据库唯一约束冲突 |

## 公开可见性规则

公开列表、搜索和非作者详情必须同时满足：

```text
post.visibility = PUBLIC
post.status = VISIBLE
post.author.status = NORMAL
```

不满足时统一按 `POST_NOT_FOUND` 或空列表处理，避免泄露私密或审核状态。
