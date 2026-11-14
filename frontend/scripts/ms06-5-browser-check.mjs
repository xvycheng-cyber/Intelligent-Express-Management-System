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

try {
  await open('/web-basics')
  await page.getByTestId('web-basics-page').waitFor()

  assert.equal(await page.locator('.wb-chapter').count(), 19)
  for (const chapter of ['2.11', '2.12', '2.13', '2.14', '2.15']) {
    assert.equal(await page.getByTestId(`chapter-${chapter}`).locator('.wb-status').innerText(), '已实现')
    assert.equal(await page.getByTestId(`demo-section-${chapter}`).count(), 1)
    assert.equal(await page.getByTestId(`practice-link-${chapter}`).count(), 1)
  }
  assert.equal(await page.getByTestId('chapter-2.18').locator('.wb-status').innerText(), '已实现')

  // 2.11 absolute out-of-flow and following element shift.
  assert.equal(await computed('absolute-item', 'position'), 'absolute')
  assert.equal(await page.getByTestId('absolute-ghost').count(), 1)
  const absoluteAfter = await rect('absolute-after')
  await page.getByTestId('absolute-mode').selectOption('static')
  assert.equal(await computed('absolute-item', 'position'), 'static')
  assert.equal(await page.getByTestId('absolute-ghost').count(), 0)
  const staticAfter = await rect('absolute-after')
  assert.ok(staticAfter.top > absoluteAfter.top + 20, 'absolute did not remove original flow space')
  await page.getByTestId('absolute-mode').selectOption('absolute')
  await page.getByTestId('absolute-axis').selectOption('right')
  await setRange('absolute-offset', 60)
  assert.match(await page.getByTestId('demo-section-2.11').locator('.wb-code code').innerText(), /right: 60px/)
  await page.getByTestId('absolute-reset').click()
  assert.equal(await computed('absolute-item', 'position'), 'absolute')

  // 2.12 containing block changes with parent position.
  assert.equal(await computed('context-parent', 'position'), 'static')
  const staticChild = await rect('context-child')
  await page.getByTestId('context-parent-position').selectOption('relative')
  assert.equal(await computed('context-parent', 'position'), 'relative')
  const relativeChild = await rect('context-child')
  assert.ok(
    Math.abs(staticChild.left - relativeChild.left) > 10 || Math.abs(staticChild.top - relativeChild.top) > 10,
    '2.12 child did not change containing block'
  )
  assert.match(await page.getByTestId('demo-section-2.12').locator('.wb-code code').innerText(), /position: relative/)
  await page.getByTestId('context-reset').click()
  assert.equal(await computed('context-parent', 'position'), 'static')

  // 2.13 fixed stays in viewport while page scrolls; absolute scrolls in frame.
  await page.locator('[id="chapter-2.13"]').scrollIntoViewIfNeeded()
  await page.waitForTimeout(250)
  assert.equal(await page.getByTestId('fixed-probe').count(), 1)
  assert.equal(await computed('fixed-probe', 'position'), 'fixed')
  const fixedBefore = await rect('fixed-probe')
  const absoluteBefore = await rect('fixed-absolute')
  await page.evaluate(() => window.scrollBy(0, 80))
  await page.waitForTimeout(120)
  const fixedAfter = await rect('fixed-probe')
  assert.ok(Math.abs(fixedAfter.top - fixedBefore.top) < 1, '2.13 fixed moved with page scroll')
  assert.ok(Math.abs(fixedAfter.left - fixedBefore.left) < 1, '2.13 fixed moved horizontally')
  const scrollFrame = page.getByTestId('fixed-scroll-frame')
  const frameScrollBefore = await scrollFrame.evaluate((element) => element.scrollTop)
  await scrollFrame.evaluate((element) => { element.scrollTop = 100 })
  await page.waitForTimeout(80)
  const frameScrollAfter = await scrollFrame.evaluate((element) => element.scrollTop)
  assert.ok(frameScrollAfter > frameScrollBefore, '2.13 scroll container did not scroll')
  const absoluteAfterScroll = await rect('fixed-absolute')
  assert.ok(Math.abs(absoluteAfterScroll.top - absoluteBefore.top) > 10, '2.13 absolute comparison did not move')
  assert.match(await page.getByTestId('demo-section-2.13').locator('.wb-code code').innerText(), /position: fixed/)
  await page.getByTestId('fixed-show').uncheck()
  assert.equal(await page.getByTestId('fixed-probe').count(), 0)
  await page.getByTestId('fixed-reset').click()
  assert.equal(await page.getByTestId('fixed-probe').count(), 1)
  await page.evaluate(() => window.scrollTo(0, 0))

  // 2.14 sticky threshold and reset.
  assert.equal(await computed('sticky-item', 'position'), 'sticky')
  const stickyFrameEl = page.getByTestId('sticky-frame')
  const stickyFrame = await rect('sticky-frame')
  const stickyInitial = await rect('sticky-item')
  assert.ok(stickyInitial.top > stickyFrame.top, '2.14 sticky initial position')
  await stickyFrameEl.evaluate((element) => { element.scrollTop = 180 })
  await page.waitForTimeout(100)
  const stickyFrameAfter = await rect('sticky-frame')
  const stickyAfterScroll = await rect('sticky-item')
  assert.ok(Math.abs(stickyAfterScroll.top - (stickyFrameAfter.top + 24)) < 2, `2.14 sticky top: ${stickyAfterScroll.top}`)
  await setRange('sticky-threshold', 50)
  await stickyFrameEl.evaluate((element) => { element.scrollTop = 180 })
  await page.waitForTimeout(100)
  const stickyFrameAfterThreshold = await rect('sticky-frame')
  const stickyAfterThreshold = await rect('sticky-item')
  assert.ok(Math.abs(stickyAfterThreshold.top - (stickyFrameAfterThreshold.top + 50)) < 2, `2.14 threshold: ${stickyAfterThreshold.top}`)
  assert.match(await page.getByTestId('demo-section-2.14').locator('.wb-code code').innerText(), /top: 50px/)
  await page.getByTestId('sticky-reset').click()
  assert.equal(await page.getByTestId('sticky-item').count(), 1)

  // 2.15 sticky constraint rectangle: parent bottom pushes sticky away.
  assert.equal(await computed('constraint-sticky', 'position'), 'sticky')
  const constraintFrame = await rect('constraint-frame')
  const constraintFrameEl = page.getByTestId('constraint-frame')
  const parentInitial = await rect('constraint-parent')
  const stickyConstraintInitial = await rect('constraint-sticky')
  await constraintFrameEl.evaluate((element) => { element.scrollTop = 220 })
  await page.waitForTimeout(120)
  const parentAfter = await rect('constraint-parent')
  const stickyConstraintAfter = await rect('constraint-sticky')
  assert.ok(parentAfter.top < parentInitial.top - 50, '2.15 parent did not scroll')
  assert.ok(stickyConstraintAfter.top > constraintFrame.top, '2.15 sticky disappeared above frame')
  assert.ok(stickyConstraintAfter.bottom <= parentAfter.bottom + 2, '2.15 sticky escaped parent boundary')
  await setRange('constraint-parent-height', 180)
  await constraintFrameEl.evaluate((element) => { element.scrollTop = 0 })
  await page.waitForTimeout(80)
  await constraintFrameEl.evaluate((element) => { element.scrollTop = 220 })
  await page.waitForTimeout(120)
  const shortParent = await rect('constraint-parent')
  const shortSticky = await rect('constraint-sticky')
  assert.ok(shortSticky.bottom <= shortParent.bottom + 2, '2.15 short parent boundary failed')
  assert.match(await page.getByTestId('demo-section-2.15').locator('.wb-code code').innerText(), /height: 180px/)
  await page.getByTestId('constraint-reset').click()

  // Comparison table and hash navigation.
  const comparison = page.getByTestId('positioning-comparison')
  assert.equal(await comparison.count(), 1)
  for (const word of ['static', 'relative', 'absolute', 'fixed', 'sticky']) {
    assert.match(await comparison.innerText(), new RegExp(word))
  }
  await page.getByTestId('practice-link-2.13').click()
  await page.waitForURL('**/web-basics#chapter-2.13')
  assert.equal(
    await page.locator('.wb-practice-nav__item.is-active').innerText().then((text) => text.includes('2.13')),
    true
  )
  await page.reload({ waitUntil: 'networkidle' })
  assert.equal(
    await page.locator('.wb-practice-nav__item.is-active').innerText().then((text) => text.includes('2.13')),
    true
  )

  // Responsive checks.
  for (const width of [1366, 980, 760, 390]) {
    await page.setViewportSize({ width, height: 900 })
    await open('/web-basics')
    for (const chapter of ['2.11', '2.12', '2.13', '2.14', '2.15']) {
      assert.equal(await page.getByTestId(`demo-section-${chapter}`).count(), 1)
      await noOverflow(`/web-basics ${chapter} ${width}`)
    }
    await noOverflow(`/web-basics positioning ${width}`)
  }

  assert.deepEqual(consoleErrors, [], `console errors: ${consoleErrors.join(' | ')}`)
  console.log('MS06_5_BROWSER_OK')
} finally {
  await browser.close()
}
