import assert from 'node:assert/strict'
import { chromium } from 'playwright'

const BASE = process.env.MS06_BASE_URL ?? 'http://127.0.0.1:5173'
const browser = await chromium.launch({ channel: 'chrome', headless: true })
const page = await browser.newPage({ viewport: { width: 1366, height: 900 } })
const consoleErrors = []
page.on('console', (message) => {
  if (message.type() === 'error') consoleErrors.push(message.text())
})
page.on('pageerror', (error) => consoleErrors.push(`pageerror: ${error.message}`))

async function open(path) {
  await page.goto(`${BASE}${path}`, { waitUntil: 'networkidle' })
  await page.waitForTimeout(120)
}

async function noOverflow(label) {
  await page.waitForTimeout(100)
  const dimensions = await page.evaluate(() => ({
    scrollWidth: document.documentElement.scrollWidth,
    innerWidth: window.innerWidth
  }))
  assert.ok(dimensions.scrollWidth <= dimensions.innerWidth, `${label}: ${JSON.stringify(dimensions)}`)
}

try {
  // Plan mode is the default; it is a real browser-rendered low-fi board.
  await open('/web-basics/wireframe')
  await page.getByTestId('wireframe-page').waitFor()
  assert.equal(await page.getByTestId('wireframe-plan-panel').count(), 1)
  assert.equal(await page.getByTestId('wireframe-plan-board').count(), 1)
  assert.equal(await page.getByTestId('plan-import-page').count(), 1)
  assert.equal(await page.getByTestId('plan-address-page').count(), 1)
  assert.match(await page.getByTestId('wireframe-plan-board').innerText(), /批量地址录入/)
  assert.match(await page.getByTestId('wireframe-plan-board').innerText(), /地址簿管理/)
  assert.equal(await page.locator('[data-testid="wireframe-page"] img').count(), 0)
  assert.equal(await page.locator('[data-testid="wireframe-page"]').getByText('2-18-original').count(), 0)
  assert.equal(await page.locator('nav[aria-label="主导航"]').count(), 1)

  // Implemented mode renders real Vue page components, not screenshots.
  await page.getByTestId('wireframe-mode-implemented').click()
  await page.waitForURL('**/web-basics/wireframe?mode=implemented')
  await page.getByTestId('wireframe-implemented-panel').waitFor()
  assert.equal(await page.getByTestId('wireframe-plan-panel').count(), 0)
  assert.equal(await page.getByTestId('wireframe-preview-import').count(), 1)
  assert.equal(await page.locator('[data-testid="wireframe-page"] img').count(), 0)
  assert.equal(await page.locator('.import-page').count(), 1)
  assert.equal(await page.locator('.address-page').count(), 0)
  assert.equal(await page.getByTestId('wireframe-live-preview').getAttribute('inert'), '')

  // Switch to address book live preview and verify actual DOM + route query.
  await page.getByTestId('preview-page-addresses').click()
  await page.waitForURL('**/web-basics/wireframe?mode=implemented&page=addresses')
  await page.getByTestId('wireframe-preview-addresses').waitFor()
  await page.locator('.address-page').waitFor()
  assert.equal(await page.locator('.import-page').count(), 0)
  assert.equal(await page.locator('.address-page').count(), 1)
  assert.equal(await page.getByTestId('wireframe-live-preview').getAttribute('inert'), '')

  // Refresh keeps both mode and page selection.
  await page.reload({ waitUntil: 'networkidle' })
  await page.locator('.address-page').waitFor()
  assert.equal(await page.getByTestId('wireframe-implemented-panel').count(), 1)
  assert.equal(await page.locator('.address-page').count(), 1)

  // Direct query and plan query.
  await open('/web-basics/wireframe?mode=implemented&page=import')
  await page.locator('.import-page').waitFor()
  assert.equal(await page.getByTestId('wireframe-implemented-panel').count(), 1)
  await open('/web-basics/wireframe?mode=plan')
  assert.equal(await page.getByTestId('wireframe-plan-panel').count(), 1)
  assert.equal(await page.getByTestId('wireframe-plan-board').count(), 1)

  // Servlet remains a non-route; no fake page was created.
  await open('/servlet-demo')
  await page.getByRole('heading', { name: '页面不存在' }).waitFor()
  assert.equal(await page.locator('a[href="/servlet-demo"]').count(), 0)

  // Responsive: page shell no overflow; live previews remain reachable.
  for (const width of [1366, 980, 760, 390]) {
    await page.setViewportSize({ width, height: 900 })
    await open('/web-basics/wireframe?mode=plan')
    await page.getByTestId('wireframe-plan-board').waitFor()
    await noOverflow(`plan ${width}`)
    await open('/web-basics/wireframe?mode=implemented&page=import')
    await page.locator('.import-page').waitFor()
    await noOverflow(`implemented import ${width}`)
    await open('/web-basics/wireframe?mode=implemented&page=addresses')
    await page.locator('.address-page').waitFor()
    await noOverflow(`implemented addresses ${width}`)
  }

  assert.deepEqual(consoleErrors, [], `console errors: ${consoleErrors.join(' | ')}`)
  console.log('MS06_7_BROWSER_OK')
} finally {
  await browser.close()
}
