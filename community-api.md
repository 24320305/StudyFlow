# StudyFlow 社区模块接口设计

负责人：成泽楷

版本：v1.0

模块范围：

- 动态发布
- 动态查询
- 搜索
- 点赞
- 评论
- 关注


---

# 1. 动态发布

## 接口

POST /api/posts


## 功能

用户发布学习动态。


## 请求参数

```json
{
  "checkinId": 1,
  "content": "今天学习Java两小时",
  "visibility": "PUBLIC"
}