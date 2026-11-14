export const NAME_MAX_CODE_POINTS = 50

/**
 * 姓名长度必须以 Unicode code point 计数。
 * HTML maxlength 与 String.length() 都按 UTF-16 code unit，不能单独作为姓名长度约束。
 */
export function codePointLength(value: string): number {
  return Array.from(value).length
}

export function isValidNameLength(value: string): boolean {
  const length = codePointLength(value)
  return length >= 1 && length <= NAME_MAX_CODE_POINTS
}
