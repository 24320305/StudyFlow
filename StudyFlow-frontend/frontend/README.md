# StudyFlow · 前端（用户端）

StudyFlow 的学习计划与每日打卡 Web 应用前端。本目录是**汪晨烨**负责的用户端部分，基于
Vue 3 + Vite + TypeScript + Element Plus + Pinia + Vue Router + Axios。

## 快速开始

```bash
npm install
npm run dev      # 本地开发，默认走 mock，打开 http://localhost:5173
npm run build    # 类型检查 + 生产构建
```

**演示账号**（仅 mock 模式）：`demo@studyflow.com` / `12345678`。

## 连接真实后端

项目默认连接陈亦雷的 Spring Boot 后端。确保后端已在 `http://localhost:8080` 运行，然后启动前端：

```ini
VITE_USE_MOCK=false
```

`vite.config.ts` 会自动把 `/api` 代理到 `http://localhost:8080`。后端不可用时，可以把该值改为 `true`，切换至本地 mock。

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

1. 陈亦雷的认证响应使用 `{ accessToken, tokenType, expiresAt, user }`；注册成功即登录。
2. 计划列表通过后端分页接口读取；当前后端没有 `status` 查询参数，前端会在最多 100 条计划的本地结果中筛选和分页。
3. `GET /api/plans/{id}/check-ins` 返回数组（非分页），用于打卡列表和统计日历。
4. 当前后端未实现 `/api/statistics` 与 `/api/uploads`。真实后端模式下，统计页根据计划和打卡记录计算；图片上传控件会禁用，文字打卡可正常保存。

## 已验证的测试点（TC-01 ~ TC-03 页面侧）

- 注册 / 登录 / 登出，重复邮箱返回 409。
- 计划创建 / 编辑 / 删除，非法日期（结束早于开始）被拦截。
- 打卡同一天重复提交为更新不新增（`UNIQUE(user_id, plan_id, check_date)`）。
- 越权访问他人计划返回 404「不存在或不可见」，前端不泄露资源是否存在。
- 统计口径：累计时长 / 完成率 / 连续天数由后端（mock）统一计算，前端仅展示。
