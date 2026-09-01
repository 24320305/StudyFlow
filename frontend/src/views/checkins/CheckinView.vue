<template>
  <div class="sf-page">
    <el-row :gutter="16">
      <el-col :span="10">
        <el-card shadow="never">
          <template #header><span>今日打卡</span></template>

          <el-form :model="form" label-width="90px">
            <el-form-item label="学习计划">
              <el-select
                v-model="planId"
                placeholder="选择计划"
                style="width: 100%"
                @change="onPlanChange"
              >
                <el-option
                  v-for="p in activePlans"
                  :key="p.id"
                  :label="p.name"
                  :value="p.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="打卡日期">
              <el-date-picker
                v-model="checkDate"
                type="date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                @change="loadFormFromList"
              />
            </el-form-item>
            <el-form-item label="学习时长">
              <el-input-number v-model="form.durationMinutes" :min="0" :max="1440" />
              <span class="unit">分钟</span>
            </el-form-item>
            <el-form-item label="完成情况">
              <el-switch
                v-model="form.completed"
                active-text="已完成"
                inactive-text="未完成"
              />
            </el-form-item>
            <el-form-item label="学习笔记">
              <el-input
                v-model="form.note"
                type="textarea"
                :rows="3"
                placeholder="记录今天的收获（可选）"
              />
            </el-form-item>
            <el-form-item label="打卡图片">
              <div class="upload-box">
                <template v-if="imagePreview">
                  <el-image :src="imagePreview" fit="cover" class="preview" />
                  <el-button link type="danger" @click="removeImage">移除图片</el-button>
                </template>
                <el-upload
                  v-else
                  :show-file-list="false"
                  :auto-upload="false"
                  accept=".jpg,.jpeg,.png,.webp"
                  :on-change="onImageChange"
                >
                  <el-button :icon="Plus">上传图片</el-button>
                </el-upload>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="saving"
                :disabled="!planId"
                @click="onSave"
              >
                保存打卡
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <span>打卡记录{{ planId ? '' : '' }}</span>
          </template>
          <el-empty
            v-if="!loading && checkins.length === 0"
            description="该计划还没有打卡记录"
          />
          <el-table v-else :data="checkins" v-loading="loading" max-height="480">
            <el-table-column prop="checkDate" label="日期" width="120" />
            <el-table-column label="时长" width="100">
              <template #default="{ row }">{{ row.durationMinutes }} 分钟</template>
            </el-table-column>
            <el-table-column label="完成" width="90">
              <template #default="{ row }">
                <el-tag :type="row.completed ? 'success' : 'info'">
                  {{ row.completed ? '已完成' : '未完成' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="note" label="笔记" min-width="140" show-overflow-tooltip />
            <el-table-column label="图片" width="80">
              <template #default="{ row }">
                <el-image
                  v-if="row.imageUrl"
                  :src="row.imageUrl"
                  fit="cover"
                  class="thumb"
                  :preview-src-list="[row.imageUrl]"
                />
                <span v-else class="no-img">—</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { UploadFile } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { listCheckins, upsertCheckin } from '@/api/checkins'
import { listPlans } from '@/api/plans'
import { uploadImage } from '@/api/uploads'
import type { CheckIn, StudyPlan } from '@/api/types'

const route = useRoute()

function todayStr(): string {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const activePlans = ref<StudyPlan[]>([])
const planId = ref<number | null>(null)
const checkDate = ref(todayStr())
const checkins = ref<CheckIn[]>([])
const loading = ref(false)
const saving = ref(false)
const imagePreview = ref('')

const form = reactive({
  durationMinutes: 0,
  completed: true,
  note: '',
  imageUrl: null as string | null,
})

async function loadPlans() {
  const res = await listPlans({ page: 1, pageSize: 100 })
  activePlans.value = res.items.filter((p) => p.status === 'ACTIVE')
  // 支持从计划列表跳转 ?planId=xxx
  const fromQuery = Number(route.query.planId)
  const matched = activePlans.value.find((p) => p.id === fromQuery)
  planId.value = matched ? matched.id : activePlans.value[0]?.id ?? null
}

async function loadCheckins() {
  if (!planId.value) {
    checkins.value = []
    return
  }
  loading.value = true
  try {
    checkins.value = await listCheckins(planId.value)
  } finally {
    loading.value = false
  }
}

function loadFormFromList() {
  const found = checkins.value.find((c) => c.checkDate === checkDate.value)
  if (found) {
    form.durationMinutes = found.durationMinutes
    form.completed = found.completed
    form.note = found.note ?? ''
    form.imageUrl = found.imageUrl
    imagePreview.value = found.imageUrl ?? ''
  } else {
    form.durationMinutes = 0
    form.completed = true
    form.note = ''
    form.imageUrl = null
    imagePreview.value = ''
  }
}

async function onPlanChange() {
  resetForm()
  await loadCheckins()
  loadFormFromList()
}

function resetForm() {
  form.durationMinutes = 0
  form.completed = true
  form.note = ''
  form.imageUrl = null
  imagePreview.value = ''
}

async function onImageChange(uploadFile: UploadFile) {
  const raw = uploadFile.raw
  if (!raw) return
  const res = await uploadImage(raw)
  form.imageUrl = res.url
  // 本地预览用 objectURL，字段保存后端返回的 url
  imagePreview.value = URL.createObjectURL(raw)
  ElMessage.success('图片已上传')
}

function removeImage() {
  form.imageUrl = null
  imagePreview.value = ''
}

async function onSave() {
  if (!planId.value) return
  if (form.durationMinutes < 0) {
    ElMessage.warning('学习时长不能为负数')
    return
  }
  saving.value = true
  try {
    await upsertCheckin(planId.value, checkDate.value, {
      durationMinutes: form.durationMinutes,
      completed: form.completed,
      note: form.note || null,
      imageUrl: form.imageUrl,
    })
    ElMessage.success('打卡已保存')
    await loadCheckins()
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loadPlans()
  await loadCheckins()
  loadFormFromList()
})
</script>

<style scoped>
.unit {
  margin-left: 8px;
  color: #909399;
}

.upload-box {
  display: flex;
  align-items: center;
  gap: 12px;
}

.preview {
  width: 120px;
  height: 90px;
  border-radius: 6px;
}

.thumb {
  width: 48px;
  height: 36px;
  border-radius: 4px;
}

.no-img {
  color: #c0c4cc;
}
</style>
