import { chromium } from 'file:///E:/desktop/高级Web&J2EE期末/Intelligent-Express-Management-System/frontend/node_modules/playwright/index.mjs'

const BASE = 'http://127.0.0.1:5173'
const OUT = 'E:/desktop/高级Web&J2EE期末/Intelligent-Express-Management-System/docs/screenshots'
const browser = await chromium.launch({ channel: 'chrome', headless: true })
const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } })

try {
  await page.goto(`${BASE}/addresses`, { waitUntil: 'networkidle' })
  await page.waitForTimeout(1200)
  await page.getByTestId('address-search-input').fill('MSDEMO')
  await page.getByTestId('address-search-button').click()
  await page.waitForTimeout(1000)
  const rows = await page.locator('[data-testid="address-table"] tbody tr').count()
  if (rows < 3) throw new Error(`expected 3 demo rows, got ${rows}`)
  await page.locator('.address-page').screenshot({ path: `${OUT}/2-21-list.png` })
  console.log('list', rows)

  await page.getByTestId('edit-address').first().click()
  await page.locator('.el-dialog:visible').waitFor()
  await page.locator('.el-dialog:visible').screenshot({ path: `${OUT}/2-21-edit-dialog.png` })
  console.log('edit dialog')
  await page.keyboard.press('Escape')
  await page.waitForTimeout(400)

  await page.getByTestId('delete-address').first().click()
  await page.locator('.el-message-box').waitFor()
  await page.locator('.el-message-box').screenshot({ path: `${OUT}/2-21-delete-confirm.png` })
  console.log('delete confirm')
  await page.getByRole('button', { name: '取消' }).click()
  await page.waitForTimeout(400)

  const tableRows = page.locator('[data-testid="address-table"] tbody tr')
  await tableRows.nth(0).locator('.el-checkbox').click()
  await tableRows.nth(1).locator('.el-checkbox').click()
  await page.getByTestId('batch-delete-open').click()
  await page.locator('.el-dialog:visible').waitFor()
  await page.locator('.el-dialog:visible').screenshot({ path: `${OUT}/2-21-batch-delete.png` })
  console.log('batch dialog')
  await page.keyboard.press('Escape')

  console.log('MS12_CAPTURE_OK')
} finally {
  await browser.close()
}
