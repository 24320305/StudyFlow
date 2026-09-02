# 社区模块代码结构

负责人：成泽楷

## 后端路径

```text
StudyFlow-backend/src/main/java/com/studyflow/backend/community
├── Post.java / Comment.java / FollowRelation.java / PostLike.java
├── CreatePostRequest.java / UpdatePostRequest.java / CreateCommentRequest.java
├── PostResponse.java / CommentResponse.java / LikeStateResponse.java / FollowStateResponse.java
├── PostRepository.java / CommentRepository.java / FollowRepository.java / PostLikeRepository.java
├── CommunityService.java
├── CommunityController.java / CommentController.java / FollowController.java
└── PostStatus.java / PostVisibility.java / CommentStatus.java
```

## 前端路径

```text
StudyFlow-frontend/frontend/src
├── api/community.ts
├── views/community/DiscoverView.vue
├── views/community/PostDetailView.vue
└── components/community
    ├── PostComposer.vue
    ├── PostCard.vue
    ├── SearchFilter.vue
    ├── InteractionBar.vue
    ├── FollowButton.vue
    ├── LikeButton.vue
    └── CommentList.vue
```

## entity

对应数据库表：Post、Comment、FollowRelation、PostLike。

## repository

负责社区模块数据库访问、分页查询、幂等判断和软删除查询。

## service

负责业务逻辑：

- 发布动态
- 查询动态
- 点赞和取消点赞
- 评论和删除评论
- 关注和取消关注
- 公开可见性、账号受限和资源归属校验

## controller

负责接口：

- `POST /api/posts`
- `GET /api/posts`
- `GET /api/posts/search`
- `GET /api/posts/mine`
- `GET/PATCH/DELETE /api/posts/{id}`
- `POST/DELETE /api/posts/{id}/likes`
- `POST/GET /api/posts/{id}/comments`
- `DELETE /api/comments/{id}`
- `POST/DELETE /api/users/{id}/follow`
