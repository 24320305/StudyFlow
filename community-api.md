# StudyFlow 社区模块接口设计

负责人：成泽楷  
版本：v1.0

## 模块范围

- 动态发布、发现列表、搜索、我的动态、详情、编辑、删除
- 点赞、取消点赞
- 评论、评论列表、删除自己的评论
- 关注、取消关注

所有接口都复用统一响应结构：

```json
{
  "code": "OK",
  "message": "success",
  "data": {},
  "requestId": "trace-id"
}
```

## 动态接口

### POST /api/posts

发布学习动态。P0 必须关联当前用户已完成的打卡。

请求：

```json
{
  "checkInId": 1,
  "content": "今天学习 Java 两小时",
  "visibility": "PUBLIC"
}
```

响应：

- 首次发布返回 `201`
- 同一 `checkInId` 重复发布返回 `200` 和已有动态

### GET /api/posts

公开发现列表。

查询参数：

| 参数 | 必填 | 说明 |
|---|---|---|
| keyword | 否 | 按动态内容搜索 |
| page | 否 | 从 1 开始 |
| pageSize | 否 | 1 到 100 |

只返回 `PUBLIC + VISIBLE + 作者 NORMAL` 的动态。

### GET /api/posts/search

公开搜索接口。`keyword` 必填，分页参数同发现列表。

### GET /api/posts/mine

查看当前用户自己的非删除动态，包含私密动态。

### GET /api/posts/{id}

查看动态详情。作者可以查看自己的非删除动态；其他用户只能查看公开可见动态。

### PATCH /api/posts/{id}

编辑自己的动态。

请求：

```json
{
  "content": "更新后的动态内容",
  "visibility": "PRIVATE"
}
```

`content` 和 `visibility` 至少提交一个。

### DELETE /api/posts/{id}

软删除自己的动态，只将 `post.status` 改为 `DELETED`，不会删除关联的 `CheckIn`。

## 点赞接口

### POST /api/posts/{id}/likes

点赞公开可互动动态。重复点赞幂等，不新增第二条记录。

响应：

```json
{
  "postId": 1,
  "liked": true,
  "likeCount": 1
}
```

### DELETE /api/posts/{id}/likes

取消点赞。重复取消保持幂等。

## 评论接口

### POST /api/posts/{id}/comments

发表评论。

请求：

```json
{
  "content": "这条学习经验很有用"
}
```

空白评论返回 `VALIDATION_FAILED`。

### GET /api/posts/{id}/comments

查看动态下所有 `VISIBLE` 评论。

### DELETE /api/comments/{id}

删除自己的评论，采用软删除。

## 关注接口

### POST /api/users/{id}/follow

关注用户。不能关注自己；重复关注幂等；目标用户必须为 `NORMAL`。

### DELETE /api/users/{id}/follow

取消关注。重复取消保持幂等。

## 权限和可见性

- `RESTRICTED` 用户不能发布、编辑、删除、点赞、评论或关注。
- 非作者访问 `PRIVATE`、`HIDDEN`、`DELETED` 或作者受限的动态时统一返回 404。
- 所有普通用户请求都从 JWT 获取当前用户，不接收请求体中的可信 `userId`、`role` 或 `status`。
