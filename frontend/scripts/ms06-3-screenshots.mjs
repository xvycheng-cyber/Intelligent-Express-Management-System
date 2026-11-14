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
const sections = [
  ['2.1', '2-01-inline.png'],
  ['2.2', '2-02-block.png'],
  ['2.3', '2-03-inline-block.png'],
  ['2.4', '2-04-flex.png'],
  ['2.5', '2-05-grid.png']
]

try {
  await page.goto(`${BASE}/web-basics`, { waitUntil: 'networkidle' })
  await page.getByTestId('web-basics-page').waitFor()
  for (const [chapter, filename] of sections) {
    const section = page.locator(`[id="chapter-${chapter}"]`)
    await section.scrollIntoViewIfNeeded()
    await page.waitForTimeout(160)
    await section.screenshot({ path: `${outputDir}/${filename}` })
    console.log(`SCREENSHOT ${filename}`)
  }
  console.log('MS06_3_SCREENSHOTS_OK')
} finally {
  await browser.close()
}
