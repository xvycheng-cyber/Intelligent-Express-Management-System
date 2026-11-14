export type ParseStatus = 'VALID' | 'INVALID'

export interface LineError {
  code: string
  field: string
  message: string
}

export interface ParsedItem {
  lineNo: number
  rawLine: string
  receiverName: string | null
  phone: string | null
  fullAddress: string | null
  status: ParseStatus
  errors: LineError[]
}

export interface ParseResult {
  totalLines: number
  effectiveLines: number
  skippedLines: number
  validCount: number
  invalidCount: number
  items: ParsedItem[]
}

export interface ParsedOriginal {
  receiverName: string
  phone: string
  fullAddress: string
}

export interface ParsedRow {
  rowKey: string
  lineNo: number
  rawLine: string
  receiverName: string
  phone: string
  fullAddress: string
  original: ParsedOriginal
  status: ParseStatus
  errors: LineError[]
}

export interface AddressSavePayload {
  receiverName: string
  phone: string
  fullAddress: string
  lineNo?: number
}

export interface BatchSaveRequest {
  items: AddressSavePayload[]
}

export interface BatchSaveResult {
  savedCount: number
  ids: string[]
}

export interface AddressSaveResult {
  id: string
}

export interface BatchDeleteResult {
  deletedCount: number
}

export interface AddressListQuery {
  page: number
  size: number
  keyword?: string
}

export interface AddressVO {
  id: string
  receiverName: string
  phone: string
  fullAddress: string
  createTime: string
  updateTime: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}
