import { request } from './client'
import type { AuthResult, User } from './types'

export interface LoginPayload {
  email: string
  password: string
}

export interface RegisterPayload {
  email: string
  password: string
  nickname: string
}

export interface UpdateMePayload {
  nickname?: string
  avatarUrl?: string | null
}

/** 登录 */
export function login(payload: LoginPayload): Promise<AuthResult> {
  return request.post('/auth/login', payload)
}

/** 注册（服务端强制 role=USER，忽略请求体中的 role） */
export function register(payload: RegisterPayload): Promise<AuthResult> {
  return request.post('/auth/register', payload)
}

/** 登出 */
export function logout(): Promise<void> {
  return request.post('/auth/logout')
}

/** 当前登录用户 */
export function getMe(): Promise<User> {
  return request.get('/me')
}

/** 更新个人资料 */
export function updateMe(payload: UpdateMePayload): Promise<User> {
  return request.patch('/me', payload)
}
