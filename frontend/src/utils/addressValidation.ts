import { codePointLength, NAME_MAX_CODE_POINTS } from './textLength'
import { isRowEdited } from './addressPayload'
import type { ParsedRow } from '@/types/address'

const NAME_PATTERN = /^(?:[\p{Script=Han}·]{1,50}|[A-Za-zＡ-Ｚａ-ｚ][A-Za-zＡ-Ｚａ-ｚ .'\-．＇－]{0,49})$/u
const PHONE_PATTERN =
  /^(?:1[3-9]\d{9}|1[3-9]\d[xX*]{4}\d{4}|0\d{2,3}\d{7,8}(?:-?\d{1,6})?|(?:\+?86[\s-]*)?1[3-9]\d(?:[\s-]?\d){8}|[（(]?\s*1[3-9]\d(?:[\s-]?\d){8}\s*[）)]?)$/
const ADDRESS_MAX_LENGTH = 255
const PHONE_MAX_LENGTH = 20

export function validateName(value: string): string | null {
  const name = value.trim()
  if (!name) return '姓名不能为空'
  if (codePointLength(name) > NAME_MAX_CODE_POINTS) {
    return `姓名不能超过 ${NAME_MAX_CODE_POINTS} 个 Unicode 码点`
  }
  if (!NAME_PATTERN.test(name)) return '姓名只能包含中文/英文姓名允许的字符'
  return null
}

export function validatePhone(value: string): string | null {
  const phone = value.trim()
  if (!phone) return '联系电话不能为空'
  if (phone.length > PHONE_MAX_LENGTH) return `联系电话不能超过 ${PHONE_MAX_LENGTH} 个字符`
  if (!PHONE_PATTERN.test(phone)) return '联系电话格式不正确'
  return null
}

export function validateAddress(value: string): string | null {
  const address = value.trim()
  if (!address) return '详细地址不能为空'
  if (address.length > ADDRESS_MAX_LENGTH) {
    return `详细地址不能超过 ${ADDRESS_MAX_LENGTH} 个字符`
  }
  return null
}

export function validateRow(row: ParsedRow): string[] {
  return [validateName(row.receiverName), validatePhone(row.phone), validateAddress(row.fullAddress)].filter(
    (issue): issue is string => !!issue
  )
}

/**
 * 提交条件：当前字段本地校验全部通过，且原始行有效，或用户已实际修改并修正。
 * 后端解析状态不会被前端自动改写。
 */
export function canSubmitRow(row: ParsedRow): boolean {
  return validateRow(row).length === 0 && (row.status === 'VALID' || isRowEdited(row))
}

export function nameLength(value: string): number {
  return codePointLength(value.trim())
}

export function isNameTooLong(value: string): boolean {
  return nameLength(value) > NAME_MAX_CODE_POINTS
}
