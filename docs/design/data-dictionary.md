# StudyFlow 数据字典

版本：v1.0  
维护规则：表负责人随迁移和接口变更同步更新。本文收录成泽楷负责的社区模块正式字段。

## 社区模块

### post

负责人：成泽楷

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 动态编号 |
| user_id | BIGINT | NOT NULL, FK -> sys_user.id | 发布者 |
| checkin_id | BIGINT | NULL, FK -> check_in.id, UNIQUE | 关联打卡；P0 发布必须传入 |
| content | VARCHAR(500) | NOT NULL | 动态内容 |
| visibility | VARCHAR(20) | NOT NULL, PUBLIC/PRIVATE | 可见范围 |
| status | VARCHAR(20) | NOT NULL, PENDING/VISIBLE/HIDDEN/DELETED | 内容状态 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |

业务规则：新发布动态默认为 `VISIBLE`；公开入口只展示 `PUBLIC + VISIBLE + 作者 NORMAL`；作者删除只改为 `DELETED`，不删除关联打卡。

### comment

负责人：成泽楷

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 评论编号 |
| user_id | BIGINT | NOT NULL, FK -> sys_user.id | 评论者 |
| post_id | BIGINT | NOT NULL, FK -> post.id | 所属动态 |
| content | VARCHAR(500) | NOT NULL | 评论内容 |
| status | VARCHAR(20) | NOT NULL, VISIBLE/HIDDEN/DELETED | 评论状态 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |

业务规则：只能评论公开可见且未删除的动态；空白评论拒绝；删除评论为软删除。

### follow

负责人：成泽楷

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 关注记录编号 |
| follower_id | BIGINT | NOT NULL, FK -> sys_user.id | 关注发起人 |
| following_id | BIGINT | NOT NULL, FK -> sys_user.id | 被关注人 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |

业务规则：`UNIQUE(follower_id, following_id)` 防止重复关注；`CHECK(follower_id <> following_id)` 禁止关注自己；目标用户必须为 `NORMAL`。

### post_like

负责人：成泽楷

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 点赞记录编号 |
| user_id | BIGINT | NOT NULL, FK -> sys_user.id | 点赞用户 |
| post_id | BIGINT | NOT NULL, FK -> post.id | 被点赞动态 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |

业务规则：`UNIQUE(user_id, post_id)` 防止重复点赞；点赞和取消点赞接口保持幂等。
