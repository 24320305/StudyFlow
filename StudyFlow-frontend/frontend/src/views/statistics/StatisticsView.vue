<template>
  <div class="sf-page">
    <el-card shadow="never" class="toolbar">
      <span class="toolbar-label">选择计划</span>
      <el-select v-model="planId" placeholder="选择计划" style="width: 260px" @change="reload">
        <el-option v-for="p in plans" :key="p.id" :label="p.name" :value="p.id" />
      </el-select>
    </el-card>

    <el-row :gutter="16" class="stat-row">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-num">{{ stats?.totalDurationMinutes ?? 0 }}</div>
          <div class="stat-label">累计学习时长（分钟）</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-num">
            {{ stats?.completionRate === null || stats?.completionRate === undefined ? '—' : stats.completionRate + '%' }}
          </div>
          <div class="stat-label">完成率</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-num">{{ stats?.streakDays ?? 0 }}</div>
          <div class="stat-label">连续打卡天数</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>
        <div class="cal-head">
          <el-button-group>
            <el-button :icon="ArrowLeft" @click="prevMonth" />
            <el-button :icon="ArrowRight" @click="nextMonth" />
          </el-button-group>
          <span class="cal-title">{{ calYear }} 年 {{ calMonth }} 月</span>
          <div class="legend">
            <span class="dot done"></span>已完成打卡
            <span class="dot today"></span>今天
          </div>
        </div>
      </template>

      <div class="calendar">
        <div class="week-row">
          <div v-for="w in weekdays" :key="w" class="weekday">{{ w }}</div>
        </div>
        <div class="grid">
          <div
            v-for="(cell, i) in calendarCells"
            :key="i"
            class="cell"
            :class="{
              empty: cell === null,
              done: cell !== null && isDone(cell),
              today: cell !== null && isToday(cell),
            }"
          >
            <span v-if="cell !== null">{{ cell }}</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { listCheckins } from '@/api/checkins'
import { listPlans } from '@/api/plans'
import { getStatistics } from '@/api/statistics'
import type { CheckIn, Statistics, StudyPlan } from '@/api/types'
import { isMockMode } from '@/api/runtime'

const weekdays = ['日', '一', '二', '三', '四', '五', '六']

const plans = ref<StudyPlan[]>([])
const planId = ref<number | null>(null)
const stats = ref<Statistics | null>(null)
const checkins = ref<CheckIn[]>([])

const now = new Date()
const calYear = ref(now.getFullYear())
const calMonth = ref(now.getMonth() + 1)

function pad(n: number): string {
  return String(n).padStart(2, '0')
}

function dateKey(day: number): string {
  return `${calYear.value}-${pad(calMonth.value)}-${pad(day)}`
}

const completedDates = computed(
  () => new Set(checkins.value.filter((c) => c.completed).map((c) => c.checkDate)),
)

const calendarCells = computed<(number | null)[]>(() => {
  const first = new Date(calYear.value, calMonth.value - 1, 1)
  const startWeekday = first.getDay()
  const daysInMonth = new Date(calYear.value, calMonth.value, 0).getDate()
  const cells: (number | null)[] = []
  for (let i = 0; i < startWeekday; i++) cells.push(null)
  for (let d = 1; d <= daysInMonth; d++) cells.push(d)
  return cells
})

function isDone(day: number): boolean {
  return completedDates.value.has(dateKey(day))
}

function isToday(day: number): boolean {
  const d = new Date()
  return (
    d.getFullYear() === calYear.value &&
    d.getMonth() + 1 === calMonth.value &&
    d.getDate() === day
  )
}

function prevMonth() {
  calMonth.value -= 1
  if (calMonth.value < 1) {
    calMonth.value = 12
    calYear.value -= 1
  }
}

function nextMonth() {
  calMonth.value += 1
  if (calMonth.value > 12) {
    calMonth.value = 1
    calYear.value += 1
  }
}

async function reload() {
  if (!planId.value) {
    stats.value = null
    checkins.value = []
    return
  }
  const list = await listCheckins(planId.value)
  checkins.value = list
  if (isMockMode) {
    stats.value = await getStatistics({ planId: planId.value })
    return
  }

  const plan = plans.value.find((item) => item.id === planId.value)
  stats.value = plan ? calculateStatistics(plan, list) : null
}

function calculateStatistics(plan: StudyPlan, list: CheckIn[]): Statistics {
  const today = new Date()
  const todayKey = `${today.getFullYear()}-${pad(today.getMonth() + 1)}-${pad(today.getDate())}`
  const rangeEnd = plan.endDate < todayKey ? plan.endDate : todayKey
  const totalDays = rangeEnd < plan.startDate ? 0 : daysInclusive(plan.startDate, rangeEnd)
  const completed = list.filter((item) => item.completed)
  const completedDates = new Set(completed.map((item) => item.checkDate))
  let streakDays = 0
  let cursor = rangeEnd
  while (completedDates.has(cursor)) {
    streakDays += 1
    cursor = addDays(cursor, -1)
  }
  return {
    totalDurationMinutes: list.reduce((total, item) => total + item.durationMinutes, 0),
    completionRate: totalDays > 0 ? Math.round((completed.length / totalDays) * 10000) / 100 : null,
    streakDays,
    completedDays: completed.length,
    totalDays,
    checkinCount: list.length,
  }
}

function daysInclusive(start: string, end: string): number {
  const startMs = new Date(`${start}T00:00:00`).getTime()
  const endMs = new Date(`${end}T00:00:00`).getTime()
  return Math.floor((endMs - startMs) / 86400000) + 1
}

function addDays(date: string, amount: number): string {
  const value = new Date(`${date}T00:00:00`)
  value.setDate(value.getDate() + amount)
  return `${value.getFullYear()}-${pad(value.getMonth() + 1)}-${pad(value.getDate())}`
}

onMounted(async () => {
  const res = await listPlans({ page: 1, pageSize: 100 })
  plans.value = res.items
  planId.value = plans.value[0]?.id ?? null
  await reload()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}

.toolbar-label {
  margin-right: 12px;
  color: #606266;
}

.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  text-align: center;
}

.stat-num {
  font-size: 32px;
  font-weight: 700;
  color: var(--sf-primary);
}

.stat-label {
  margin-top: 6px;
  color: #909399;
  font-size: 14px;
}

.cal-head {
  display: flex;
  align-items: center;
  gap: 16px;
}

.cal-title {
  font-weight: 600;
  font-size: 16px;
}

.legend {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #909399;
  font-size: 13px;
}

.dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 4px;
}

.dot.done {
  background: var(--sf-primary);
}

.dot.today {
  border: 2px solid #e6a23c;
}

.week-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 8px;
}

.weekday {
  text-align: center;
  color: #909399;
  font-size: 13px;
  padding: 8px 0;
}

.grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
}

.cell {
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #303133;
  font-size: 14px;
}

.cell.empty {
  background: transparent;
}

.cell.done {
  background: var(--sf-primary);
  color: #fff;
  font-weight: 600;
}

.cell.today {
  box-shadow: inset 0 0 0 2px #e6a23c;
}
</style>
