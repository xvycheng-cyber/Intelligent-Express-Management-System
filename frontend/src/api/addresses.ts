import { request } from './request'
import type {
  AddressListQuery,
  AddressSavePayload,
  AddressSaveResult,
  AddressVO,
  BatchDeleteResult,
  BatchSaveRequest,
  BatchSaveResult,
  PageResult,
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

export function listAddresses(query: AddressListQuery): Promise<PageResult<AddressVO>> {
  return request<PageResult<AddressVO>>({
    url: '/addresses',
    method: 'get',
    params: query
  })
}

export function getAddress(id: string): Promise<AddressVO> {
  return request<AddressVO>({
    url: `/addresses/${id}`,
    method: 'get'
  })
}

export function createAddress(payload: AddressSavePayload): Promise<AddressSaveResult> {
  return request<AddressSaveResult>({
    url: '/addresses',
    method: 'post',
    data: payload
  })
}

export function updateAddress(id: string, payload: AddressSavePayload): Promise<AddressSaveResult> {
  return request<AddressSaveResult>({
    url: `/addresses/${id}`,
    method: 'put',
    data: payload
  })
}

export function deleteAddress(id: string): Promise<null> {
  return request<null>({
    url: `/addresses/${id}`,
    method: 'delete'
  })
}

export function batchDeleteAddresses(ids: string[]): Promise<BatchDeleteResult> {
  return request<BatchDeleteResult>({
    url: '/addresses/batch-delete',
    method: 'post',
    data: { ids }
  })
}
