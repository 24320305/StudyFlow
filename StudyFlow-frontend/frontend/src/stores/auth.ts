// 全项目唯一的认证状态源（执行方案 7.2：一套路由守卫 + 一个认证状态源）。
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  getMe,
  login as loginApi,
  logout as logoutApi,
  register as registerApi,
  updateMe as updateMeApi,
} from '@/api/auth'
import type { RegisterPayload, UpdateMePayload } from '@/api/auth'
import { clearToken, getToken, setToken } from '@/api/client'
import type { User } from '@/api/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(getToken())
  const user = ref<User | null>(null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(email: string, password: string): Promise<void> {
    const res = await loginApi({ email, password })
    token.value = res.accessToken
    user.value = res.user
    setToken(res.accessToken)
  }

  async function register(payload: RegisterPayload): Promise<void> {
    const res = await registerApi(payload)
    token.value = res.accessToken
    user.value = res.user
    setToken(res.accessToken)
  }

  /** 拉取当前用户信息（路由守卫在 token 存在但 user 为空时调用） */
  async function fetchMe(): Promise<void> {
    user.value = await getMe()
  }

  /** 更新个人资料 */
  async function updateProfile(payload: UpdateMePayload): Promise<void> {
    user.value = await updateMeApi(payload)
  }

  async function logout(): Promise<void> {
    try {
      await logoutApi()
    } catch {
      // 登出失败不阻塞本地清理
    }
    clear()
  }

  function clear(): void {
    token.value = ''
    user.value = null
    clearToken()
  }

  return {
    token,
    user,
    isLoggedIn,
    login,
    register,
    fetchMe,
    updateProfile,
    logout,
    clear,
  }
})
