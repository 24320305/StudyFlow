// 全项目唯一的 Axios 实例（执行方案 7.2：全项目仅一个 Axios 实例）。
// 统一解包 { code, message, data, requestId }，统一处理错误提示与 401 登出。
import axios from 'axios'
import type { AxiosError, AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiEnvelope } from './types'

const TOKEN_KEY = 'studyflow_token'

export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) ?? ''
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

const client = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截：自动携带 JWT
client.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截：解包统一响应；失败按 HTTP 状态表达语义并提示。
client.interceptors.response.use(
  (response) => {
    const body = response.data as ApiEnvelope<unknown>
    if (body && body.code === 'OK') {
      return body.data as never
    }
    const msg = body?.message || '请求失败'
    ElMessage.error(msg)
    return Promise.reject(new Error(msg))
  },
  (error: AxiosError<ApiEnvelope<unknown>>) => {
    const status = error.response?.status
    const msg = error.response?.data?.message || error.message || '网络错误'
    // 仅当“带着旧 token 的请求”收到 401 时才判定为会话失效并登出；
    // 登录接口自身的 401（密码错误）不触发跳转。
    if (status === 401 && getToken()) {
      clearToken()
      window.location.assign('/login')
      return Promise.reject(error)
    }
    ElMessage.error(msg)
    return Promise.reject(error)
  },
)

// 类型化的请求封装：返回的 Promise 直接是解包后的 data。
export const request = {
  get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return client.get(url, config) as Promise<T>
  },
  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return client.post(url, data, config) as Promise<T>
  },
  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return client.put(url, data, config) as Promise<T>
  },
  patch<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return client.patch(url, data, config) as Promise<T>
  },
  delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return client.delete(url, config) as Promise<T>
  },
}

export default client
