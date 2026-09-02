# StudyFlow 五人分工统一执行方案（执行版）

> 适用范围：六天内完成 StudyFlow 的账户、计划、打卡统计、社区和后台治理。目标不是把任务平均切开，而是让每一个接口、表、页面、测试和报告段落都只有一个可追溯的正式版本。

## 1. 先定五条总规则

1. **一项产物只有一名主负责人。** 产物包括数据库表、迁移脚本、Controller/Service、前端页面、可复用组件、测试用例、报告小节和部署文件。协作者可以提 Issue 或 PR，但不能各自维护第二份实现。
2. **每人必须同时留下代码、报告、测试和协作证据。** 不以开会、联调、统稿或测试替代代码；也不要求每个人都强行写一套前后端。每人至少有自己的可运行代码、本人主笔段落、正常和异常用例、截图或日志、Issue/提交/审查记录。
3. **先冻结契约，再开始并行开发。** 第一天确认数据字典、OpenAPI、状态与错误码、权限矩阵、需求到测试追踪表。字段、状态、接口、页面四者必须同日更新。
4. **“谁能看见”和“谁能修改”由服务端决定。** 前端路由守卫和隐藏按钮只改善体验，不能代替 JWT、当前账号状态和资源归属校验。
5. **通知功能降为 P2 预留。** 原说明书没有通知的实体、页面、完整业务流程或 TC 用例。本轮不建 `notification` 表、不写通知接口、不做通知页面；P0/P1 完成后才可另开 Issue 扩展。

## 2. 五人的正式分工

| 成员 | 唯一主责代码 | 本人主笔报告 | 测试与其他职责 | 明确边界 |
|---|---|---|---|---|
| 陈亦雷 | Spring Boot 3 骨架；`common/contract`、JWT、安全过滤器、角色与资源归属校验；`sys_user`、`study_plan` 迁移和账户/计划接口 | 摘要与关键词、1.1、4.1-4.2、4.4、7.1；最终统稿和 PDF 导出 | TC-01、TC-02 的模块测试；维护 `main`、发布标签、公共契约和安全决策 | 不逐个审批普通业务 PR，不代写其他人模块；只审核安全、跨表、破坏性和公共契约变更 |
| 汪晨烨 | Vue 3/Vite/Element Plus 初始化；路由、Axios 客户端、认证状态、用户端登录注册、首页、计划、打卡、日历和统计页面 | 第 2 章、5.1-5.3；用户端用例图、截图和交互说明 | 用户端流程和表单校验；为 TC-01 至 TC-03 提供页面证据 | 不维护社区后端、后台页面或动态卡片；只通过统一领域 API 调用后端 |
| 鲍奕涵 | `common/web`、全局异常、参数校验；`check_in`、统计、上传、健康检查；多环境配置、部署脚本；社区发现页和详情页页面壳；打卡图片上传组件 | 第 3.1、3.2 的“打卡保存”部分；5.4 的发现/详情页说明；附录 API、错误码、部署环境 | TC-03；OpenAPI 模板、聚合和每日联调核验；部署和浏览器兼容性验证 | 不实现通知 P2，不维护社区业务规则或认证组件；不手工代写其他模块的接口文档 |
| 成泽楷 | `post`、`comment`、`follow`、`post_like` 迁移；**动态发布**、列表/搜索、点赞、关注、评论接口；`PostComposer`、`PostCard`、搜索和互动组件 | 1.2-1.3、3.2 的“发布与互动”部分、4.3、5.4 社区互动部分、6.1-6.2 | TC-04 至 TC-06；绘制唯一正式 ER 图；维护需求和迭代字段 | 不直接修改 `sys_user`、`study_plan`、`check_in`；提交社区迁移后由陈亦雷按规则审核 |
| 陈瀚锐 | `report` 迁移；管理员用户/内容/举报接口和 `/admin/**` 页面；测试框架、回归脚本 | 3.3、5.4 管理后台部分、6.3；管理员用例图和流程说明 | TC-07、TC-08、全量回归；维护 Bug 状态、严重程度、复测结论 | 不另建权限体系；管理员跨模块动作只调用公开业务 Service，不直接越过模块边界写 Repository |

