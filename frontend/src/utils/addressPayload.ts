import type { AddressSavePayload, ParsedRow } from '@/types/address'

export function toPayload(row: ParsedRow): AddressSavePayload {
  return {
    receiverName: row.receiverName.trim(),
    phone: row.phone.trim(),
    fullAddress: row.fullAddress.trim(),
    lineNo: row.lineNo
  }
}

export function isRowEdited(row: ParsedRow): boolean {
  return (
    row.receiverName !== row.original.receiverName ||
    row.phone !== row.original.phone ||
    row.fullAddress !== row.original.fullAddress
  )
}

export function restoreRow(row: ParsedRow): void {
  row.receiverName = row.original.receiverName
  row.phone = row.original.phone
  row.fullAddress = row.original.fullAddress
}

export function payloadSignature(items: AddressSavePayload[]): string {
  return JSON.stringify(items)
}
