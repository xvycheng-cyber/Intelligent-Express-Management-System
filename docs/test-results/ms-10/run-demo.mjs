import { chromium } from 'file:///E:/desktop/高级Web&J2EE期末/Intelligent-Express-Management-System/frontend/node_modules/playwright/index.mjs'
import { writeFileSync } from 'node:fs'

const BASE = 'http://127.0.0.1:5173'
const OUT = 'E:/desktop/高级Web&J2EE期末/Intelligent-Express-Management-System/docs/test-results/ms-10'
const browser = await chromium.launch({ channel: 'chrome', headless: true })
const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } })
const evidence = { five: [], ten: [] }
const started = performance.now()

function log(section, step, detail, status = 'PASS') {
  const item = { step, detail, status, atMs: Math.round(performance.now() - started) }
  evidence[section].push(item)
  console.log(section, step, status, detail)
}

async function open(path) {
  await page.goto(`${BASE}${path}`, { waitUntil: 'networkidle' })
  await page.waitForTimeout(200)
}

function rows() {
  return page.locator('[data-testid="address-table"] tbody tr')
}

async function waitAddressRows(count) {
  await page.waitForFunction(
    (expected) => document.querySelectorAll('[data-testid="address-table"] tbody tr').length === expected,
    count,
    { timeout: 15000 }
  )
}

async function search(keyword) {
  await page.getByTestId('address-search-input').fill(keyword)
  await page.getByTestId('address-search-button').click()
  await page.waitForTimeout(800)
}

async function saveCurrent(text) {
  const ta = page.locator('textarea').first()
  await ta.fill(text)
  await page.getByTestId('parse-button').click()
  await page.waitForTimeout(800)
}

async function fillInvalidRow(index, name, phone, address) {
  const row = page.locator('[data-testid="parse-result-table"] tbody tr').nth(index)
  const inputs = row.locator('input')
  await inputs.nth(0).fill(name)
  await inputs.nth(1).fill(phone)
  await row.locator('textarea').fill(address)
  await page.waitForTimeout(200)
}