### 每个人的完成定义

每人完成自己的模块前，必须同时满足：

1. 一项可运行的主责代码已经合并；
2. 至少一个正常用例和一个异常、越权或重复提交用例已通过；
3. 报告段落包含需求边界、数据/API 设计、实现证据、测试结果和已知限制；
4. 有一张截图、接口响应、测试日志或部署日志；
5. 有一个 Issue、一条提交和一次非作者审查；
6. Bug 由**模块代码负责人修复**，陈瀚锐负责协调和最终回归，不替所有人修业务 Bug。

## 3. 决策、实现、独立验收

使用三个标记避免“大家负责”：D 表示拍板并维护正式版本，I 表示实现，A 表示独立验收。A 不能由实现者本人担任。

| 事项 | D | I | A | 交付物 |
|---|---|---|---|---|
| 数据命名、主键、外键、状态枚举 | 陈亦雷 | 各表负责人 | 陈瀚锐在空库初始化；成泽楷同步 ER 图 | `data-dictionary.md`、迁移脚本、ER 图 |
| 安全、JWT、统一响应和公共 API 契约 | 陈亦雷 | 陈亦雷负责安全；鲍奕涵负责异常/校验映射 | 陈瀚锐 | `openapi.yaml`、权限矩阵、错误码表 |
| 账户、个人资料、学习计划 | 陈亦雷 | 陈亦雷后端，汪晨烨页面 | 陈瀚锐 | 接口、页面、TC-01/TC-02 证据 |
| 打卡、统计、图片上传 | 鲍奕涵 | 鲍奕涵后端/上传，汪晨烨页面 | 陈瀚锐 | 接口、统计口径、TC-03 证据 |
| 社区发布、搜索、互动 | 成泽楷 | 成泽楷领域接口/组件，鲍奕涵页面壳 | 陈瀚锐 | 社区 DTO、TC-04 至 TC-06 |
| 举报与后台治理流程 | 陈瀚锐 | 陈瀚锐 | 鲍奕涵；陈亦雷复核 ADMIN 权限 | 后台接口、页面、TC-07/TC-08 |
| 前端应用壳、路由、Axios 和状态显示规范 | 汪晨烨 | 汪晨烨 | 鲍奕涵 | 路由表、API 客户端、公共常量 |
| 部署与发布 | 鲍奕涵 | 鲍奕涵部署；各模块负责人修本模块问题 | 陈瀚锐 | 启动说明、健康检查、部署验证 |
| `main` 合并与发布标签 | 陈亦雷 | 各 PR 作者 | 陈瀚锐确认回归结论 | 版本标签、变更记录 |

陈亦雷的数据库审核采用两个固定窗口：第 1 天冻结总模型，第 3 天复核跨模块关联。普通的、与数据字典一致的单表新增字段由表负责人提交迁移并经一名非作者审查即可；涉及外键、公共字段、删除/重命名字段、状态变更或数据迁移时，必须再由陈亦雷批准。这样既保留统一架构，又不会让所有开发排队等待。

## 4. 第一天冻结的唯一真源

以下五个版本化文件必须进入仓库；任何口头约定都不能覆盖它们：

```text
docs/design/data-dictionary.md
docs/contracts/openapi.yaml
docs/design/states-and-errors.md
docs/design/permission-matrix.md
docs/testing/requirement-test-traceability.md
```

| 文件 | D | 日常维护规则 |
|---|---|---|
| 数据字典 | 陈亦雷 | 表负责人随迁移 PR 更新自己的表；跨表项需陈亦雷批准 |
| OpenAPI | 鲍奕涵 | 每个接口负责人必须在修改 Controller/DTO 的同一 PR 更新自己负责的路径；鲍奕涵只负责模板、lint、汇总和每日比对 |
| 状态与错误码 | 陈亦雷 | 鲍奕涵实现异常映射；新增状态或错误码必须先开 Issue |
| 权限矩阵 | 陈亦雷 | 模块负责人补充资源动作；陈瀚锐用它写越权用例 |
| 需求-测试追踪表 | 陈瀚锐 | 成泽楷维护需求/迭代列；每个模块负责人补充对应证据 |

