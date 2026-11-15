import assert from 'node:assert/strict'
import { chromium } from 'playwright'

const BASE = process.env.MS08_EDGE_BASE_URL ?? 'http://127.0.0.1:5173'
const PHONE = '17700000999'
const browser = await chromium.launch({ channel: 'msedge', headless: true })
const page = await browser.newPage({ viewport: { width: 1280, height: 720 } })
const pageErrors = []
page.on('pageerror', (error) => pageErrors.push(error.message))

async function open(path) {
  await page.goto(`${BASE}${path}`, { waitUntil: 'networkidle' })
  await page.waitForTimeout(100)
}

async function noOverflow(label) {
  const dimensions = await page.evaluate(() => ({
    scrollWidth: document.documentElement.scrollWidth,
    innerWidth: window.innerWidth
  }))
  assert.ok(dimensions.scrollWidth <= dimensions.innerWidth, `${label}: ${JSON.stringify(dimensions)}`)
}

try {
  // Real Edge import → parse → save.
  await open('/import')
  await page.getByRole('heading', { name: '批量录入收件地址' }).waitFor()
  await page.locator('textarea').first().fill(`测试，${PHONE}，浙江省杭州市西湖区Edge路1号`)
  await page.getByTestId('parse-button').click()
  await page.waitForFunction(() => document.querySelectorAll('[data-testid="parse-result-table"] tbody tr').length === 1)
  await page.getByTestId('open-save-dialog').click()
  await page.getByTestId('confirm-save').click()
  await page.getByTestId('save-success').waitFor()

  // Real Edge address list → search → delete.
  await open('/addresses')
  await page.getByRole('heading', { name: '收件地址簿' }).waitFor()
  await page.getByTestId('address-search-input').fill(PHONE)
  await page.getByTestId('address-search-button').click()
  await page.waitForFunction(() => !document.querySelector('[data-testid="address-table"] .el-loading-mask'))
  await page.getByTestId('delete-address').first().click()
  await page.getByRole('button', { name: '确认删除' }).click()
  await page.waitForFunction(() => !document.querySelector('[data-testid="address-table"] .el-loading-mask'))

  await open('/web-basics')
  await page.getByTestId('web-basics-page').waitFor()
  await open('/web-basics/wireframe')
  await page.getByTestId('wireframe-page').waitFor()

  await open('/servlet-demo')
  await page.getByTestId('servlet-demo-page').waitFor()
  await page.getByTestId('run-exact').click()
  await page.waitForFunction(() => document.querySelector('[data-testid="status-exact"]')?.textContent?.includes('200'))
  assert.match(await page.getByTestId('actual-exact').innerText(), /ExactMappingDemoServlet/)

  const paths = ['/import', '/addresses', '/web-basics', '/web-basics/wireframe', '/servlet-demo']
  for (const width of [1280, 1366, 980, 760, 390]) {
    await page.setViewportSize({ width, height: Math.max(720, Math.round(width * 0.7)) })
    for (const path of paths) {
      await open(path)
      await noOverflow(`${path} ${width}`)
    }
  }

  assert.deepEqual(pageErrors, [])
  console.log('MS08_EDGE_OK')
} finally {
  await browser.close()
}
