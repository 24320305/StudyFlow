/*
表名：post

作用：
保存用户发布的学习动态

业务流程：
用户先完成学习打卡(check_in)
然后选择发布动态(post)

规则：
一条动态必须属于一个用户
动态状态使用 status
可见性使用 visibility
*/

CREATE TABLE post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- 动态编号

    user_id BIGINT NOT NULL, -- 发布动态的用户ID

    checkin_id BIGINT, -- 关联的打卡记录ID

    content VARCHAR(500) NOT NULL, -- 动态文字内容

    visibility VARCHAR(20) NOT NULL, -- 动态可见范围：PUBLIC/PRIVATE

    status VARCHAR(20) NOT NULL, -- 动态状态：VISIBLE/HIDDEN/DELETED

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 发布时间
);

/*
表名：comment

作用：
保存用户对动态的评论

业务规则：
一条评论属于一条动态
评论内容不能为空
评论状态统一使用 status
*/

CREATE TABLE comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- 评论编号

    user_id BIGINT NOT NULL, -- 评论用户ID

    post_id BIGINT NOT NULL, -- 被评论的动态ID

    content VARCHAR(500) NOT NULL, -- 评论内容

    status VARCHAR(20) NOT NULL, -- 评论状态：VISIBLE/HIDDEN/DELETED

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 评论时间
);

/*
表名：follow

作用：
保存用户之间的关注关系

例如：
张三关注李四

follower_id = 张三
following_id = 李四

规则：
同一个用户不能重复关注同一个人
*/

CREATE TABLE follow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- 关注记录编号

    follower_id BIGINT NOT NULL, -- 发起关注的人

    following_id BIGINT NOT NULL, -- 被关注的人

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 关注时间

    -- 防止重复关注
    UNIQUE(follower_id, following_id)
);
/*
社区点赞表
作用：
记录哪个用户点赞了哪条动态

规则：
同一个用户不能重复点赞同一条动态
*/

CREATE TABLE post_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- 点赞记录ID

    user_id BIGINT NOT NULL, -- 点赞的人

    post_id BIGINT NOT NULL, -- 被点赞的动态

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间

    UNIQUE(user_id, post_id)
);