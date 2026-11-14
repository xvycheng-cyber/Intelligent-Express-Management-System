import assert from 'node:assert/strict'
import { chromium } from 'playwright'

const BASE = process.env.MS05_BASE_URL ?? 'http://127.0.0.1:5173'
const API = process.env.MS05_API_URL ?? 'http://127.0.0.1:8080/api'
const PHONE_PREFIX = '199100'
const browser = await chromium.launch({ channel: 'chrome', headless: true })
const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } })
const consoleErrors = []
page.on('console', (msg) => {
  if (msg.type() === 'error') consoleErrors.push(msg.text())
})
page.on('pageerror', (error) => consoleErrors.push(`pageerror: ${error.message}`))

async function api(path, options = {}) {
  const response = await fetch(`${API}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options
  })
  const body = await response.json()
  if (!response.ok || body.code !== 0) {
    const error = new Error(body.message || `HTTP ${response.status}`)
    error.code = body.code
    throw error
  }
  return body.data
}

async function cleanupTestRows() {
  try {
    const data = await api(`/addresses?page=1&size=100&keyword=${PHONE_PREFIX}`)
    const ids = data.records.map((row) => row.id)
    if (ids.length) {
      await api('/addresses/batch-delete', {
        method: 'POST',
        body: JSON.stringify({ ids })
      })
    }
  } catch (error) {
    if (error.code !== 40401) throw error
  }
}

async function seedTestRows() {
  await cleanupTestRows()
  for (let i = 1; i <= 11; i += 1) {
    const phone = `${PHONE_PREFIX}${String(i).padStart(5, '0')}`
    let address = `浙江省杭州市测试小区${i}号`
    if (i === 10) address = '浙江省杭州市百分%小区'
    if (i === 11) address = '浙江省杭州市下划_小区'
    await api('/addresses', {
      method: 'POST',
      body: JSON.stringify({
        receiverName: '列表测试',
        phone,
        fullAddress: address
      })
    })
  }
}

function tableRows() {
  return page.locator('[data-testid="address-table"] tbody tr')
}

async function waitRows(count) {
  await page.waitForFunction(
    (expected) => document.querySelectorAll('[data-testid="address-table"] tbody tr').length === expected,
    count,
    { timeout: 15_000 }
  )
}

async function waitLoadingDone() {
  await page.waitForFunction(
    () => !document.querySelector('[data-testid="address-table"] .el-loading-mask'),
    null,
    { timeout: 15_000 }
  )
}

async function search(keyword) {
  await page.getByTestId('address-search-input').fill(keyword)
  await page.getByTestId('address-search-button').click()
  await waitLoadingDone()
}

async function resetSearch() {
  await page.getByTestId('address-reset-button').click()
  await waitLoadingDone()
}

async function selectRow(index) {
  await tableRows().nth(index).locator('.el-checkbox').click()
}

try {
  await seedTestRows()
  await page.goto(`${BASE}/addresses`, { waitUntil: 'networkidle' })
  await page.getByRole('heading', { name: '收件地址簿' }).waitFor()
  await waitLoadingDone()

  // 1. Page and real list
  assert.equal(await page.getByTestId('address-total').innerText(), '11')
  assert.equal(await tableRows().count(), 10)
  assert.equal(await page.locator('.side-nav__item.is-active').innerText(), '地址簿管理')

  // 2. Name / phone / address search
  await search('列表测试')
  await waitRows(10)
  assert.equal(await page.getByTestId('address-total').innerText(), '11')
  await search(`${PHONE_PREFIX}00005`)
  await waitRows(1)
  assert.match(await tableRows().first().innerText(), /19910000005/)
  await search('测试小区5')
  await waitRows(1)
  assert.match(await tableRows().first().innerText(), /测试小区5/)

  // 3. Percent and underscore are literal
  await search('%')
  await waitRows(1)
  assert.match(await tableRows().first().innerText(), /%/)
  await search('_')
  await waitRows(1)
  assert.match(await tableRows().first().innerText(), /_/)

  // 4. 50 / 51 Unicode code points
  await search('西'.repeat(50))
  assert.equal(await tableRows().count(), 0)
  await page.getByTestId('address-search-input').fill('西'.repeat(51))
  await page.waitForTimeout(250)
  assert.ok((await page.locator('.search-bar').innerText()).includes('关键词最多 50 个 Unicode 码点'))
  assert.equal(await page.getByTestId('address-total').innerText(), '0')

  // 5. Reset
  await resetSearch()
  await waitRows(10)
  assert.equal(await page.getByTestId('address-total').innerText(), '11')

  // 6. Pagination and page size
  await page.locator('.el-pagination .btn-next').click()
  await waitLoadingDone()
  await waitRows(1)
  assert.match(await page.locator('.el-pagination .el-pager li.is-active').innerText(), /2/)
  await page.locator('.el-pagination .el-select').click()
  await page.locator('.el-select-dropdown__item').filter({ hasText: /^20(\/page|条\/页)$/ }).click()
  await waitLoadingDone()
  assert.equal(await page.getByTestId('address-total').innerText(), '11')
  await page.locator('.el-pagination .el-select').click()
  await page.locator('.el-select-dropdown__item').filter({ hasText: /^10(\/page|条\/页)$/ }).click()
  await waitLoadingDone()

  // 7. Selection clears on page/search
  await selectRow(0)
  assert.equal(await page.getByTestId('selected-count').innerText(), '1')
  await page.locator('.el-pagination .btn-next').click()
  await waitLoadingDone()
  assert.equal(await page.getByTestId('selected-count').innerText(), '0')
  await selectRow(0)
  assert.equal(await page.getByTestId('selected-count').innerText(), '1')
  await search('列表测试')
  await waitLoadingDone()
  assert.equal(await page.getByTestId('selected-count').innerText(), '0')
  await resetSearch()
  await waitLoadingDone()

  // 8. Delete the only row on page 2, auto back to page 1
  await page.locator('.el-pagination .btn-next').click()
  await waitLoadingDone()
  await waitRows(1)
  await page.getByTestId('delete-address').first().click()
  await page.getByRole('button', { name: '确认删除' }).click()
  await waitLoadingDone()
  await waitRows(10)
  assert.match(await page.locator('.el-pagination .el-pager li.is-active').innerText(), /1/)
  assert.equal(await page.getByTestId('address-total').innerText(), '10')

  // 9. Create, with 51-code-point frontend validation
  await page.getByTestId('create-address').click()
  await page.locator('.el-dialog:visible').waitFor()
  await page.getByTestId('address-form-name').fill('新'.repeat(51))
  await page.getByTestId('address-form-submit').click()
  await page.waitForTimeout(150)
  assert.ok((await page.locator('.el-dialog:visible').innerText()).includes('不能超过 50'))
  await page.getByTestId('address-form-name').fill('新建测试')
  await page.getByTestId('address-form-phone').fill('19910000031')
  await page.getByTestId('address-form-address').fill('浙江省杭州市新建小区1号')
  await page.getByTestId('address-form-submit').click()
  await page.locator('.el-dialog:visible').waitFor({ state: 'hidden' })
  await waitLoadingDone()
  await search('19910000031')
  await waitRows(1)
  assert.match(await tableRows().first().innerText(), /新建测试/)

  // 10. Edit persists after refresh
  await page.getByTestId('edit-address').first().click()
  await page.locator('.el-dialog:visible').waitFor()
  await page.getByTestId('address-form-name').fill('编辑测试')
  await page.getByTestId('address-form-address').fill('浙江省杭州市编辑小区2号')
  await page.getByTestId('address-form-submit').click()
  await page.locator('.el-dialog:visible').waitFor({ state: 'hidden' })
  await waitLoadingDone()
  await page.getByTestId('address-refresh').click()
  await waitLoadingDone()
  assert.match(await tableRows().first().innerText(), /编辑测试/)

  // 11. Single delete
  await page.getByTestId('delete-address').first().click()
  await page.getByRole('button', { name: '确认删除' }).click()
  await waitLoadingDone()
  await waitRows(0)
  assert.equal(await page.getByTestId('address-total').innerText(), '0')

  // 12. Empty search result state
  assert.ok((await page.getByTestId('address-table').innerText()).includes('没有找到匹配的地址'))

  // 13. Batch delete sends only selected current-page IDs
  await resetSearch()
  await waitLoadingDone()
  await waitRows(10)
  await selectRow(0)
  await selectRow(1)
  assert.equal(await page.getByTestId('selected-count').innerText(), '2')
  let batchBody = null
  const captureBatchRequest = (request) => {
    if (request.url().endsWith('/api/addresses/batch-delete') && request.method() === 'POST') {
      batchBody = request.postDataJSON()
    }
  }
  page.on('request', captureBatchRequest)
  await page.getByTestId('batch-delete-open').click()
  await page.locator('.el-dialog:visible').waitFor()
  await page.getByTestId('batch-delete-confirm').click()
  await waitLoadingDone()
  page.off('request', captureBatchRequest)
  await page.waitForFunction(
    () => document.querySelector('[data-testid="selected-count"]')?.textContent?.trim() === '0'
  )
  assert.ok(batchBody)
  assert.equal(batchBody.ids.length, 2)
  assert.equal(await page.getByTestId('selected-count').innerText(), '0')

  // 14. Search returns no result
  await search('绝对不存在的关键词')
  await waitLoadingDone()
  assert.equal(await tableRows().count(), 0)
  assert.ok((await page.getByTestId('address-table').innerText()).includes('没有找到匹配的地址'))

  // 15. True empty state after deleting all remaining current-page rows
  await resetSearch()
  await waitLoadingDone()
  await waitRows(8)
  for (let i = 0; i < 8; i += 1) {
    await selectRow(i)
  }
  assert.equal(await page.getByTestId('selected-count').innerText(), '8')
  await page.getByTestId('batch-delete-open').click()
  await page.locator('.el-dialog:visible').waitFor()
  await page.getByTestId('batch-delete-confirm').click()
  await waitLoadingDone()
  await waitRows(0)
  assert.equal(await page.getByTestId('address-total').innerText(), '0')
  assert.ok((await page.getByTestId('address-table').innerText()).includes('地址簿还是空的'))

  // 16. Request failure state (browser-level simulated 50002)
  await page.route('**/api/addresses?*', async (route) => {
    if (route.request().method() === 'GET') {
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ code: 50002, message: '数据库操作失败，请稍后重试', data: null })
      })
    } else {
      await route.continue()
    }
  })
  await page.getByTestId('address-refresh').click()
  await page.getByTestId('address-list-error').waitFor()
  assert.match(await page.getByTestId('address-list-error').innerText(), /数据库操作失败/)
  await page.unroute('**/api/addresses?*')
  await page.getByTestId('address-refresh').click()
  await waitLoadingDone()

  const unexpectedConsoleErrors = consoleErrors.filter(
    (text) => !text.includes('500 (Internal Server Error)')
  )
  assert.deepEqual(unexpectedConsoleErrors, [])
  console.log('MS05_BROWSER_OK')
} finally {
  await cleanupTestRows()
  await browser.close()
}
