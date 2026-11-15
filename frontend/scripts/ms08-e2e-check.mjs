import assert from 'node:assert/strict'
import { chromium } from 'playwright'

const BASE = process.env.MS08_BASE_URL ?? 'http://127.0.0.1:5173'
const PHONE_PREFIX = '1770000000'
const browser = await chromium.launch({ channel: 'chrome', headless: true })
const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } })
const pageErrors = []

page.on('pageerror', (error) => pageErrors.push(error.message))

async function rows() {
  return page.locator('[data-testid="parse-result-table"] tbody tr').count()
}

async function waitAddressRows(count) {
  await page.waitForFunction(
    (expected) => document.querySelectorAll('[data-testid="address-table"] tbody tr').length === expected,
    count,
    { timeout: 15_000 }
  )
}

async function searchAddresses(keyword) {
  await page.getByTestId('address-search-input').fill(keyword)
  await page.getByTestId('address-search-button').click()
  await page.waitForFunction(() => !document.querySelector('[data-testid="address-table"] .el-loading-mask'))
}

try {
  await page.goto(`${BASE}/import`, { waitUntil: 'networkidle' })
  await page.getByRole('heading', { name: '批量录入收件地址' }).waitFor()

  const textarea = page.locator('textarea').first()
  await textarea.fill([
    `测试甲，${PHONE_PREFIX}1，浙江省杭州市西湖区测试路1号`,
    `测试乙，${PHONE_PREFIX}2，浙江省杭州市西湖区测试路2号`,
    `测试丙，${PHONE_PREFIX}3，浙江省杭州市西湖区测试路3号`
  ].join('\n'))
  await page.getByTestId('parse-button').click()
  await page.waitForFunction(() => document.querySelectorAll('[data-testid="parse-result-table"] tbody tr').length === 3)
  assert.equal(await rows(), 3)

  await page.getByTestId('open-save-dialog').click()
  await page.getByTestId('confirm-save').click()
  await page.getByTestId('save-success').waitFor()
  assert.match(await page.getByTestId('save-success').innerText(), /3\s*条地址/)

  await page.goto(`${BASE}/addresses`, { waitUntil: 'networkidle' })
  await searchAddresses(PHONE_PREFIX)
  await waitAddressRows(3)
  assert.match(await page.getByTestId('address-table').innerText(), /测试甲/)

  // Edit one saved row, refresh, and verify persistence.
  await page.getByTestId('edit-address').first().click()
  await page.locator('.el-dialog:visible').waitFor()
  await page.getByTestId('address-form-name').fill('测试甲已修改')
  await page.getByTestId('address-form-address').fill('浙江省杭州市西湖区修改路9号')
  await page.getByTestId('address-form-submit').click()
  await page.locator('.el-dialog:visible').waitFor({ state: 'hidden' })
  await page.getByTestId('address-refresh').click()
  await page.waitForFunction(() => !document.querySelector('[data-testid="address-table"] .el-loading-mask'))
  assert.match(await page.getByTestId('address-table').innerText(), /测试甲已修改/)

  // Single delete one row.
  await page.getByTestId('delete-address').first().click()
  await page.getByRole('button', { name: '确认删除' }).click()
  await page.waitForFunction(() => !document.querySelector('[data-testid="address-table"] .el-loading-mask'))
  await waitAddressRows(2)

  // Select the two remaining rows and batch-delete them.
  const checks = page.locator('[data-testid="address-table"] tbody .el-checkbox')
  await checks.nth(0).click()
  await checks.nth(1).click()
  await page.getByTestId('batch-delete-open').click()
  await page.getByTestId('batch-delete-confirm').click()
  await page.waitForFunction(() => !document.querySelector('[data-testid="address-table"] .el-loading-mask'))
  await waitAddressRows(0)
  assert.match(await page.getByTestId('address-table').innerText(), /地址簿还是空的|没有找到匹配的地址/)

  assert.deepEqual(pageErrors, [])
  console.log('MS08_E2E_OK')
} finally {
  await browser.close()
}