### 4.1 命名、格式和响应契约

| 项目 | 固定规则 |
|---|---|
| Java、JSON、Vue 字段 | `camelCase` |
| 数据库表和字段 | `snake_case`；用户物理表使用 `sys_user` |
| 主键 | `BIGINT` 自增；不能按模块混用 UUID 和自增主键 |
| 日期 | `YYYY-MM-DD`；计划和打卡日期不携带时间 |
| 时间 | 带 `+08:00` 的 ISO 8601；展示按 `Asia/Shanghai` |
| 分页 | `page` 从 1 开始；统一返回 `items`、`page`、`pageSize`、`total` |
| 列表/空值 | 空集合返回 `[]`；无单对象返回 `null`；不得同一字段混用空字符串和 `null` |

所有接口统一响应为：

```json
{
  "code": "OK",
  "message": "success",
  "data": {},
  "requestId": "trace-id"
}
```

失败响应保持同一结构，并按 HTTP 状态表达语义：401 未登录或凭证无效，403 无权限或账号受限，404 不存在或不可见，409 邮箱重复/重复关系/非法状态流转，413 文件过大，415 文件类型不支持。前端只能按稳定业务码和 HTTP 状态处理，不能匹配中文错误文案。

### 4.2 关键 DTO 字段字典

这张表是防止 `title/name`、`actualMinutes/durationMinutes`、`auditStatus/status` 漂移的最低约束。其他字段可以扩展，但不得改变下列名称和含义。

| 概念对象 | API/Java 字段 | 数据库字段 | 关键规则 |
|---|---|---|---|
| User | `email`、`nickname`、`avatarUrl`、`role`、`status` | `email`、`nickname`、`avatar_url`、`role`、`status` | `email` 唯一；密码只存哈希；注册接口只能创建 `USER` |
| StudyPlan | `name`、`startDate`、`endDate`、`dailyTarget`、`status` | `name`、`start_date`、`end_date`、`daily_target`、`status` | `dailyTarget` 的单位在字典中固定为分钟；结束日期不得早于开始日期 |
| CheckIn | `checkDate`、`durationMinutes`、`completed`、`note`、`imageUrl` | `check_date`、`duration_minutes`、`completed`、`note`、`image_url` | `completed` 是布尔值；没有记录即统计上的未打卡，不能持久化 `MISSED` |
| Post | `checkinId`、`content`、`visibility`、`status` | `checkin_id`、`content`、`visibility`、`status` | 使用 `status`，绝不使用 `auditStatus`；`visibility` 与 `status` 独立 |
| Comment | `postId`、`content`、`status` | `post_id`、`content`、`status` | 使用 `status`，空评论拒绝 |
| Report | `targetType`、`targetId`、`reason`、`status`、`handledBy`、`handledAt`、`remark` | `target_type`、`target_id`、`reason`、`status`、`handled_by`、`handled_at`、`remark` | `targetId` 是多态关联，不能声明成指向多个表的物理外键 |

## 5. 状态、权限和关键业务边界

### 5.1 唯一状态表

| 对象 | 固定值和规则 |
|---|---|
| 角色 | `USER`、`ADMIN`；注册请求中的 `role` 一律忽略 |
| 用户状态 | `NORMAL`、`RESTRICTED`、`DISABLED`；`RESTRICTED` 可保留计划和打卡，但不能发动态、评论、点赞或关注；`DISABLED` 不能登录 |
| 计划状态 | `ACTIVE`、`PAUSED`、`COMPLETED`；只有 `ACTIVE` 可以新增或更新当天打卡 |
| 打卡完成情况 | `completed: true/false`；不引入 `COMPLETED`、`PARTIAL`、`MISSED` 枚举，也不写入“缺失打卡”记录 |
| 动态可见性 | `PUBLIC`、`PRIVATE` |
| 动态状态 | `PENDING`、`VISIBLE`、`HIDDEN`、`DELETED`；字段名固定为 `status` |
| 评论状态 | `VISIBLE`、`HIDDEN`、`DELETED`；字段名固定为 `status` |
| 举报状态 | `OPEN`、`PROCESSING`、`RESOLVED`、`REJECTED`；完成处理必须记录处理人、时间、动作和备注 |

