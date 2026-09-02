# StudyFlow 社区模块数据字典

负责人：成泽楷

模块范围：
- 动态发布
- 评论
- 点赞
- 关注


---

# 1. post（动态表）

## 表说明

保存用户发布的学习动态。

业务流程：

用户完成学习打卡后，可以选择发布学习动态。

一条动态最多关联一条打卡记录。


## 字段说明

|字段名|类型|是否为空|说明|
|-|-|-|-|
|id|BIGINT|否|动态唯一编号|
|user_id|BIGINT|否|发布动态的用户ID|
|checkin_id|BIGINT|是|关联的学习打卡ID|
|content|VARCHAR(500)|否|动态文字内容|
|visibility|VARCHAR(20)|否|动态可见范围|
|status|VARCHAR(20)|否|动态状态|
|created_at|TIMESTAMP|否|创建时间|


## 字段规则

visibility：

- PUBLIC：公开
- PRIVATE：私密


status：

- VISIBLE：正常显示
- HIDDEN：隐藏
- DELETED：删除


---

# 2. comment（评论表）

## 表说明

保存用户对学习动态的评论。


## 字段说明

|字段名|类型|是否为空|说明|
|-|-|-|-|
|id|BIGINT|否|评论编号|
|user_id|BIGINT|否|评论用户ID|
|post_id|BIGINT|否|所属动态ID|
|content|VARCHAR(500)|否|评论内容|
|status|VARCHAR(20)|否|评论状态|
|created_at|TIMESTAMP|否|评论时间|


## 字段规则

content不能为空。

status：

- VISIBLE：正常评论
- HIDDEN：隐藏
- DELETED：删除


---

# 3. follow（关注表）

## 表说明

保存用户之间的关注关系。


例如：

用户A关注用户B：

follower_id = 用户A

following_id = 用户B


## 字段说明

|字段名|类型|是否为空|说明|
|-|-|-|-|
|id|BIGINT|否|关注记录编号|
|follower_id|BIGINT|否|关注者ID|
|following_id|BIGINT|否|被关注者ID|
|created_at|TIMESTAMP|否|关注时间|


## 约束

同一个用户不能重复关注同一个用户。


UNIQUE：

(follower_id, following_id)


---

# 4. post_like（点赞表）

## 表说明

保存用户点赞动态的记录。


## 字段说明

|字段名|类型|是否为空|说明|
|-|-|-|-|
|id|BIGINT|否|点赞记录编号|
|user_id|BIGINT|否|点赞用户ID|
|post_id|BIGINT|否|被点赞动态ID|
|created_at|TIMESTAMP|否|点赞时间|


## 约束

同一个用户对同一条动态只能点赞一次。


UNIQUE：

(user_id, post_id)


---

# 5. 社区模块关系说明

User：

一个用户可以发布多条动态。


Post：

一条动态可以拥有多条评论。


User 和 Follow：

用户之间存在关注关系。


User 和 PostLike：

用户可以点赞多条动态。