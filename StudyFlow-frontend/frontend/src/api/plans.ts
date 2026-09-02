import { request } from './client'
import type { Page, PlanStatus, StudyPlan } from './types'

export interface PlanPayload {
  name: string
  startDate: string
  endDate: string
  dailyTarget: number
  status?: PlanStatus
}

export interface PlanListParams {
  page?: number
  pageSize?: number
  status?: PlanStatus
}

/** 分页查询当前用户的学习计划 */
export function listPlans(params?: PlanListParams): Promise<Page<StudyPlan>> {
  return request.get('/plans', { params })
}

/** 查询单个计划 */
export function getPlan(id: number): Promise<StudyPlan> {
  return request.get(`/plans/${id}`)
}

/** 新建计划 */
export function createPlan(payload: PlanPayload): Promise<StudyPlan> {
  return request.post('/plans', payload)
}

/** 更新计划 */
export function updatePlan(id: number, payload: PlanPayload): Promise<StudyPlan> {
  return request.patch(`/plans/${id}`, payload)
}

/** 删除计划 */
export function deletePlan(id: number): Promise<void> {
  return request.delete(`/plans/${id}`)
}
