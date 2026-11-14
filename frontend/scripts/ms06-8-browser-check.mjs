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
  await page.waitForTimeout(150)
}

async function rect(selector) {
  return page.locator(selector).evaluate((element) => {
    const box = element.getBoundingClientRect()
    return { width: box.width, height: box.height, top: box.top, left: box.left }
  })
}

async function outline(selector) {
  return page.locator(selector).evaluate((element) => {
    const style = getComputedStyle(element)
    return { style: style.outlineStyle, width: style.outlineWidth, color: style.outlineColor }
  })
}

async function highlighted() {
  return page.locator('.app-shell').evaluate((element) => element.classList.contains('wb-layout-highlight'))
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
  await open('/web-basics#chapter-2.19')
  await page.getByTestId('web-basics-page').waitFor()
  await page.getByTestId('demo-section-2.19').waitFor()

  assert.equal(await page.getByTestId('chapter-2.19').locator('.wb-status').innerText(), '已实现')
  assert.equal(await page.getByTestId('chapter-2.18').locator('.wb-status').innerText(), '已实现')
  assert.equal(await page.getByTestId('layout-highlight-toggle').isChecked(), true)
  assert.equal(await highlighted(), true)

  const topbar = await rect('.topbar')
  const sidebar = await rect('.sidebar')
  const main = await rect('.app-main')
  assert.ok(topbar.height > 40, `topbar height: ${topbar.height}`)
  assert.ok(sidebar.width > 100, `sidebar width: ${sidebar.width}`)
  assert.ok(main.width > 300, `main width: ${main.width}`)

  for (const selector of ['.topbar', '.sidebar', '.app-main']) {
    const style = await outline(selector)
    assert.notEqual(style.style, 'none', `${selector} has no visible outline`)
    assert.ok(Number.parseFloat(style.width) >= 2, `${selector} outline width`)
  }

  const metricText = await page.getByTestId('layout-mode').innerText()
  assert.match(metricText, /宽屏/)
  assert.equal(await page.getByTestId('demo-section-2.19').locator('img').count(), 0)

  // Toggle highlight off/on; real layout keeps the same dimensions.
  await page.getByTestId('layout-highlight-toggle').uncheck()
  assert.equal(await highlighted(), false)
  assert.equal((await outline('.topbar')).style, 'none')
  const topbarAfterOff = await rect('.topbar')
  assert.ok(Math.abs(topbarAfterOff.width - topbar.width) < 1, 'highlight toggle changed layout width')
  await page.getByTestId('layout-highlight-toggle').check()
  assert.equal(await highlighted(), true)

  // Real navigation still works; cleanup removes the highlight class on unmount.
  await page.getByRole('link', { name: '批量地址录入' }).click()
  await page.waitForURL('**/import')
  assert.equal(await highlighted(), false)
  await page.getByRole('heading', { name: '批量录入收件地址' }).waitFor()

  // Responsive: mode is based on the real viewport; shell/sidebar/main remain real DOM.
  for (const width of [1366, 980, 760, 390]) {
    await page.setViewportSize({ width, height: 900 })
    await open('/web-basics#chapter-2.19')
    await page.getByTestId('demo-section-2.19').waitFor()
    assert.equal(await highlighted(), true)
    assert.equal(await page.getByTestId('chapter-2.19').locator('.wb-status').innerText(), '已实现')
    const mode = await page.getByTestId('layout-mode').innerText()
    if (width <= 640) assert.match(mode, /窄屏/)
    else if (width <= 980) assert.match(mode, /中窄屏/)
    else assert.match(mode, /宽屏/)
    const sidebarNow = await rect('.sidebar')
    const topbarNow = await rect('.topbar')
    const mainNow = await rect('.app-main')
    assert.ok(sidebarNow.width > 0 && topbarNow.height > 0 && mainNow.width > 0, `regions ${width}`)
    await noOverflow(`layout ${width}`)
  }

  assert.deepEqual(consoleErrors, [], `console errors: ${consoleErrors.join(' | ')}`)
  console.log('MS06_8_BROWSER_OK')
} finally {
  await browser.close()
}
