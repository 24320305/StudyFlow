<template>
  <div class="sf-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-head">
          <div class="filters">
            <el-radio-group v-model="filterStatus" @change="reload">
              <el-radio-button value="">全部</el-radio-button>
              <el-radio-button value="ACTIVE">进行中</el-radio-button>
              <el-radio-button value="PAUSED">已暂停</el-radio-button>
              <el-radio-button value="COMPLETED">已完成</el-radio-button>
            </el-radio-group>
          </div>
          <el-button type="primary" @click="$router.push('/plans/new')">新建计划</el-button>
        </div>
      </template>

      <el-empty v-if="!loading && plans.length === 0" description="暂无学习计划" />
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push('/checkins?planId=' + row.id)">
              打卡
            </el-button>
            <el-button link type="primary" @click="$router.push(`/plans/${row.id}/edit`)">
              编辑
            </el-button>
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[5, 10, 20]"
          layout="total, sizes, prev, pager, next"
          @current-change="reload"
          @size-change="reload"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deletePlan, listPlans } from '@/api/plans'
import type { StudyPlan } from '@/api/types'
import { PLAN_STATUS_LABEL, PLAN_STATUS_TAG } from '@/core/constants'

const plans = ref<StudyPlan[]>([])
const loading = ref(false)
const filterStatus = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function reload() {
  loading.value = true
  try {
    const res = await listPlans({
      page: page.value,
      pageSize: pageSize.value,
      ...(filterStatus.value ? { status: filterStatus.value as StudyPlan['status'] } : {}),
    })
    plans.value = res.items
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function onDelete(row: StudyPlan) {
  await ElMessageBox.confirm(`确定删除计划「${row.name}」吗？删除后不可恢复。`, '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
  await deletePlan(row.id)
  ElMessage.success('已删除')
  reload()
}

onMounted(reload)
</script>

<style scoped>
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