P0 的动态策略也必须一次定死：新发布动态直接为 `VISIBLE`；管理员可在 `VISIBLE` 与 `HIDDEN` 间切换，作者删除后为 `DELETED`。`PENDING` 仅是后续自动审核的预留值，本轮任何接口不能擅自把它当成另一个流程。公开列表、搜索和普通用户详情只返回 `PUBLIC + VISIBLE + 作者 NORMAL` 的动态。

### 5.2 打卡、发布、图片和举报的边界

1. `PUT /api/plans/{id}/check-ins/{date}` 只保存或更新 `CheckIn`，**永远不自动创建 `Post`**。
2. `POST /api/posts` 由成泽楷实现。P0 中必须传入属于当前用户、且已保存成功的 `checkinId`；服务端校验归属后创建动态。数据库 `post.checkin_id` 保持可空以保留以后扩展空间，但 P0 业务不允许无打卡来源的自由发帖。
3. `post.checkin_id` 对非空值唯一。一条打卡至多发布一条动态；重复点击发布返回同一条动态或稳定的幂等结果，不能产生第二条。
4. 删除或隐藏 `Post` 只改变 `post.status`，绝不级联删除 `CheckIn`；统计始终只读取计划和打卡原始数据。
5. P0 图片只支持一张可选的 `CheckIn.imageUrl`，不建附件表、不支持社区独立配图或后台任意上传。动态需要图片时展示关联打卡图片。个人头像若保留，也走同一个上传入口，不另做附件体系。
6. `POST /api/uploads` 使用 `multipart/form-data` 的 `file` 字段，白名单为 JPG/PNG/WebP、上限 5 MB，服务端重命名并返回 `{ "fileId": "...", "url": "..." }`。打卡提交的 `imageUrl` 必须来自当前用户的有效上传结果；删除接口只允许清理没有被有效记录引用的文件。
7. P0 举报目标只允许 `POST` 和 `USER`。`report.target_id` 是多态值，服务层按 `targetType` 校验目标存在，不能把它伪装成多重外键；`reporter_id` 和 `handled_by` 可以正常外键指向 `sys_user`。评论举报需另开 P2 变更。
8. 管理员处理举报时，修改动态状态必须调用成泽楷公开的社区 Service；限制账号必须调用陈亦雷公开的用户 Service。禁止后台 Controller 直接写跨模块 Repository 或 SQL。

### 5.3 服务端权限规则

- 所有普通用户请求从 JWT 获取当前用户，不能信任请求体中的 `userId`、`role`、`status`、`handledBy` 或内容状态字段。
- 受保护请求除校验 JWT 外，还必须查询当前账号状态，不能只相信旧 token 中的角色声明；后台限制应立即生效。
- 计划、打卡、动态、评论、上传文件的读写均需校验资源归属；不可见资源对普通用户统一按 404 处理，避免泄露内容是否存在。
- 前端状态标签、枚举中文文案集中在一个常量文件；组件中不得硬编码第二套状态映射。

### 5.4 统计口径

统计由鲍奕涵的后端 Service 统一计算，汪晨烨的页面只展示接口结果：

- `totalDurationMinutes` 是查询范围内、当前用户有效 `CheckIn.durationMinutes` 的总和；
- `completionRate` 是 `completed=true` 的打卡天数除以计划的有效日期数；统计区间为 `max(queryStart, startDate)` 至 `min(queryEnd, endDate, today)`。分母为 0 时返回 `null`，不返回伪造的 0%；
- `streakDays` 按统计区间末尾向前连续的 `completed=true` 的 `checkDate` 计算，缺少记录即中断；
- 当前模型没有“暂停历史”表，因此不能在报告或页面中声称暂停日期会自动从历史完成率分母剔除。若需要该规则，必须新增模型、接口和 TC 用例后才能启用。

