# StudyFlow · 前端（用户端）

StudyFlow 的学习计划与每日打卡 Web 应用前端。本目录是**汪晨烨**负责的用户端部分，基于
Vue 3 + Vite + TypeScript + Element Plus + Pinia + Vue Router + Axios。

## 快速开始

```bash
npm install
npm run dev      # 本地开发，默认走 mock，打开 http://localhost:5173
npm run build    # 类型检查 + 生产构建
```

**演示账号**：`demo@studyflow.com` / `123456`（mock 内置，后端就绪后由真实账号替代）。

## 连接真实后端

后端（Spring Boot）就绪后，只需修改 `.env`：

```ini
VITE_USE_MOCK=false
```

`vite.config.ts` 会自动把 `/api` 代理到 `http://localhost:8080`。前端代码无需改动。

## 目录结构与归属

遵循《五人分工统一执行方案》第 7.2 节的前端归属，**全项目仅一个 Axios 实例、一个认证状态源、一套路由守卫**：

```
src/
├── core/constants.ts        # 全项目唯一的状态枚举与中文文案映射
├── api/
│   ├── client.ts            # 唯一 Axios 实例 + 统一响应解包 + 401 登出
│   ├── types.ts             # 后端契约类型（字段名严格按 4.2 字典）
│   ├── auth.ts / plans.ts / checkins.ts / statistics.ts / uploads.ts
├── stores/auth.ts           # 唯一认证状态源
├── router/index.ts          # 路由 + 全局守卫
├── layout/MainLayout.vue    # 布局与侧边菜单
└── views/
    ├── auth/                # 登录 / 注册
    ├── home/                # 首页
    ├── plans/               # 计划列表 / 新建编辑
    ├── checkins/            # 每日打卡
    └── statistics/          # 统计 + 日历
mock/index.ts                # 本地 mock（Vite 插件，连接后端后即可删除/停用）
```

## 契约与假设（需与后端确认）

前端严格按照统一契约实现，以下几条为**前端当前假设**，后端联调时请核对：

1. `POST /api/auth/register` 返回 `{ token, user }`（注册即登录）。若后端注册不返回 token，
   需在 `RegisterView.vue` 里改为注册后跳转登录页。
2. `GET /api/statistics` 需要传 `planId` 才能得到有意义的 `completionRate` / `streakDays`
   （文档 5.4 的完成率分母是「计划的有效日期数」）。页面默认选择第一个计划。
3. `GET /api/plans/{id}/check-ins` 返回数组（非分页），用于日历标记；`page` 从 1 起的分页
   仅用于 `GET /api/plans`。
4. `POST /api/uploads` 返回 `{ fileId, url }`；打卡图片的 `imageUrl` 必须来自该结果。

## 已验证的测试点（TC-01 ~ TC-03 页面侧）

- 注册 / 登录 / 登出，重复邮箱返回 409。
- 计划创建 / 编辑 / 删除，非法日期（结束早于开始）被拦截。
- 打卡同一天重复提交为更新不新增（`UNIQUE(user_id, plan_id, check_date)`）。
- 越权访问他人计划返回 404「不存在或不可见」，前端不泄露资源是否存在。
- 统计口径：累计时长 / 完成率 / 连续天数由后端（mock）统一计算，前端仅展示。
