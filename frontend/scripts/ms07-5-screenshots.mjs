import { mkdirSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { chromium } from 'playwright'

const BASE = process.env.MS07_BASE_URL ?? 'http://127.0.0.1:5173'
const outputDir =
  process.env.MS07_SCREENSHOT_DIR ??
  fileURLToPath(new URL('../../docs/screenshots/', import.meta.url))
mkdirSync(outputDir, { recursive: true })

const browser = await chromium.launch({ channel: 'chrome', headless: true })
const page = await browser.newPage({ viewport: { width: 1366, height: 900 }, deviceScaleFactor: 1 })

try {
  await page.goto(`${BASE}/servlet-demo`, { waitUntil: 'networkidle' })
  await page.getByTestId('servlet-demo-page').waitFor()

  await page.getByTestId('servlet-run-all').click()
  await page.waitForFunction(() => {
    const button = document.querySelector('[data-testid="servlet-run-all"]')
    const queryStatus = document.querySelector('[data-testid="status-query"]')?.textContent ?? ''
    return button && !button.hasAttribute('disabled') && queryStatus.includes('200')
  }, { timeout: 20_000 })

  // Make the detail panel show an intentional 404 (matrix still keeps all statuses).
  await page.getByTestId('run-default-404').click()
  await page.waitForFunction(() => {
    const text = document.querySelector('[data-testid="active-status"]')?.textContent ?? ''
    return text.includes('404')
  }, { timeout: 10_000 })
  await page.waitForTimeout(150)

  await page.evaluate(() => window.scrollTo(0, 0))
  await page.waitForTimeout(100)
  await page.screenshot({ path: `${outputDir}/2-20-servlet-mapping.png`, fullPage: true })
  console.log('SCREENSHOT 2-20-servlet-mapping.png')

  await page.locator('.servlet-lab').screenshot({ path: `${outputDir}/2-20-servlet-mapping-requests.png` })
  console.log('SCREENSHOT 2-20-servlet-mapping-requests.png')

  await page.locator('.servlet-detail-grid').screenshot({ path: `${outputDir}/2-20-servlet-mapping-http.png` })
  console.log('SCREENSHOT 2-20-servlet-mapping-http.png')

  console.log('MS07_5_SCREENSHOTS_OK')
} finally {
  await browser.close()
}
