import axios, { type AxiosRequestConfig } from 'axios'
import type { ApiResponse } from '@/types/api'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE ?? '/api',
  timeout: 10_000
})

/**
 * 统一请求封装：成功返回 data；失败抛出带后端 message 的 Error。
 */
export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  try {
    const response = await http.request<ApiResponse<T>>(config)
    const body = response.data
    if (body.code !== 0) {
      throw new Error(body.message || '请求失败')
    }
    return body.data
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const body = error.response?.data as ApiResponse<unknown> | undefined
      throw new Error(body?.message || '网络异常，请稍后重试')
    }
    throw error
  }
}

export default http
