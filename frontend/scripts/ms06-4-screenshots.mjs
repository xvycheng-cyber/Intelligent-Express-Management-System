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
  ['2.6', '2-06-box-parts.png'],
  ['2.7', '2-07-content-box.png'],
  ['2.8', '2-08-border-box.png'],
  ['2.9', '2-09-normal-flow.png'],
  ['2.10', '2-10-relative.png']
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
  console.log('MS06_4_SCREENSHOTS_OK')
} finally {
  await browser.close()
}
