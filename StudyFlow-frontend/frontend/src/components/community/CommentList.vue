<template>
  <div class="comment-list">
    <div class="comment-editor">
      <el-input
        v-model="content"
        type="textarea"
        :rows="2"
        maxlength="500"
        show-word-limit
        placeholder="写下你的评论"
      />
      <el-button type="primary" :icon="Position" :loading="submitting" @click="submit">
        发表评论
      </el-button>
    </div>

    <el-empty v-if="!loading && comments.length === 0" description="暂无评论" />
    <div v-else v-loading="loading" class="comments">
      <div v-for="item in comments" :key="item.id" class="comment-item">
        <el-avatar :size="30" :src="item.author.avatarUrl || undefined">
          {{ item.author.nickname.charAt(0).toUpperCase() }}
        </el-avatar>
        <div class="comment-body">
          <div class="comment-meta">
            <span class="comment-author">{{ item.author.nickname }}</span>
            <span>{{ formatTime(item.createdAt) }}</span>
            <el-button
              v-if="item.author.id === currentUserId"
              link
              type="danger"
              size="small"
              @click="remove(item)"
            >
              删除
            </el-button>
          </div>
          <div class="comment-content">{{ item.content }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Position } from '@element-plus/icons-vue'
import { addComment, deleteComment, listComments } from '@/api/community'
import type { CommunityComment } from '@/api/types'

const props = defineProps<{
  postId: number
  currentUserId?: number
}>()

const emit = defineEmits<{
  changed: [count: number]
}>()

const comments = ref<CommunityComment[]>([])
const content = ref('')
const loading = ref(false)
const submitting = ref(false)

async function reload() {
  loading.value = true
  try {
    comments.value = await listComments(props.postId)
    emit('changed', comments.value.length)
  } finally {
    loading.value = false
  }
}

async function submit() {
  const text = content.value.trim()
  if (!text) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  submitting.value = true
  try {
    const item = await addComment(props.postId, { content: text })
    comments.value.push(item)
    content.value = ''
    emit('changed', comments.value.length)
    ElMessage.success('评论已发布')
  } finally {
    submitting.value = false
  }
}

async function remove(item: CommunityComment) {
  await ElMessageBox.confirm('确定删除这条评论吗？', '删除评论', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
  await deleteComment(item.id)
  comments.value = comments.value.filter((comment) => comment.id !== item.id)
  emit('changed', comments.value.length)
}

function formatTime(value: string): string {
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

onMounted(reload)
</script>

<style scoped>
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.comment-editor {
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: flex-start;
  gap: 10px;
}

.comments {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.comment-body {
  min-width: 0;
  flex: 1;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #909399;
  font-size: 12px;
}

.comment-author {
  color: #303133;
  font-weight: 600;
}

.comment-content {
  margin-top: 4px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