## 6. 数据库、迁移和跨模块调用

| 概念对象 | 物理表 | 表负责人 | 必须约束 |
|---|---|---|---|
| User | `sys_user` | 陈亦雷 | `email` 唯一；密码哈希；角色和状态使用固定枚举 |
| StudyPlan | `study_plan` | 陈亦雷 | `user_id` 外键；日期合法 |
| CheckIn | `check_in` | 鲍奕涵 | `UNIQUE(user_id, plan_id, check_date)`；时长、日期范围和计划归属校验 |
| Post | `post` | 成泽楷 | `checkin_id` 可空且非空时唯一；`visibility`、`status` 分列 |
| Comment | `comment` | 成泽楷 | 仅能评论可见且未删除动态；内容非空 |
| Follow | `follow` | 成泽楷 | `UNIQUE(follower_id, following_id)`；禁止关注自己 |
| PostLike | `post_like` | 成泽楷 | `UNIQUE(post_id, user_id)`；重复点击幂等 |
| Report | `report` | 陈瀚锐 | `reporter_id`、`handled_by` 外键；`target_type + target_id` 由服务层验证 |

迁移顺序固定为：`sys_user -> study_plan -> check_in -> post -> comment/follow/post_like -> report`。迁移脚本只追加，不修改已经合并的历史迁移；每份迁移写明版本、目的、依赖和回滚说明。

跨模块调用只允许走公开 Service：打卡模块调用计划归属校验；社区发布调用打卡摘要/归属校验；后台修改用户或内容状态调用对应领域的状态变更 Service。任何人不得为了方便直接引用其他模块内部 Repository。

## 7. 接口和前端的文件归属

### 7.1 接口主责

| 接口范围 | 实现与 OpenAPI 主责 | 前端使用方 |
|---|---|---|
| `POST /api/auth/register`、`POST /api/auth/login`、`POST /api/auth/logout`、`GET/PATCH /api/me` | 陈亦雷 | 汪晨烨 |
| `POST/GET/PATCH/DELETE /api/plans`、`GET/PATCH /api/plans/{id}` | 陈亦雷 | 汪晨烨 |
| `GET /api/plans/{id}/check-ins`、`PUT /api/plans/{id}/check-ins/{date}`、`GET /api/statistics` | 鲍奕涵 | 汪晨烨 |
| `POST /api/uploads`、`DELETE /api/uploads/{fileId}` | 鲍奕涵 | 汪晨烨 |
| `POST /api/posts`、`GET/PATCH/DELETE /api/posts/{id}`、`GET /api/posts`、`GET /api/posts/search` | 成泽楷 | 鲍奕涵页面壳、成泽楷组件 |
| 点赞、关注、评论接口 | 成泽楷 | 成泽楷组件 |
| `POST /api/reports` | 陈瀚锐 | 鲍奕涵页面壳 |
| `GET /api/admin/overview`、`GET /api/admin/users`、`PATCH /api/admin/users/{id}`、`GET /api/admin/posts`、`PATCH /api/admin/posts/{id}/audit`、`GET /api/admin/reports`、`PATCH /api/admin/reports/{id}` | 陈瀚锐 | 陈瀚锐 |

每个接口负责人对 DTO、Service、OpenAPI 段落和模块测试负责。鲍奕涵不代写全部接口，而是在每天联调前检查 OpenAPI 是否与已合并实现一致。

### 7.2 前端归属

