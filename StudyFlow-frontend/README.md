# StudyFlow

学习计划 + 每日打卡 + 学习社区 的 Web 应用（五人协作课程项目）。

## 目录

```
StudyFlow/
├── frontend/   # 用户端前端（汪晨烨负责）：Vue 3 + Vite + Element Plus
└── backend/    # 后端（陈亦雷/鲍奕涵/成泽楷/陈瀚锐 各自模块）——待加入
```

前端独立运行说明见 [frontend/README.md](frontend/README.md)。

## 统一约定（摘要）

- 字段命名：Java/JSON/Vue 用 `camelCase`，数据库用 `snake_case`，物理表 `sys_user`。
- 打卡与动态分离：保存打卡不自动创建动态，删除动态不删除打卡。
- 通知功能 P2 预留，本轮不实现。
- 统一响应：`{ code, message, data, requestId }`。
