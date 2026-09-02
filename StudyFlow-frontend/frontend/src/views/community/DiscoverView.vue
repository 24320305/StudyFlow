<template>
  <div class="sf-page community-page">
    <el-row :gutter="16">
      <el-col :span="8">
        <PostComposer :checkins="publishableCheckins" @published="onPublished" />
        <el-card shadow="never">
          <template #header>
            <span>可发布打卡</span>
          </template>
          <el-empty
            v-if="!loadingCheckins && publishableCheckins.length === 0"
            description="暂无已完成打卡"
          />
          <el-table v-else :data="publishableCheckins" size="small" max-height="320" v-loading="loadingCheckins">
            <el-table-column prop="checkDate" label="日期" width="104" />
            <el-table-column label="时长" width="86">
              <template #default="{ row }">{{ row.durationMinutes }} 分钟</template>
            </el-table-column>
            <el-table-column prop="note" label="笔记" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="never" class="feed-panel">
          <template #header>
            <div class="feed-head">
              <el-tabs v-model="scope" class="scope-tabs" @tab-change="reloadPosts">
                <el-tab-pane label="发现" name="public" />
                <el-tab-pane label="我的动态" name="mine" />
              </el-tabs>
              <SearchFilter v-if="scope === 'public'" v-model="keyword" @search="reloadFromFirstPage" />
            </div>
          </template>

          <el-empty v-if="!loadingPosts && posts.length === 0" description="暂无动态" />
          <div v-else v-loading="loadingPosts" class="feed-list">
            <PostCard
              v-for="post in posts"
              :key="post.id"
              :post="post"
              :current-user-id="auth.user?.id"
              @detail="goDetail"
              @comments="openComments"
              @edit="openEdit"
              @delete="removePost"
              @changed="replacePost"
            />
          </div>

          <div class="pager">
            <el-pagination
              v-model:current-page="page"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[5, 10, 20]"
              layout="total, sizes, prev, pager, next"
              @current-change="reloadPosts"
              @size-change="reloadFromFirstPage"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-drawer v-model="commentDrawer.visible" title="动态评论" size="42%">
      <CommentList
        v-if="commentDrawer.post"
        :post-id="commentDrawer.post.id"
        :current-user-id="auth.user?.id"
        @changed="onCommentCountChanged"
      />
    </el-drawer>

    <el-dialog v-model="editDialog.visible" title="编辑动态" width="520px">
      <el-form label-width="80px">
        <el-form-item label="可见范围">
          <el-segmented v-model="editDialog.visibility" :options="visibilityOptions" />
        </el-form-item>
        <el-form-item label="动态内容">
          <el-input
            v-model="editDialog.content"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="editDialog.saving" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import CommentList from '@/components/community/CommentList.vue'
import PostCard from '@/components/community/PostCard.vue'
import PostComposer from '@/components/community/PostComposer.vue'
import SearchFilter from '@/components/community/SearchFilter.vue'
import {
  deletePost,
  listMyPosts,
  listPosts,
  updatePost,
} from '@/api/community'
import { listCheckins } from '@/api/checkins'
import { listPlans } from '@/api/plans'
import type { CheckIn, CommunityPost, PostVisibility } from '@/api/types'
import { POST_VISIBILITY_LABEL } from '@/core/constants'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const scope = ref<'public' | 'mine'>('public')
const keyword = ref('')
const posts = ref<CommunityPost[]>([])
const publishableCheckins = ref<CheckIn[]>([])
const loadingPosts = ref(false)
const loadingCheckins = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const commentDrawer = reactive({
  visible: false,
  post: null as CommunityPost | null,
})

const editDialog = reactive({
  visible: false,
  saving: false,
  postId: null as number | null,
  content: '',
  visibility: 'PUBLIC' as PostVisibility,
})

const visibilityOptions = (Object.keys(POST_VISIBILITY_LABEL) as PostVisibility[]).map((value) => ({
  label: POST_VISIBILITY_LABEL[value],
  value,
}))

async function loadPublishableCheckins() {
  loadingCheckins.value = true
  try {
    const planPage = await listPlans({ page: 1, pageSize: 100 })
    const lists = await Promise.all(planPage.items.map((plan) => listCheckins(plan.id)))
    publishableCheckins.value = lists
      .flat()
      .filter((item) => item.completed)
      .sort((a, b) => b.checkDate.localeCompare(a.checkDate))
  } finally {
    loadingCheckins.value = false
  }
}

async function reloadPosts() {
  loadingPosts.value = true
  try {
    const res =
      scope.value === 'mine'
        ? await listMyPosts({ page: page.value, pageSize: pageSize.value })
        : await listPosts({
            page: page.value,
            pageSize: pageSize.value,
            ...(keyword.value ? { keyword: keyword.value } : {}),
          })
    posts.value = res.items
    total.value = res.total
  } finally {
    loadingPosts.value = false
  }
}

function reloadFromFirstPage() {
  page.value = 1
  reloadPosts()
}

function onPublished(post: CommunityPost) {
  scope.value = post.visibility === 'PRIVATE' ? 'mine' : 'public'
  reloadFromFirstPage()
}

function replacePost(next: CommunityPost) {
  posts.value = posts.value.map((item) => (item.id === next.id ? next : item))
}

function goDetail(post: CommunityPost) {
  router.push(`/community/posts/${post.id}`)
}

function openComments(post: CommunityPost) {
  commentDrawer.post = post
  commentDrawer.visible = true
}

function onCommentCountChanged(count: number) {
  if (!commentDrawer.post) return
  const next = { ...commentDrawer.post, commentCount: count }
  commentDrawer.post = next
  replacePost(next)
}

function openEdit(post: CommunityPost) {
  editDialog.postId = post.id
  editDialog.content = post.content
  editDialog.visibility = post.visibility
  editDialog.visible = true
}

async function saveEdit() {
  if (!editDialog.postId || !editDialog.content.trim()) {
    ElMessage.warning('动态内容不能为空')
    return
  }
  editDialog.saving = true
  try {
    const post = await updatePost(editDialog.postId, {
      content: editDialog.content.trim(),
      visibility: editDialog.visibility,
    })
    replacePost(post)
    editDialog.visible = false
    ElMessage.success('动态已更新')
  } finally {
    editDialog.saving = false
  }
}

async function removePost(post: CommunityPost) {
  await ElMessageBox.confirm('确定删除这条动态吗？删除后不会影响原打卡记录。', '删除动态', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
  await deletePost(post.id)
  posts.value = posts.value.filter((item) => item.id !== post.id)
  total.value = Math.max(0, total.value - 1)
  ElMessage.success('动态已删除')
}

onMounted(async () => {
  await Promise.all([loadPublishableCheckins(), reloadPosts()])
})
</script>

<style scoped>
.community-page {
  padding: 20px;
}

.feed-panel {
  min-height: 620px;
}

.feed-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.scope-tabs {
  min-width: 180px;
}

.scope-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.feed-list {
  min-height: 300px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
