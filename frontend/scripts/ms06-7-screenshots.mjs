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
  await page.goto(`${BASE}/web-basics/wireframe?mode=plan`, { waitUntil: 'networkidle' })
  const board = page.getByTestId('wireframe-plan-board')
  await board.waitFor()
  await board.scrollIntoViewIfNeeded()
  await page.waitForTimeout(160)
  await board.screenshot({ path: `${outputDir}/2-18-wireframe-plan.png` })
  console.log('SCREENSHOT 2-18-wireframe-plan.png')

  await page.goto(`${BASE}/web-basics/wireframe?mode=implemented&page=import`, { waitUntil: 'networkidle' })
  await page.locator('.import-page').waitFor()
  await page.evaluate(() => window.scrollTo(0, 0))
  await page.waitForTimeout(200)
  await page.screenshot({ path: `${outputDir}/2-18-implemented.png` })
  console.log('SCREENSHOT 2-18-implemented.png')

  console.log('MS06_7_SCREENSHOTS_OK')
} finally {
  await browser.close()
}
