<template>
  <el-card shadow="never" class="composer">
    <template #header>
      <div class="composer-head">
        <span>发布学习动态</span>
        <el-tag type="info" effect="plain">需选择已完成打卡</el-tag>
      </div>
    </template>

    <el-form label-width="86px">
      <el-form-item label="关联打卡">
        <el-select
          v-model="form.checkInId"
          filterable
          placeholder="选择一条已完成打卡"
          style="width: 100%"
        >
          <el-option
            v-for="item in checkins"
            :key="item.id"
            :label="formatCheckIn(item)"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="可见范围">
        <el-segmented v-model="form.visibility" :options="visibilityOptions" />
      </el-form-item>
      <el-form-item label="动态内容">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="3"
          maxlength="500"
          show-word-limit
          placeholder="记录这次学习完成后的收获"
        />
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          :icon="Promotion"
          :disabled="!canSubmit"
          :loading="submitting"
          @click="submit"
        >
          发布动态
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'
import { publishPost } from '@/api/community'
import type { CheckIn, CommunityPost, PostVisibility } from '@/api/types'
import { POST_VISIBILITY_LABEL } from '@/core/constants'

defineProps<{
  checkins: CheckIn[]
}>()

const emit = defineEmits<{
  published: [post: CommunityPost]
}>()

const form = reactive({
  checkInId: null as number | null,
  content: '',
  visibility: 'PUBLIC' as PostVisibility,
})
const submitting = ref(false)

const visibilityOptions = (Object.keys(POST_VISIBILITY_LABEL) as PostVisibility[]).map((value) => ({
  label: POST_VISIBILITY_LABEL[value],
  value,
}))

const canSubmit = computed(() => Boolean(form.checkInId && form.content.trim()))

function formatCheckIn(item: CheckIn): string {
  return `${item.checkDate} · ${item.durationMinutes} 分钟${item.note ? ' · ' + item.note : ''}`
}

async function submit() {
  if (!form.checkInId || !form.content.trim()) {
    ElMessage.warning('请选择打卡并填写动态内容')
    return
  }
  submitting.value = true
  try {
    const post = await publishPost({
      checkInId: form.checkInId,
      content: form.content.trim(),
      visibility: form.visibility,
    })
    ElMessage.success('动态已发布')
    form.content = ''
    form.checkInId = null
    emit('published', post)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.composer {
  margin-bottom: 16px;
}

.composer-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
</style>
