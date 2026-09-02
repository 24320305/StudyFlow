# StudyFlow 需求-测试追踪表

版本：v1.0  
需求编号、优先级、迭代和完成定义维护人：成泽楷

| 需求编号 | 优先级 | 迭代 | 负责人 | 完成定义 | 测试用例 | 当前证据 |
|---|---|---|---|---|---|---|
| CMT-01 | P0 | 第 4 天 | 成泽楷 | 保存打卡不会自动生成动态；发布必须关联自己的已完成打卡 | TC-04 | `CommunityIntegrationTest.publishesOnePostPerCompletedCheckInAndKeepsTheCheckInAfterPostDeletion` |
| CMT-02 | P0 | 第 4 天 | 成泽楷 | 同一 `checkInId` 重复发布不会产生第二条动态 | TC-04 | `uk_post_checkin` 约束和重复发布 200 测试 |
| CMT-03 | P0 | 第 4 天 | 成泽楷 | 删除动态只改变 `post.status`，CheckIn 仍存在 | TC-04 | 删除动态后检查 `checkInRepository.existsById` |
| CMT-04 | P0 | 第 4 天 | 成泽楷 | 点赞、取消点赞、关注、取消关注可用，重复提交幂等 | TC-05 | `makesLikeAndFollowRequestsIdempotentAndRejectsBlankComments` |
| CMT-05 | P0 | 第 4 天 | 成泽楷 | 空评论拒绝，正常评论可发布和展示 | TC-05 | `VALIDATION_FAILED` 断言和评论接口测试 |
| CMT-06 | P0 | 第 4 天 | 成泽楷 | 私密、隐藏、删除、受限作者内容不进入公开入口 | TC-06 | 私密动态详情 404、搜索 total=0、受限账号写操作 403 |
| CMT-07 | P1 | 第 4 天 | 成泽楷 | 社区发现页、详情页、发布框、动态卡片和互动组件可联调 | 页面侧检查 | `src/views/community`、`src/components/community`、`src/api/community.ts` |

## 后端测试日志

```text
CommunityIntegrationTest: Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

## 已知限制

- 后端当前已完成社区领域接口；举报和后台治理由陈瀚锐负责。
- 图片仍来自打卡 `imageUrl`，社区模块不单独上传图片。
- 前端关注按钮当前只维护本次页面交互状态，后端响应未提供“当前是否已关注该作者”的初始字段。
