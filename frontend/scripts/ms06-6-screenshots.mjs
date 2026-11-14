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
  await page.getByTestId('web-basics-page').waitFor()

  for (const [chapter, filename] of [
    ['2.16', '2-16-offsets.png'],
    ['2.17', '2-17-offset-override.png']
  ]) {
    const section = page.locator(`[id="chapter-${chapter}"]`)
    await section.scrollIntoViewIfNeeded()
    await page.waitForTimeout(160)
    await section.screenshot({ path: `${outputDir}/${filename}` })
    console.log(`SCREENSHOT ${filename}`)
  }

  const stacking = page.getByTestId('stacking-supplement')
  await stacking.scrollIntoViewIfNeeded()
  await page.waitForTimeout(160)
  await stacking.screenshot({ path: `${outputDir}/2-17-stacking.png` })
  console.log('SCREENSHOT 2-17-stacking.png')

  console.log('MS06_6_SCREENSHOTS_OK')
} finally {
  await browser.close()
}
