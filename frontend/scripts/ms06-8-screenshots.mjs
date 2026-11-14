import { mkdirSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { chromium } from 'playwright'

const BASE = process.env.MS06_BASE_URL ?? 'http://127.0.0.1:5173'
const outputDir =
  process.env.MS06_SCREENSHOT_DIR ??
  fileURLToPath(new URL('../../docs/screenshots/', import.meta.url))
mkdirSync(outputDir, { recursive: true })

const browser = await chromium.launch({ channel: 'chrome', headless: true })
const page = await browser.newPage({ viewport: { width: 1366, height: 900 }, deviceScaleFactor: 1 })

try {
  await page.goto(`${BASE}/web-basics`, { waitUntil: 'networkidle' })
  await page.getByTestId('demo-section-2.19').waitFor()
  await page.waitForTimeout(250)
  await page.evaluate(() => window.scrollTo(0, 0))
  await page.waitForTimeout(150)
  await page.screenshot({ path: `${outputDir}/2-19-principles.png` })
  console.log('SCREENSHOT 2-19-principles.png')
  console.log('MS06_8_SCREENSHOTS_OK')
} finally {
  await browser.close()
}
