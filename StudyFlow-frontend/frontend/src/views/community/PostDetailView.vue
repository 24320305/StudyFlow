<template>
  <div class="sf-page detail-page">
    <el-button class="back-button" :icon="ArrowLeft" @click="$router.push('/community')">
      返回社区
    </el-button>

    <el-skeleton v-if="loading" :rows="6" animated />
    <template v-else-if="post">
      <PostCard
        :post="post"
        :current-user-id="auth.user?.id"
        @changed="post = $event"
        @delete="removePost"
        @detail="noop"
        @comments="noop"
      />
      <el-card shadow="never">
        <template #header>
          <span>评论</span>
        </template>
        <CommentList :post-id="post.id" :current-user-id="auth.user?.id" @changed="updateCommentCount" />
      </el-card>
    </template>
    <el-empty v-else description="动态不存在或不可见" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import CommentList from '@/components/community/CommentList.vue'
import PostCard from '@/components/community/PostCard.vue'
import { deletePost, getPost } from '@/api/community'
import type { CommunityPost } from '@/api/types'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const post = ref<CommunityPost | null>(null)
const loading = ref(false)

async function reload() {
  const id = Number(route.params.id)
  if (!Number.isFinite(id)) return
  loading.value = true
  try {
    post.value = await getPost(id)
  } finally {
    loading.value = false
  }
}

function updateCommentCount(count: number) {
  if (!post.value) return
  post.value = { ...post.value, commentCount: count }
}

async function removePost(target: CommunityPost) {
  await ElMessageBox.confirm('确定删除这条动态吗？删除后不会影响原打卡记录。', '删除动态', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
  await deletePost(target.id)
  ElMessage.success('动态已删除')
  router.push('/community')
}

function noop() {
  return undefined
}

onMounted(reload)
</script>

<style scoped>
.detail-page {
  max-width: 920px;
  margin: 0 auto;
}

.back-button {
  margin-bottom: 14px;
}
</style>
