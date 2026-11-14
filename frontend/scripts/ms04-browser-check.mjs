import assert from 'node:assert/strict'
import { chromium } from 'playwright'

const BASE = process.env.MS04_BASE_URL ?? 'http://127.0.0.1:5173'
const browser = await chromium.launch({ channel: 'chrome', headless: true })
const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } })
const consoleErrors = []
page.on('console', (msg) => {
  if (msg.type() === 'error') consoleErrors.push(msg.text())
})
page.on('pageerror', (error) => consoleErrors.push(`pageerror: ${error.message}`))

function tableRows() {
  return page.locator('[data-testid="parse-result-table"] tbody tr')
}

async function waitRows(count) {
  await page.waitForFunction(
    (expected) => document.querySelectorAll('[data-testid="parse-result-table"] tbody tr').length === expected,
    count,
    { timeout: 15_000 }
  )
}

async function parse(text) {
  const raw = page.locator('[data-testid="raw-text-input"] textarea')
  const textarea = (await raw.count()) > 0 ? raw : page.locator('textarea').first()
  await textarea.fill(text)
  await page.getByTestId('parse-button').click()
}

try {
  await page.goto(`${BASE}/import`, { waitUntil: 'networkidle' })
  await page.getByRole('heading', { name: '批量录入收件地址' }).waitFor()
  assert.equal(await page.locator('nav[aria-label="主导航"] .side-nav__item').count(), 2)
  assert.equal(await page.getByTestId('empty-state').count(), 1)

  // 1) 单条联系人 + 中文逗号
  await parse('菜小鸟，13800138000，浙江省杭州市西湖区湖滨小区12栋301室')
  await waitRows(1)
  assert.equal(await tableRows().count(), 1)
  assert.match(await page.locator('.stat-card').first().innerText(), /总记录\s*1/)
  assert.match(await page.locator('.stat-card').nth(1).innerText(), /有效记录\s*1/)

  // 2) 多条 + 英文逗号 + Tab + 空行 + INVALID
  await parse(
    '张小明,13900139000,浙江省杭州市滨江区某某小区5栋202室\n' +
      '\n' +
      '李女士\t057188888888\t浙江省杭州市拱墅区运河东路88号\n' +
      '只有姓名没有电话'
  )
  await waitRows(3)
  assert.equal(await tableRows().count(), 3)
  assert.ok((await page.locator('[data-testid="parse-result-table"]').innerText()).includes('待修正'))
  const saveButton = page.getByTestId('open-save-dialog')
  assert.equal(await saveButton.isEnabled(), true)

  // 3) 未修正 INVALID 不进入提交集合
  await saveButton.click()
  await page.locator('.el-dialog').waitFor()
  const dialogText = await page.locator('.el-dialog').innerText()
  assert.match(dialogText, /待修正行\s*1/)
  assert.match(dialogText, /实际提交\s*2/)
  await page.getByRole('button', { name: '返回修改' }).click()

  // 4) 修改姓名 / 电话 / 地址，并验证编辑统计
  const invalidRow = tableRows().nth(2)
  const invalidInputs = invalidRow.locator('input')
  await invalidInputs.nth(0).fill('王女士')
  await invalidInputs.nth(1).fill('13700137000')
  await invalidRow.locator('textarea').fill('浙江省杭州市余杭区文一西路100号')
  await page.waitForTimeout(200)
  assert.match(await page.locator('.stat-card').nth(3).innerText(), /人工修改\s*[1-9]/)

  // 5) 恢复原始值
  const firstRow = tableRows().nth(0)
  await firstRow.locator('input').nth(0).fill('临时改名')
  await page.waitForTimeout(100)
  assert.match(await page.locator('.stat-card').nth(3).innerText(), /人工修改\s*2/)
  await firstRow.getByTestId('restore-row').click()
  await page.waitForTimeout(100)
  assert.match(await page.locator('.stat-card').nth(3).innerText(), /人工修改\s*1/)

  // 6) Unicode：51 BMP 拒绝、50 扩展平面接受
  const unicodeName = '张'.repeat(51)
  await firstRow.locator('input').nth(0).fill(unicodeName)
  await page.waitForTimeout(100)
  assert.match(await firstRow.innerText(), /51 \/ 50/)
  const extName = '𠀀'.repeat(50)
  await firstRow.locator('input').nth(0).fill(extName)
  await page.waitForTimeout(100)
  assert.match(await firstRow.innerText(), /50 \/ 50/)

  // 7) 保存失败时保留输入和编辑结果（浏览器层模拟 50002）
  let failedCalls = 0
  await page.route('**/api/addresses/batch', async (route) => {
    failedCalls += 1
    await route.fulfill({
      status: 500,
      contentType: 'application/json',
      body: JSON.stringify({ code: 50002, message: '数据库操作失败，请稍后重试', data: null })
    })
  })
  await saveButton.click()
  await page.locator('.el-dialog').waitFor()
  await page.getByTestId('confirm-save').click()
  await page.getByTestId('save-error').waitFor()
  assert.equal(failedCalls, 1)
  assert.ok((await page.locator('textarea').first().inputValue()).includes('只有姓名没有电话'))
  assert.equal(await tableRows().count(), 3)
  await page.unroute('**/api/addresses/batch')

  // 8) 实际保存成功
  await saveButton.click()
  await page.locator('.el-dialog').waitFor()
  await page.getByTestId('confirm-save').click()
  await page.getByTestId('save-success').waitFor()
  assert.match(await page.getByTestId('save-success').innerText(), /已保存\s*3\s*条地址/)

  // 9) 连续点击防重复：独立拦截器统计 batch 请求数
  await page.goto(`${BASE}/import`, { waitUntil: 'networkidle' })
  await parse('防重甲，13800138001，浙江省杭州市A\n防重乙，13800138002，浙江省杭州市B')
  await waitRows(2)
  let duplicateCalls = 0
  await page.route('**/api/addresses/batch', async (route) => {
    duplicateCalls += 1
    await new Promise((resolve) => setTimeout(resolve, 250))
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ code: 0, message: 'OK', data: { savedCount: 2, ids: ['1', '2'] } })
    })
  })
  await page.getByTestId('open-save-dialog').click()
  const confirm = page.getByTestId('confirm-save')
  await Promise.all([confirm.click(), confirm.click({ timeout: 1000 }).catch(() => {})])
  await page.getByTestId('save-success').waitFor()
  assert.equal(duplicateCalls, 1)
  await page.unroute('**/api/addresses/batch')

  const unexpectedConsoleErrors = consoleErrors.filter(
    (text) => !text.includes('500 (Internal Server Error)')
  )
  assert.deepEqual(unexpectedConsoleErrors, [])
  console.log('MS04_BROWSER_OK')
} finally {
  await browser.close()
}
