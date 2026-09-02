<template>
  <el-card shadow="never" class="post-card">
    <div class="post-head">
      <div class="author">
        <el-avatar :size="36" :src="post.author.avatarUrl || undefined">
          {{ post.author.nickname.charAt(0).toUpperCase() }}
        </el-avatar>
        <div>
          <div class="author-name">{{ post.author.nickname }}</div>
          <div class="post-time">{{ formatTime(post.createdAt) }}</div>
        </div>
      </div>
      <div class="head-actions">
        <el-tag size="small" :type="post.visibility === 'PUBLIC' ? 'success' : 'warning'" effect="plain">
          {{ POST_VISIBILITY_LABEL[post.visibility] }}
        </el-tag>
        <el-tag v-if="post.status !== 'VISIBLE'" size="small" type="info" effect="plain">
          {{ POST_STATUS_LABEL[post.status] }}
        </el-tag>
        <FollowButton :user-id="post.author.id" :is-self="isMine" />
      </div>
    </div>

    <p class="content">{{ post.content }}</p>

    <div class="post-foot">
      <InteractionBar
        :post="post"
        @detail="emit('detail', post)"
        @comments="emit('comments', post)"
        @like-changed="onLikeChanged"
      />
      <div v-if="isMine" class="owner-actions">
        <el-button link type="primary" :icon="Edit" @click.stop="emit('edit', post)">编辑</el-button>
        <el-button link type="danger" :icon="Delete" @click.stop="emit('delete', post)">删除</el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { Delete, Edit } from '@element-plus/icons-vue'
import FollowButton from './FollowButton.vue'
import InteractionBar from './InteractionBar.vue'
import type { CommunityPost } from '@/api/types'
import { POST_STATUS_LABEL, POST_VISIBILITY_LABEL } from '@/core/constants'

const props = defineProps<{
  post: CommunityPost
  currentUserId?: number
}>()

const emit = defineEmits<{
  detail: [post: CommunityPost]
  comments: [post: CommunityPost]
  edit: [post: CommunityPost]
  delete: [post: CommunityPost]
  changed: [post: CommunityPost]
}>()

const isMine = props.currentUserId === props.post.author.id

function formatTime(value: string): string {
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

function onLikeChanged(liked: boolean, likeCount: number) {
  emit('changed', { ...props.post, likedByCurrentUser: liked, likeCount })
}
</script>

<style scoped>
.post-card {
  margin-bottom: 12px;
}

.post-head,
.post-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.author,
.head-actions,
.owner-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-name {
  font-weight: 600;
  color: #303133;
}

.post-time {
  margin-top: 2px;
  color: #909399;
  font-size: 12px;
}

.content {
  margin: 16px 0;
  color: #303133;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
