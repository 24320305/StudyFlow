import { request } from './client'
import type { CheckIn } from './types'

export interface CheckinPayload {
  durationMinutes: number
  completed: boolean
  note?: string | null
  imageUrl?: string | null
}

export interface CheckinListParams {
  startDate?: string
  endDate?: string
}

/** 查询某计划的打卡记录 */
export function listCheckins(
  planId: number,
  params?: CheckinListParams,
): Promise<CheckIn[]> {
  return request.get(`/plans/${planId}/check-ins`, { params })
}

/** 保存/更新某天的打卡（同一天重复提交为更新，不新增） */
export function upsertCheckin(
  planId: number,
  date: string,
  payload: CheckinPayload,
): Promise<CheckIn> {
  return request.put(`/plans/${planId}/check-ins/${date}`, payload)
}
