import axios, { type AxiosRequestConfig } from 'axios'
import type { ApiResponse, Violation } from '@/types/api'

export class ApiError extends Error {
  readonly code: number
  readonly data: unknown
  readonly violations: Violation[]

  constructor(message: string, code = 0, data: unknown = null) {
    super(message)
    this.name = 'ApiError'
    this.code = code
    this.data = data
    this.violations = extractViolations(data)
  }
}

function extractViolations(data: unknown): Violation[] {
  if (!data || typeof data !== 'object') return []
  const violations = (data as { violations?: unknown }).violations
  if (!Array.isArray(violations)) return []
  return violations.filter(
    (item): item is Violation =>
      !!item &&
      typeof item === 'object' &&
      typeof (item as Violation).field === 'string' &&
      typeof (item as Violation).message === 'string'
  )
}

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE ?? '/api',
  timeout: 10_000
})

/**
 * 统一请求封装：成功返回 data；失败抛出带业务码/violations 的 ApiError。
 */
export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  try {
    const response = await http.request<ApiResponse<T>>(config)
    const body = response.data
    if (body.code !== 0) {
      throw new ApiError(body.message || '请求失败', body.code, body.data)
    }
    return body.data
  } catch (error) {
    if (error instanceof ApiError) throw error
    if (axios.isAxiosError(error)) {
      if (error.code === 'ECONNABORTED') {
        throw new ApiError('请求超时，请检查网络后重试')
      }
      const body = error.response?.data as ApiResponse<unknown> | undefined
      if (body) {
        throw new ApiError(body.message || '请求失败', body.code, body.data)
      }
      throw new ApiError('网络异常，请确认后端服务已启动后重试')
    }
    throw error instanceof Error ? error : new ApiError('请求失败')
  }
}

export default http
