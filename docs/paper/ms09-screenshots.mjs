import { mkdirSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { chromium } from 'playwright'

const BASE = process.env.MS09_BASE_URL ?? 'http://127.0.0.1:5173'
const outputDir =
  process.env.MS09_SCREENSHOT_DIR ??
  fileURLToPath(new URL('../../docs/screenshots/', import.meta.url))
mkdirSync(outputDir, { recursive: true })

const browser = await chromium.launch({ channel: 'chrome', headless: true })
const page = await browser.newPage({ viewport: { width: 1440, height: 1000 }, deviceScaleFactor: 1 })

try {
  await page.goto(`${BASE}/import`, { waitUntil: 'networkidle' })
  await page.getByRole('heading', { name: '批量录入收件地址' }).waitFor()
  await page.locator('.import-page').screenshot({ path: `${outputDir}/2-21-import.png` })
  console.log('SCREENSHOT 2-21-import.png')

  await page.locator('textarea').first().fill([
    '菜小鸟，13800138000，浙江省杭州市西湖区湖滨小区12栋301室',
    '张三，13900139000，浙江省杭州市余杭区文一西路100号',
    '只有姓名没有电话'
  ].join('\n'))
  await page.getByTestId('parse-button').click()
  await page.waitForFunction(() => document.querySelectorAll('[data-testid="parse-result-table"] tbody tr').length === 3)
  await page.locator('.import-page').screenshot({ path: `${outputDir}/2-21-parse-result.png` })
  console.log('SCREENSHOT 2-21-parse-result.png')

  const firstRow = page.locator('[data-testid="parse-result-table"] tbody tr').first()
  await firstRow.locator('input').first().fill('菜小鸟（已核对）')
  await page.getByTestId('open-save-dialog').click()
  await page.locator('.el-dialog:visible').waitFor()
  await page.locator('.el-dialog:visible').screenshot({ path: `${outputDir}/2-21-edit-preview.png` })
  console.log('SCREENSHOT 2-21-edit-preview.png')

  console.log('MS09_SCREENSHOTS_OK')
} finally {
  await browser.close()
}
