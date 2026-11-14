import { request } from './request'
import type {
  AddressSavePayload,
  BatchSaveRequest,
  BatchSaveResult,
  ParseResult
} from '@/types/address'

export function parseAddresses(rawText: string): Promise<ParseResult> {
  return request<ParseResult>({
    url: '/addresses/parse',
    method: 'post',
    data: { rawText }
  })
}

export function saveAddressBatch(items: AddressSavePayload[]): Promise<BatchSaveResult> {
  const body: BatchSaveRequest = { items }
  return request<BatchSaveResult>({
    url: '/addresses/batch',
    method: 'post',
    data: body
  })
}
