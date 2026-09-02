# 社区模块代码结构

负责人：成泽楷


community

├── entity
│
├── dto
│
├── repository
│
├── service
│
├── controller
│
└── exception


## entity

对应数据库表：

Post
Comment
Follow
PostLike


## repository

负责数据库访问。


## service

负责业务逻辑：

- 发布动态
- 查询动态
- 点赞
- 评论
- 关注


## controller

负责接口：

POST /api/posts

GET /api/posts

POST /api/posts/{id}/like

POST /api/posts/{id}/comments

POST /api/users/{id}/follow