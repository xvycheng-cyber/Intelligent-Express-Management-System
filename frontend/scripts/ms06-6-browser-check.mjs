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

async function rect(testId, index = 0) {
  return page.getByTestId(testId).nth(index).evaluate((element) => {
    const box = element.getBoundingClientRect()
    return { width: box.width, height: box.height, top: box.top, left: box.left, right: box.right, bottom: box.bottom }
  })
}

async function computed(testId, property, index = 0) {
  return page.getByTestId(testId).nth(index).evaluate(
    (element, prop) => getComputedStyle(element).getPropertyValue(prop),
    property
  )
}

async function noOverflow(label) {
  await page.waitForTimeout(80)
  const dimensions = await page.evaluate(() => ({
    scrollWidth: document.documentElement.scrollWidth,
    innerWidth: window.innerWidth
  }))
  assert.ok(dimensions.scrollWidth <= dimensions.innerWidth, `${label}: ${JSON.stringify(dimensions)}`)
}

async function topmostAtOverlap() {
  return page.evaluate(() => {
    const a = document.querySelector('[data-testid="stack-a"]')?.getBoundingClientRect()
    const b = document.querySelector('[data-testid="stack-b"]')?.getBoundingClientRect()
    if (!a || !b) return null
    const left = Math.max(a.left, b.left)
    const right = Math.min(a.right, b.right)
    const top = Math.max(a.top, b.top)
    const bottom = Math.min(a.bottom, b.bottom)
    const element = document.elementFromPoint((left + right) / 2, (top + bottom) / 2)
    return element?.closest('[data-testid]')?.getAttribute('data-testid') ?? null
  })
}

try {
  await open('/web-basics')
  await page.getByTestId('web-basics-page').waitFor()

  assert.equal(await page.locator('.wb-chapter').count(), 19)
  for (const chapter of ['2.16', '2.17']) {
    assert.equal(await page.getByTestId(`chapter-${chapter}`).locator('.wb-status').innerText(), '已实现')
    assert.equal(await page.getByTestId(`demo-section-${chapter}`).count(), 1)
    assert.equal(await page.getByTestId(`practice-link-${chapter}`).count(), 1)
  }
  assert.equal(await page.getByTestId('chapter-2.18').locator('.wb-status').innerText(), '已实现')
  assert.equal(await page.getByTestId('stacking-supplement').count(), 1)

  // 2.16: mode changes and four offset directions.
  assert.equal(await computed('offset-item', 'position'), 'relative')
  const relativeItem = await rect('offset-item')
  await page.getByTestId('offset-mode').selectOption('static')
  assert.equal(await computed('offset-item', 'position'), 'static')
  const staticItem = await rect('offset-item')
  assert.ok(staticItem.top < relativeItem.top, '2.16 static should not have relative top offset')
  await page.getByTestId('offset-mode').selectOption('relative')
  await setRange('offset-value', 0)
  const offsetBase = await rect('offset-item')
  for (const [axis, check] of [
    ['top', (r) => r.top > offsetBase.top + 20],
    ['right', (r) => r.left < offsetBase.left - 20],
    ['bottom', (r) => r.top < offsetBase.top - 20],
    ['left', (r) => r.left > offsetBase.left + 20]
  ]) {
    await page.getByTestId('offset-axis').selectOption(axis)
    await setRange('offset-value', 40)
    const after = await rect('offset-item')
    assert.ok(check(after), `2.16 ${axis} offset did not move as expected: ${JSON.stringify(after)}`)
    assert.match(await page.getByTestId('demo-section-2.16').locator('.wb-code code').innerText(), new RegExp(`${axis}: 40px`))
  }
  await page.getByTestId('offset-mode').selectOption('absolute')
  assert.equal(await computed('offset-item', 'position'), 'absolute')
  await page.getByTestId('offset-reset').click()
  assert.equal(await computed('offset-item', 'position'), 'relative')

  // 2.17: conflicting offsets, auto/fixed size and margin auto.
  await page.getByTestId('conflict-scenario').selectOption('absolute-auto')
  await setRange('conflict-offset', 24)
  const stage = await rect('conflict-stage')
  const auto = await rect('conflict-item')
  assert.ok(auto.width > stage.width - 80, `2.17 auto stretch too small: ${auto.width}`)
  assert.match(await page.getByTestId('demo-section-2.17').locator('.wb-code code').innerText(), /width: auto/)
  await page.getByTestId('conflict-scenario').selectOption('absolute-fixed')
  const fixed = await rect('conflict-item')
  assert.ok(Math.abs(fixed.width - 220) < 2, `2.17 fixed width: ${fixed.width}`)
  assert.ok(fixed.left >= stage.left + 10, '2.17 fixed left position missing')
  assert.match(await page.getByTestId('demo-section-2.17').locator('.wb-code code').innerText(), /left 生效/)
  await page.getByTestId('conflict-scenario').selectOption('absolute-vertical')
  const vertical = await rect('conflict-item')
  assert.ok(vertical.height > 100, `2.17 vertical stretch: ${vertical.height}`)
  await page.getByTestId('conflict-scenario').selectOption('relative-both')
  assert.equal(await computed('conflict-item', 'position'), 'relative')
  await page.getByTestId('conflict-scenario').selectOption('absolute-margin-auto')
  const centered = await rect('conflict-item')
  const centerDelta = Math.abs((centered.left + centered.right) / 2 - (stage.left + stage.right) / 2)
  assert.ok(centerDelta < 4, `2.17 margin auto not centered: ${centerDelta}`)
  await page.getByTestId('conflict-reset').click()

  // 2.17 supplemental stacking contexts.
  await page.getByTestId('stacking-supplement').scrollIntoViewIfNeeded()
  await page.waitForTimeout(150)
  await page.getByTestId('stack-context-toggle').check()
  const bTop = await topmostAtOverlap()
  assert.equal(bTop, 'stack-b', '2.17 parent stacking context should keep B above A')
  await page.getByTestId('stack-context-toggle').uncheck()
  const aTop = await topmostAtOverlap()
  assert.equal(aTop, 'stack-a', '2.17 without parent context A z-index 999 should win')
  await page.getByTestId('stack-context-toggle').check()
  assert.match(
    await page.getByTestId('stacking-supplement').locator('.wb-code code').innerText(),
    /z-index: 999/
  )
  await page.getByTestId('stack-reset').click()

  // Hash navigation and refresh.
  await page.getByTestId('practice-link-2.17').click()
  await page.waitForURL('**/web-basics#chapter-2.17')
  assert.equal(
    await page.locator('.wb-practice-nav__item.is-active').innerText().then((text) => text.includes('2.17')),
    true
  )
  await page.reload({ waitUntil: 'networkidle' })
  assert.equal(
    await page.locator('.wb-practice-nav__item.is-active').innerText().then((text) => text.includes('2.17')),
    true
  )

  // Responsive checks.
  for (const width of [1366, 980, 760, 390]) {
    await page.setViewportSize({ width, height: 900 })
    await open('/web-basics')
    for (const chapter of ['2.16', '2.17']) {
      assert.equal(await page.getByTestId(`demo-section-${chapter}`).count(), 1)
      await noOverflow(`/web-basics ${chapter} ${width}`)
    }
    assert.equal(await page.getByTestId('stacking-supplement').count(), 1)
    await noOverflow(`/web-basics stacking ${width}`)
  }

  assert.deepEqual(consoleErrors, [], `console errors: ${consoleErrors.join(' | ')}`)
  console.log('MS06_6_BROWSER_OK')
} finally {
  await browser.close()
}
