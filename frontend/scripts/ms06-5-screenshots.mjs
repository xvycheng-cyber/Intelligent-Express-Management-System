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
    ['2.11', '2-11-absolute.png'],
    ['2.12', '2-12-absolute-context.png']
  ]) {
    const section = page.locator(`[id="chapter-${chapter}"]`)
    await section.scrollIntoViewIfNeeded()
    await page.waitForTimeout(160)
    await section.screenshot({ path: `${outputDir}/${filename}` })
    console.log(`SCREENSHOT ${filename}`)
  }

  // 2.13: viewport screenshot so the true position:fixed badge is visible.
  const fixedSection = page.locator('[id="chapter-2.13"]')
  await fixedSection.scrollIntoViewIfNeeded()
  await page.waitForTimeout(160)
  await page.screenshot({ path: `${outputDir}/2-13-fixed.png` })
  console.log('SCREENSHOT 2-13-fixed.png')

  // 2.14: scroll the real sticky frame so the screenshot shows the stuck state.
  const stickySection = page.locator('[id="chapter-2.14"]')
  await stickySection.scrollIntoViewIfNeeded()
  await page.getByTestId('sticky-frame').evaluate((element) => { element.scrollTop = 180 })
  await page.waitForTimeout(160)
  await stickySection.screenshot({ path: `${outputDir}/2-14-sticky.png` })
  console.log('SCREENSHOT 2-14-sticky.png')

  // 2.15: scroll the constraint frame to the boundary.
  const constraintSection = page.locator('[id="chapter-2.15"]')
  await constraintSection.scrollIntoViewIfNeeded()
  await page.getByTestId('constraint-frame').evaluate((element) => { element.scrollTop = 220 })
  await page.waitForTimeout(160)
  await constraintSection.screenshot({ path: `${outputDir}/2-15-sticky-rect.png` })
  console.log('SCREENSHOT 2-15-sticky-rect.png')

  console.log('MS06_5_SCREENSHOTS_OK')
} finally {
  await browser.close()
}