| 路径或组件 | 主负责人 | 不能越过的边界 |
|---|---|---|
| `src/core`、`src/router`、`src/stores/auth`、`src/api/client` | 汪晨烨 | 全项目仅一个 Axios 实例、一个认证状态源、一套路由守卫 |
| `src/views/auth`、`home`、`plans`、`checkins`、`statistics` | 汪晨烨 | 用户端只展示并提交领域 API 数据，不在页面中重复统计或权限规则 |
| `src/views/community/DiscoverView.vue`、`PostDetailView.vue` | 鲍奕涵 | 负责布局、加载/空态、分页和 API 组合；不复制互动业务逻辑 |
| `src/components/upload/CheckInImageUpload.vue` | 鲍奕涵 | 仅服务打卡图片，统一文件校验和上传状态 |
| `src/components/community/PostComposer.vue`、`PostCard.vue`、`SearchFilter.vue`、`InteractionBar.vue`、`FollowButton.vue`、`LikeButton.vue`、`CommentList.vue` | 成泽楷 | 通过 Props/Emit 与页面壳交互；请求经 `src/api/community.ts`，不得在组件里散落 Axios |
| `src/views/admin/**` | 陈瀚锐 | 后台布局、筛选、状态标签和处理备注在此维护；仍由后端二次权限校验 |

汪晨烨原来的“参与动态卡片”调整为用户端 UI 规范复核，不再承担 `PostCard` 的代码所有权。这样社区页面、卡片和互动组件不会被三个人同时修改。

## 8. 报告、测试和证据的唯一版本

### 8.1 报告主笔边界

| 报告位置 | 主笔 | 技术复核 | 最终编辑 |
|---|---|---|---|
| 摘要、关键词、1.1、4.1-4.2、4.4、7.1 | 陈亦雷 | 各模块负责人提供真实结论 | 陈亦雷 |
| 1.2-1.3、4.3、3.2 社区段、5.4 社区互动段、6.1-6.2 | 成泽楷 | 陈亦雷复核 ER/约束；鲍奕涵复核页面壳 | 陈亦雷 |
| 第 2 章、5.1-5.3 | 汪晨烨 | 陈亦雷复核账户/计划；鲍奕涵复核打卡统计 | 陈亦雷 |
| 3.1、3.2 打卡保存段、5.4 发现/详情页说明、API/错误码/部署附录 | 鲍奕涵 | 成泽楷复核社区接口；陈瀚锐复核后台截图 | 陈亦雷 |
| 3.3、5.4 管理后台段、6.3 | 陈瀚锐 | 陈亦雷复核权限；鲍奕涵复核部署和运行证据 | 陈亦雷 |

同一章节只有一个可编辑主文件。截图必须标注版本标签、测试账号和日期，并来自已经合并的版本；禁止每个人各自导出 PDF 后再拼接。

### 8.2 测试责任

| 用例 | 模块主测 | 独立验收 | 最少证据 |
|---|---|---|---|
| TC-01 注册与登录 | 陈亦雷 | 陈瀚锐 | 重复邮箱、错误密码、退出后 401 |
| TC-02 学习计划 | 陈亦雷 | 陈瀚锐 | 创建/编辑/暂停/完成、非法日期、他人计划不可访问 |
| TC-03 每日打卡与统计 | 鲍奕涵 | 陈瀚锐 | 重复提交不新增、时长/笔记读取、统计同步变化 |
| TC-04 打卡发布动态 | 成泽楷 | 鲍奕涵 | 只打卡没有 Post；发布关联；删除 Post 后 CheckIn 仍在 |
| TC-05 社区互动 | 成泽楷 | 陈瀚锐 | 重复点赞/关注、取消、空评论 |
| TC-06 内容可见性 | 成泽楷 | 陈瀚锐 | 私密、隐藏、受限作者内容不出现在公开入口 |
| TC-07 举报处理 | 陈瀚锐 | 鲍奕涵 | 队列、处理人、时间、状态、备注可追溯 |
| TC-08 管理员权限 | 陈瀚锐 | 鲍奕涵 | 普通用户 403、管理员成功、前后端双重校验 |

陈瀚锐负责测试模板、缺陷台账和最终回归，但不自验自己写的管理端；鲍奕涵独立执行 TC-07、TC-08 并保留结果。

## 9. GitHub Project、PR 和变更流程

只使用一个 GitHub Project，不能同时维护个人 Excel、群公告和另一块看板。

