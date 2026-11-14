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

async function setRange(testId, value) {
  await page.getByTestId(testId).evaluate((element, nextValue) => {
    const input = element
    input.value = String(nextValue)
    input.dispatchEvent(new Event('input', { bubbles: true }))
    input.dispatchEvent(new Event('change', { bubbles: true }))
  }, value)
  await page.waitForTimeout(80)
}

async function gapBetween(firstTestId, secondTestId) {
  return page.evaluate(([firstId, secondId]) => {
    const first = document.querySelector(`[data-testid="${firstId}"]`)
    const second = document.querySelector(`[data-testid="${secondId}"]`)
    if (!first || !second) return Number.NaN
    return second.getBoundingClientRect().top - first.getBoundingClientRect().bottom
  }, [firstTestId, secondTestId])
}

async function noOverflow(label) {
  await page.waitForTimeout(80)
  const dimensions = await page.evaluate(() => ({
    scrollWidth: document.documentElement.scrollWidth,
    innerWidth: window.innerWidth
  }))
  assert.ok(dimensions.scrollWidth <= dimensions.innerWidth, `${label}: ${JSON.stringify(dimensions)}`)
}

async function sectionVisible(chapter) {
  await page.getByTestId(`demo-section-${chapter}`).scrollIntoViewIfNeeded()
  await page.waitForTimeout(80)
  return page.getByTestId(`demo-section-${chapter}`).count()
}

try {
  await open('/web-basics')
  await page.getByTestId('web-basics-page').waitFor()

  // 19 chapters, all complete, in course order.
  assert.equal(await page.locator('.wb-chapter').count(), 19)
  const navNumbers = await page.locator('.wb-practice-nav__number').allInnerTexts()
  assert.deepEqual(navNumbers, [
    '2.1', '2.2', '2.3', '2.4', '2.5', '2.6', '2.7', '2.8', '2.9', '2.10',
    '2.11', '2.12', '2.13', '2.14', '2.15', '2.16', '2.17', '2.18', '2.19'
  ])
  for (let chapter = 1; chapter <= 19; chapter += 1) {
    const number = `2.${chapter}`
    assert.equal(
      await page.getByTestId(`chapter-${number}`).locator('.wb-status').innerText(),
      '已实现',
      `${number} status`
    )
    assert.equal(await page.getByTestId(`practice-link-${number}`).count(), 1, `${number} nav link`)
  }
  assert.equal(await page.getByTestId('display-comparison').count(), 1)
  assert.equal(await page.getByTestId('positioning-comparison').count(), 1)
  assert.equal(await page.getByTestId('stacking-supplement').count(), 1)

  // All in-page anchors resolve and preserve active state.
  for (let chapter = 1; chapter <= 17; chapter += 1) {
    const number = `2.${chapter}`
    await page.getByTestId(`practice-link-${number}`).click()
    await page.waitForURL(`**/web-basics#chapter-${number}`)
    assert.equal(await page.getByTestId(`demo-section-${number}`).count(), 1, `${number} demo`)
    assert.equal(
      await page.locator('.wb-practice-nav__item.is-active').innerText().then((text) => text.includes(number)),
      true,
      `${number} active`
    )
  }

  // MS-06-R1: 2.2 real margin collapse and flex contrast.
  await page.getByTestId('practice-link-2.2').click()
  await page.waitForURL('**/web-basics#chapter-2.2')
  await setRange('margin-bottom', 24)
  await setRange('margin-top', 16)
  const normalGap = await gapBetween('margin-normal-first', 'margin-normal-second')
  assert.ok(Math.abs(normalGap - 24) < 1, `2.2 normal flow margin collapse: ${normalGap}`)
  const contrastGap = await gapBetween('margin-contrast-first', 'margin-contrast-second')
  assert.ok(Math.abs(contrastGap - 40) < 1, `2.2 flex contrast margins: ${contrastGap}`)
  await setRange('margin-bottom', 16)
  await setRange('margin-top', 32)
  const normalGap2 = await gapBetween('margin-normal-first', 'margin-normal-second')
  assert.ok(Math.abs(normalGap2 - 32) < 1, `2.2 second margin collapse: ${normalGap2}`)
  await page.getByTestId('block-reset').click()

  // 2.18 is a real route with separate plan and implemented modes.
  await page.getByTestId('practice-link-2.18').click()
  await page.waitForURL('**/web-basics/wireframe')
  await page.getByTestId('wireframe-plan-board').waitFor()
  await page.getByTestId('wireframe-mode-implemented').click()
  await page.waitForURL('**/web-basics/wireframe?mode=implemented')
  await page.locator('.import-page').waitFor()
  assert.equal(await page.getByTestId('wireframe-preview-import').count(), 1)
  await page.getByTestId('preview-page-addresses').click()
  await page.locator('.address-page').waitFor()
  assert.equal(await page.getByTestId('wireframe-preview-addresses').count(), 1)

  // 2.19 highlight works on the real AppLayout and cleans up on navigation.
  await open('/web-basics#chapter-2.19')
  await page.getByTestId('demo-section-2.19').waitFor()
  assert.equal(await page.getByTestId('chapter-2.19').locator('.wb-status').innerText(), '已实现')
  assert.equal(await page.locator('.app-shell').evaluate((el) => el.classList.contains('wb-layout-highlight')), true)
  assert.equal(await page.getByTestId('layout-highlight-toggle').isChecked(), true)
  await page.getByTestId('layout-highlight-toggle').uncheck()
  assert.equal(await page.locator('.app-shell').evaluate((el) => el.classList.contains('wb-layout-highlight')), false)
  await page.getByTestId('layout-highlight-toggle').check()

  // Fixed probe is scoped to 2.13; not visible in other chapters.
  await page.getByTestId('practice-link-2.11').click()
  await page.waitForTimeout(150)
  assert.equal(await page.getByTestId('fixed-probe').count(), 0)
  await page.getByTestId('practice-link-2.13').click()
  await page.getByTestId('fixed-probe').waitFor({ state: 'visible', timeout: 5000 })
  assert.equal(await page.getByTestId('fixed-probe').count(), 1)
  await page.getByTestId('practice-link-2.19').click()
  await page.waitForTimeout(300)
  assert.equal(await page.getByTestId('fixed-probe').count(), 0)

  // Servlet demo is now a real route; unknown subpaths still use the app 404.
  await open('/servlet-demo/not-a-real-route')
  await page.getByRole('heading', { name: '页面不存在' }).waitFor()
  assert.ok(await page.locator('a[href="/servlet-demo"]').count() >= 1)

  // Responsive: every in-page chapter and the independent 2.18 route.
  for (const width of [1366, 980, 760, 390]) {
    await page.setViewportSize({ width, height: 900 })
    await open('/web-basics')
    for (let chapter = 1; chapter <= 17; chapter += 1) {
      const number = `2.${chapter}`
      assert.equal(await sectionVisible(number), 1, `${number} ${width}`)
      await noOverflow(`chapter ${number} ${width}`)
    }
    assert.equal(await sectionVisible('2.19'), 1)
    await noOverflow(`chapter 2.19 ${width}`)
    await open('/web-basics/wireframe?mode=plan')
    await page.getByTestId('wireframe-plan-board').waitFor()
    await noOverflow(`2.18 plan ${width}`)
    await open('/web-basics/wireframe?mode=implemented&page=import')
    await page.locator('.import-page').waitFor()
    await noOverflow(`2.18 implemented ${width}`)
  }

  assert.deepEqual(consoleErrors, [], `console errors: ${consoleErrors.join(' | ')}`)
  console.log('MS06_9_BROWSER_OK')
} finally {
  await browser.close()
}