try {
  // ---------- 5 minute: 3 rows, 2 valid / 1 invalid ----------
  await open('/import')
  await page.screenshot({ path: `${OUT}/five-import.png` })
  await saveCurrent([
    'MSFIVEAA，17790004001，浙江省杭州市西湖区MS10-DEMO-5-A',
    'MSFIVEAB，17790004002，浙江省杭州市余杭区MS10-DEMO-5-B',
    'MSFIVEAX'
  ].join('\n'))
  const parse5 = await page.locator('[data-testid="parse-result-table"] tbody tr').count()
  log('five', 'parse', `rows=${parse5}`, parse5 === 3 ? 'PASS' : 'FAIL')
  await page.screenshot({ path: `${OUT}/five-parse.png` })
  await fillInvalidRow(2, 'MSFIVEAX', '17790004003', '浙江省杭州市西湖区MS10-DEMO-5-C')
  await page.getByTestId('open-save-dialog').click()
  await page.locator('.el-dialog').waitFor()
  log('five', 'save-dialog', (await page.locator('.el-dialog').innerText()).slice(0, 80))
  await page.getByTestId('confirm-save').click()
  await page.getByTestId('save-success').waitFor()
  log('five', 'save', await page.getByTestId('save-success').innerText())
  await page.screenshot({ path: `${OUT}/five-save.png` })

  await open('/addresses')
  await search('MSFIVE')
  await waitAddressRows(3)
  log('five', 'list-search', `rows=${await rows().count()}`)
  await page.getByTestId('edit-address').first().click()
  await page.locator('.el-dialog').waitFor()
  await page.getByTestId('address-form-address').fill('浙江省杭州市西湖区MS10-DEMO-5-EDITED')
  await page.getByTestId('address-form-submit').click()
  await page.waitForTimeout(800)
  await search('MSFIVE')
  await waitAddressRows(3)
  log('five', 'edit', 'first record address updated')
  await page.screenshot({ path: `${OUT}/five-list.png` })
  await page.getByTestId('delete-address').first().click()
  await page.getByRole('button', { name: '确认删除' }).click()
  await page.waitForTimeout(800)
  await search('MSFIVE')
  log('five', 'delete', `rows=${await rows().count()}`)

  // ---------- 10 minute: 5 rows, 3 valid / 2 invalid ----------
  await open('/import')
  await saveCurrent([
    'MSTENAA，17790005001，浙江省杭州市西湖区MS10-DEMO-10-A',
    'MSTENAB，17790005002，浙江省杭州市余杭区MS10-DEMO-10-B',
    'MSTENAX',
    'MSTENAC，0571-88886666，浙江省杭州市拱墅区MS10-DEMO-10-C',
    'MSTENAZ，1779000500，浙江省杭州市滨江区MS10-DEMO-10-D'
  ].join('\n'))
  const parse10 = await page.locator('[data-testid="parse-result-table"] tbody tr').count()
  log('ten', 'parse', `rows=${parse10}`, parse10 === 5 ? 'PASS' : 'FAIL')
  await page.screenshot({ path: `${OUT}/ten-parse.png` })
  await fillInvalidRow(2, 'MSTENAX', '17790005003', '浙江省杭州市西湖区MS10-DEMO-10-X')
  await fillInvalidRow(4, 'MSTENAZ', '17790005004', '浙江省杭州市滨江区MS10-DEMO-10-Z')
  await page.getByTestId('open-save-dialog').click()
  await page.locator('.el-dialog').waitFor()
  await page.getByTestId('confirm-save').click()
  await page.getByTestId('save-success').waitFor()
  log('ten', 'save', await page.getByTestId('save-success').innerText())
  await page.screenshot({ path: `${OUT}/ten-save.png` })

  await open('/addresses')
  await search('MSTEN')
  await waitAddressRows(5)
  log('ten', 'list-search', `rows=${await rows().count()}`)
  await page.screenshot({ path: `${OUT}/ten-list.png` })

  // New record
  await page.getByTestId('create-address').click()
  await page.locator('.el-dialog').waitFor()
  await page.getByTestId('address-form-name').fill('MSTENNEW')
  await page.getByTestId('address-form-phone').fill('17790005005')
  await page.getByTestId('address-form-address').fill('浙江省杭州市西湖区MS10-DEMO-NEW')
  await page.getByTestId('address-form-submit').click()
  await page.waitForTimeout(800)
  await search('MSTENNEW')
  await waitAddressRows(1)
  log('ten', 'create', 'MSTENNEW created')
  await page.getByTestId('edit-address').first().click()
  await page.locator('.el-dialog').waitFor()
  await page.getByTestId('address-form-address').fill('浙江省杭州市西湖区MS10-DEMO-NEW-EDITED')
  await page.getByTestId('address-form-submit').click()
  await page.waitForTimeout(800)
  log('ten', 'edit', 'MSTENNEW edited')
  await page.getByTestId('delete-address').first().click()
  await page.getByRole('button', { name: '确认删除' }).click()
  await page.waitForTimeout(800)
  await search('MSTEN')
  log('ten', 'single-delete', `rows=${await rows().count()}`)

  // batch delete first two of remaining page
  await rows().nth(0).locator('.el-checkbox').click()
  await rows().nth(1).locator('.el-checkbox').click()
  await page.getByTestId('batch-delete-open').click()
  await page.getByTestId('batch-delete-confirm').click()
  await page.waitForTimeout(800)
  log('ten', 'batch-delete', `rows=${await rows().count()}`)

  await open('/web-basics')
  await page.getByTestId('web-basics-page').waitFor()
  log('ten', 'web-basics', 'web-basics-page loaded')
  await page.screenshot({ path: `${OUT}/ten-web-basics.png` })

  await open('/servlet-demo')
  await page.getByTestId('servlet-demo-page').waitFor()
  log('ten', 'servlet-demo', 'servlet-demo-page loaded')
  await page.screenshot({ path: `${OUT}/ten-servlet-demo.png` })

  writeFileSync(`${OUT}/ui-demo-evidence.json`, JSON.stringify({ evidence, elapsedMs: Math.round(performance.now() - started) }, null, 2), 'utf8')
  console.log('MS10_DEMO_OK')
} catch (error) {
  console.error('MS10_DEMO_FAIL', error)
  writeFileSync(`${OUT}/ui-demo-evidence.json`, JSON.stringify({ evidence, error: String(error), elapsedMs: Math.round(performance.now() - started) }, null, 2), 'utf8')
  process.exitCode = 1
} finally {
  await browser.close()
}