| 看板内容 | 责任人 |
|---|---|
| 需求编号、优先级、迭代和完成定义 | 成泽楷 |
| Bug 状态、严重程度、负责人、修复提交和复测结论 | 陈瀚锐 |
| API 联调、跨域、部署验证标签和结果 | 鲍奕涵 |
| `main`、release、安全和公共契约决策 | 陈亦雷 |

分支建议固定为 `feat/chen-auth-plan`、`feat/wang-frontend`、`feat/bao-checkin-engineering`、`feat/cheng-community`、`feat/chenhan-admin-test`。`main` 只保留可启动、可演示的版本。

每个 PR 必须说明：改动内容、涉及接口/表/页面、字段或状态影响、测试方式、是否需迁移和报告更新。普通 PR 至少需要一位非作者审查；安全、跨表、破坏性或公共契约变更还需要陈亦雷审查。

任何字段、状态或接口变化严格按以下顺序：

```text
提出 Issue
  -> 写明数据库/API/UI/测试/报告影响
  -> 更新唯一真源文件
  -> 同一 PR 更新代码、迁移和模块测试
  -> 非作者验收
  -> 合并并同步报告
```

## 10. 六天安排和每日闸门

| 天数 | 当天工作重点 | 通过条件 |
|---|---|---|
| 第 1 天 | 冻结五份唯一真源；后端/前端骨架；ER 初版；测试和看板模板 | 五份文件、表主责、接口主责、报告主责、分支和 P0/P1/P2 全部确认 |
| 第 2 天 | 注册登录、个人资料、计划；用户端认证和计划页；公共异常与校验；社区/后台 DTO 和页面壳 | 可注册登录；可创建、查询、编辑、暂停和完成计划 |
| 第 3 天 | 打卡、统计、上传；日历和统计页；空库迁移验证；跨模块关系复核 | `计划 -> 打卡 -> 统计` 跑通；重复打卡不新增；第 3 天数据库审核完成 |
| 第 4 天 | 先保存打卡、再发布动态；发现/详情；搜索、点赞、关注、评论；可见性 | 只打卡不生成动态；一条打卡至多一条动态；隐藏/私密内容不可见 |
| 第 5 天 | 举报、审核、用户管理；后台页面；部署、健康检查和浏览器验证 | 管理员能处理举报；普通用户管理接口 403；干净环境按说明启动 |
| 第 6 天 | 全量联调、回归、修复、证据归档、报告统稿和演示 | TC-01 至 TC-08 有结论；五人均具备代码/报告/测试/协作证据 |

每天固定三次同步：开始前说产出/目标/阻塞；中途只讨论字段、接口、状态和依赖；结束前展示可运行结果。超过 30 分钟的阻塞必须进看板，不能只在聊天中口头解决。

## 11. 降级顺序和首次会议清单

范围不足时按此顺序降级：通知 P2、复杂搜索排序、批量管理、高级图表、评论举报。不得降级账户、计划、打卡、统计、服务端权限、动态发布最小闭环、举报处理最小闭环和 TC-08 的 403 验证。任何降级都要记录原因、影响和替代方案，报告中不得写成已完成。

第一次会议结束前，五人共同确认：

- [ ] 五份唯一真源文件已有 v1.0；
- [ ] `CheckIn.completed`、`durationMinutes`，以及 `Post.status`、`Report.targetType` 已按本文冻结；
- [ ] 成泽楷明确负责 `POST /api/posts`，鲍奕涵的打卡接口不会自动创建动态；
- [ ] 图片只绑定打卡，通知为 P2，不占本轮开发资源；
- [ ] 陈亦雷已排好第 1 天和第 3 天数据库审核窗口；
- [ ] 一个 GitHub Project 已建立，并按第 9 节分配维护人；
- [ ] 每个人都创建了自己的 Issue、分支、报告段落和至少一条测试任务；
- [ ] 约定单一可编辑主报告文件与最终截图命名规则。

完成以上清单后，五人可以按原始分工并行开工，同时不会出现“同一个接口两个人写、同一张表被不同人改、报告和实际代码各说各话”的情况。
