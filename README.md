# StudyFlow

## 项目简介
StudyFlow 是一个帮助在校学生制定学习计划、坚持每日打卡、并通过学习社区互相监督激励的 Web 应用。针对"计划容易半途而废、缺乏同伴监督、学习过程无沉淀"的痛点，提供计划管理、每日打卡、学习统计与动态分享四项核心能力。

## 核心功能
- **用户**：注册登录、个人资料管理（角色 USER / ADMIN）。
- **学习计划**：创建计划（起止日期、每日目标时长），支持进行中 / 已暂停 / 已完成状态流转。
- **每日打卡**：记录学习时长、完成情况、笔记与图片，同一天重复提交为更新而非新增。
- **学习统计**：累计时长、完成率、连续打卡天数，以及日历视图。
- **学习社区**：打卡动态、评论、举报与审核。
- **后台管理**：用户与内容管理。

## 技术栈
- 前端：Vue 3 + Vite + TypeScript + Element Plus + Pinia + Vue Router + Axios。
- 后端：Spring Boot + MyBatis + MySQL（JWT 认证、统一响应、接口级校验）。
- 协作：Git / GitHub；统一 camelCase 字段，统一响应结构 `{ code, message, data, requestId }`。

## 分工
- 汪晨烨：用户端前端（页面 + 核心层 + mock + 测试）
- 陈亦雷：后端认证与计划
- 鲍奕涵：后端打卡 / 统计 / 上传
- 成泽楷：社区模块
- 陈瀚锐：后台管理

## 进度
用户端前端已完成（页面、核心层、本地 mock、接口验证），当前处于前后端联调阶段。

## 前端本地运行
```bash
cd frontend
npm install
npm run dev     # 默认走 mock，http://localhost:5173
```
演示账号 `demo@studyflow.com` / `123456`；后端就绪后改 `.env` 中 `VITE_USE_MOCK=false` 即可直连。
