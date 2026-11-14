export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface Violation {
  field: string
  message: string
  lineNo?: number | null
}

export interface HealthData {
  status: 'UP' | 'DOWN' | string
}
