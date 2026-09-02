<template>
  <div class="sf-page">
    <el-card shadow="never" class="form-card">
      <template #header>
        <span>{{ isEdit ? '编辑计划' : '新建计划' }}</span>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="110px"
        style="max-width: 560px"
      >
        <el-form-item label="计划名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：考研英语冲刺" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="form.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择开始日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="form.endDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择结束日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="每日目标" prop="dailyTarget">
          <el-input-number v-model="form.dailyTarget" :min="1" :max="1440" />
          <span class="unit">分钟 / 天</span>
        </el-form-item>
        <el-form-item v-if="isEdit" label="状态" prop="status">
          <el-select v-model="form.status" style="width: 200px">
            <el-option label="进行中" value="ACTIVE" />
            <el-option label="已暂停" value="PAUSED" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSubmit">保存</el-button>
          <el-button @click="$router.push('/plans')">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createPlan, getPlan, updatePlan } from '@/api/plans'
import type { PlanStatus } from '@/api/types'

const route = useRoute()
const router = useRouter()

const id = computed(() => {
  const raw = route.params.id
  return raw ? Number(raw) : null
})
const isEdit = computed(() => id.value !== null)

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({
  name: '',
  startDate: '',
  endDate: '',
  dailyTarget: 60,
  status: 'ACTIVE' as PlanStatus,
})

function validateEndDate(_rule: unknown, value: string, callback: (err?: Error) => void) {
  if (form.startDate && value && value < form.startDate) {
    callback(new Error('结束日期不得早于开始日期'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  name: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' },
    { validator: validateEndDate, trigger: 'change' },
  ],
  dailyTarget: [{ required: true, message: '请设置每日目标', trigger: 'change' }],
}

async function onSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const basePayload = {
        name: form.name,
        startDate: form.startDate,
        endDate: form.endDate,
        dailyTarget: form.dailyTarget,
      }
      if (isEdit.value && id.value !== null) {
        await updatePlan(id.value, { ...basePayload, status: form.status })
        ElMessage.success('已更新')
      } else {
        await createPlan(basePayload)
        ElMessage.success('已创建')
      }
      router.push('/plans')
    } finally {
      loading.value = false
    }
  })
}

onMounted(async () => {
  if (isEdit.value && id.value !== null) {
    loading.value = true
    try {
      const plan = await getPlan(id.value)
      form.name = plan.name
      form.startDate = plan.startDate
      form.endDate = plan.endDate
      form.dailyTarget = plan.dailyTarget
      form.status = plan.status
    } finally {
      loading.value = false
    }
  }
})
</script>

<style scoped>
.form-card {
  max-width: 720px;
}

.unit {
  margin-left: 8px;
  color: #909399;
}
</style>
