import { request } from './request'
import type { HealthData } from '@/types/api'

export function getHealth(): Promise<HealthData> {
  return request<HealthData>({
    url: '/health',
    method: 'get'
  })
}
