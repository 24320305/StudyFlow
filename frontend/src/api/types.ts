// 统一后端契约类型。字段名严格遵循《五人分工统一执行方案》4.2 关键 DTO 字段字典。

/** 统一响应包装 */
export interface ApiEnvelope<T> {
  code: string
  message: string
  data: T
  requestId: string
}

export type UserRole = 'USER' | 'ADMIN'
export type UserStatus = 'NORMAL' | 'RESTRICTED' | 'DISABLED'

export interface User {
  id: number
  email: string
  nickname: string
  avatarUrl: string | null
  role: UserRole
  status: UserStatus
}

export interface AuthResult {
  token: string
  user: User
}

export type PlanStatus = 'ACTIVE' | 'PAUSED' | 'COMPLETED'

export interface StudyPlan {
  id: number
  userId: number
  name: string
  startDate: string
  endDate: string
  dailyTarget: number
  status: PlanStatus
}

export interface CheckIn {
  id: number
  planId: number
  checkDate: string
  durationMinutes: number
  completed: boolean
  note: string | null
  imageUrl: string | null
}

/** 统计口径结果（由后端 Service 统一计算，前端只展示） */
export interface Statistics {
  totalDurationMinutes: number
  /** 分母为 0 时为 null，不返回伪造的 0% */
  completionRate: number | null
  streakDays: number
  completedDays: number
  totalDays: number
  checkinCount: number
}

/** 统一分页返回：page 从 1 开始 */
export interface Page<T> {
  items: T[]
  page: number
  pageSize: number
  total: number
}

export interface UploadResult {
  fileId: string
  url: string
}
