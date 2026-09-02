<template>
  <div class="sf-page">
    <el-card class="greeting" shadow="never">
      <div class="greeting-inner">
        <div>
          <h2 class="hello">你好，{{ auth.user?.nickname ?? '同学' }} 👋</h2>
          <p class="desc">坚持每天一点点，学习计划自然水到渠成。</p>
        </div>
        <div class="quick-actions">
          <el-button type="primary" @click="$router.push('/checkins')">去打卡</el-button>
          <el-button @click="$router.push('/plans/new')">新建计划</el-button>
          <el-button @click="$router.push('/statistics')">查看统计</el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16" class="stat-row">
      <el-col :span="8" v-for="s in statCards" :key="s.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-num">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>
        <div class="card-head">
          <span>我的学习计划</span>
          <el-button link type="primary" @click="$router.push('/plans')">查看全部</el-button>
        </div>
      </template>

      <el-empty v-if="plans.length === 0" description="还没有学习计划，先新建一个吧" />
      <el-table v-else :data="plans" v-loading="loading">
        <el-table-column prop="name" label="计划名称" min-width="160" />
        <el-table-column label="周期" min-width="200">
          <template #default="{ row }">{{ row.startDate }} ~ {{ row.endDate }}</template>
        </el-table-column>
        <el-table-column label="每日目标" width="110">
          <template #default="{ row }">{{ row.dailyTarget }} 分钟</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="PLAN_STATUS_TAG[row.status] ?? 'info'">
              {{ PLAN_STATUS_LABEL[row.status] ?? row.status }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { listPlans } from '@/api/plans'
import type { StudyPlan } from '@/api/types'
import { PLAN_STATUS_LABEL, PLAN_STATUS_TAG } from '@/core/constants'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const plans = ref<StudyPlan[]>([])
const loading = ref(false)

const statCards = computed(() => {
  const active = plans.value.filter((p) => p.status === 'ACTIVE').length
  const completed = plans.value.filter((p) => p.status === 'COMPLETED').length
  return [
    { label: '进行中计划', value: active },
    { label: '已完成计划', value: completed },
    { label: '全部计划', value: plans.value.length },
  ]
})

onMounted(async () => {
  loading.value = true
  try {
    const page = await listPlans({ page: 1, pageSize: 100 })
    plans.value = page.items.slice(0, 5)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.greeting {
  margin-bottom: 16px;
}

.greeting-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.hello {
  margin: 0 0 6px;
}

.desc {
  margin: 0;
  color: #909399;
}

.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  text-align: center;
}

.stat-num {
  font-size: 30px;
  font-weight: 700;
  color: var(--sf-primary);
}

.stat-label {
  margin-top: 6px;
  color: #909399;
  font-size: 14px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
