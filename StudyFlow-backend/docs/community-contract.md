# 社区模块说明

这是成泽楷负责的社区后端模块的当前实现边界。它不是单独的一套项目，直接复用现有的登录、用户、学习计划和统一响应结构。

## 已完成的内容

1. 社区数据表：`post`、`comment`、`follow`、`post_like`，包含外键、唯一约束和查询索引。
2. 最小打卡前置：`check_in` 表，以及保存、更新、查询打卡的接口。
3. 动态发布、发现列表、搜索、我的动态、详情、编辑和软删除。
4. 点赞、取消点赞、评论、删除自己的评论、关注和取消关注。
5. 权限、可见性、重复点击和数据隔离规则，以及自动化接口测试。

## 最重要的业务规则

- 保存打卡和发布动态是两个动作。`PUT /api/plans/{planId}/check-ins/{date}` 只保存打卡，绝不自动发布动态。
- 发布时必须传自己的、已完成的 `checkInId`。一条打卡只能对应一条动态；重复点击发布会返回原动态，不会新增第二条。
- 删除动态只是将 `post.status` 改为 `DELETED`，不会删除打卡。因此统计功能以后仍可读取原始打卡数据。
- 公开列表、搜索和其他用户查看详情时，只展示 `PUBLIC + VISIBLE + 作者 NORMAL` 的动态。
- `PRIVATE`、`HIDDEN`、`DELETED`，或作者已经受限的动态，对其他用户统一返回“未找到”，避免泄露内容是否存在。
- `RESTRICTED` 用户仍能保留并使用自己的计划、打卡，但不能对社区进行写操作；`DISABLED` 用户由 JWT 过滤器直接拦截。
- 点赞和关注是幂等的：连续点击“点赞”或“关注”只保留一条记录；取消操作后可以重新建立。

## 接口速查

| 功能 | 接口 | 说明 |
|---|---|---|
| 保存打卡 | `PUT /api/plans/{planId}/check-ins/{checkDate}` | 同一用户、计划、日期重复提交会更新原记录 |
| 查看打卡 | `GET /api/plans/{planId}/check-ins` | 只能查看自己的计划 |
| 发布动态 | `POST /api/posts` | 请求体为 `checkInId`、`content`、`visibility` |
| 发现和搜索 | `GET /api/posts`、`GET /api/posts/search` | 支持 `keyword`、`page`、`pageSize` |
| 我的动态 | `GET /api/posts/mine` | 包含自己的私密/隐藏内容，不含已删除内容 |
| 动态详情/编辑/删除 | `GET`、`PATCH`、`DELETE /api/posts/{id}` | 编辑和删除只能由作者执行 |
| 点赞 | `POST`、`DELETE /api/posts/{id}/likes` | 返回当前点赞状态和点赞数 |
| 评论 | `POST`、`GET /api/posts/{id}/comments` | 空评论会被拒绝 |
| 删除评论 | `DELETE /api/comments/{id}` | 只能删除自己的评论 |
| 关注 | `POST`、`DELETE /api/users/{id}/follow` | 不能关注自己 |

所有接口使用统一的 JSON 外壳：`code`、`message`、`data`、`requestId`。完整字段、分页和错误码以 `docs/api-contract.md` 为准。

## 当前未做的内容

- 学习统计、图片上传和真实文件管理。
- 举报、内容审核的 HTTP 接口和管理后台页面。社区服务已预留 `changeModerationStatus` 给后续管理员模块调用。
- 社区前端页面与互动组件。后端接口已经可供前端联调，不需要前端再自行计算权限或可见性。
- 真实 MySQL 空库的迁移验证。本地 H2 MySQL 模式已通过 Flyway 启动测试；部署前仍需用目标 MySQL 版本验证一次。
