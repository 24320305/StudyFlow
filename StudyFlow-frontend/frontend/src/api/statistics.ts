import { request } from './client'
import type { Statistics } from './types'

export interface StatisticsParams {
  planId?: number
  startDate?: string
  endDate?: string
}

/** 获取统计口径结果（后端统一计算，前端只展示） */
export function getStatistics(params: StatisticsParams): Promise<Statistics> {
  return request.get('/statistics', { params })
}
