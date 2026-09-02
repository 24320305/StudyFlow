<template>
  <div class="interaction-bar">
    <LikeButton
      :post-id="post.id"
      :liked="post.likedByCurrentUser"
      :like-count="post.likeCount"
      @changed="onLikeChanged"
    />
    <el-button link type="info" :icon="ChatDotRound" @click.stop="emit('comments')">
      {{ post.commentCount }}
    </el-button>
    <el-button link type="info" :icon="View" @click.stop="emit('detail')">详情</el-button>
  </div>
</template>

<script setup lang="ts">
import { ChatDotRound, View } from '@element-plus/icons-vue'
import LikeButton from './LikeButton.vue'
import type { CommunityPost } from '@/api/types'

defineProps<{
  post: CommunityPost
}>()

const emit = defineEmits<{
  comments: []
  detail: []
  likeChanged: [liked: boolean, likeCount: number]
}>()

function onLikeChanged(liked: boolean, likeCount: number) {
  emit('likeChanged', liked, likeCount)
}
</script>

<style scoped>
.interaction-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
