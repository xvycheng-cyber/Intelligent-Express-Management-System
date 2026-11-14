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
  await page.waitForTimeout(100)
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
  for (const chapter of ['2.6', '2.7', '2.8', '2.9', '2.10']) {
    assert.equal(await page.getByTestId(`chapter-${chapter}`).locator('.wb-status').innerText(), '已实现')
    assert.equal(await page.getByTestId(`demo-section-${chapter}`).count(), 1)
    assert.equal(await page.getByTestId(`practice-link-${chapter}`).count(), 1)
  }
  assert.equal(await page.getByTestId('chapter-2.18').locator('.wb-status').innerText(), '已实现')
  assert.equal(await page.getByTestId('chapter-2.18').locator('.wb-status').innerText(), '已实现')

  // 2.6 box model layers and calculated/actual sizes.
  assert.equal(await computed('box-target', 'box-sizing'), 'content-box')
  const boxInitial = await rect('box-target')
  assert.ok(Math.abs(boxInitial.width - 288) < 2, `2.6 initial border box: ${boxInitial.width}`)
  assert.equal(await page.getByTestId('box-actual').innerText(), '288 × 168')
  const borderLayer = await computed('box-target', 'border-top-width')
  assert.equal(borderLayer, '6px')
  await setRange('box-padding', 30)
  await setRange('box-border', 12)
  const boxGrown = await rect('box-target')
  assert.ok(boxGrown.width > boxInitial.width + 30, '2.6 padding/border did not grow border box')
  const boxBeforeMargin = await rect('box-target')
  await setRange('box-margin', 30)
  const boxAfterMargin = await rect('box-target')
  assert.ok(Math.abs(boxAfterMargin.width - boxBeforeMargin.width) < 1, '2.6 margin must not change border box')
  assert.match(await page.getByTestId('demo-section-2.6').locator('.wb-code code').innerText(), /padding: 30px/)
  await page.getByTestId('box-reset').click()
  const boxReset = await rect('box-target')
  assert.ok(Math.abs(boxReset.width - 288) < 2, '2.6 reset failed')

  // 2.7 content-box: width is content, border box grows by padding/border.
  assert.equal(await computed('box-sizing-content-box-target', 'box-sizing'), 'content-box')
  const contentBoxInitial = await rect('box-sizing-content-box-target')
  assert.ok(Math.abs(contentBoxInitial.width - 292) < 2, `2.7 border box: ${contentBoxInitial.width}`)
  const contentBoxContent = await rect('box-sizing-content-box-content')
  assert.ok(Math.abs(contentBoxContent.width - 260) < 2, `2.7 content width: ${contentBoxContent.width}`)
  await setRange('box-sizing-content-box-padding', 20)
  await setRange('box-sizing-content-box-border', 10)
  const contentBoxGrown = await rect('box-sizing-content-box-target')
  assert.ok(Math.abs(contentBoxGrown.width - (260 + 40 + 20)) < 2, `2.7 grown width: ${contentBoxGrown.width}`)
  assert.match(await page.getByTestId('demo-section-2.7').locator('.wb-code code').innerText(), /box-sizing: content-box/)

  // 2.8 border-box: same CSS width keeps the border box fixed; content shrinks.
  assert.equal(await computed('box-sizing-border-box-target', 'box-sizing'), 'border-box')
  const borderBoxInitial = await rect('box-sizing-border-box-target')
  assert.ok(Math.abs(borderBoxInitial.width - 260) < 2, `2.8 border box: ${borderBoxInitial.width}`)
  const borderBoxContent = await rect('box-sizing-border-box-content')
  assert.ok(Math.abs(borderBoxContent.width - 228) < 2, `2.8 content width: ${borderBoxContent.width}`)
  await setRange('box-sizing-border-box-padding', 20)
  await setRange('box-sizing-border-box-border', 10)
  const borderBoxGrown = await rect('box-sizing-border-box-target')
  assert.ok(Math.abs(borderBoxGrown.width - 260) < 2, `2.8 border box changed: ${borderBoxGrown.width}`)
  const borderBoxContentGrown = await rect('box-sizing-border-box-content')
  assert.ok(Math.abs(borderBoxContentGrown.width - 200) < 2, `2.8 content did not shrink: ${borderBoxContentGrown.width}`)
  assert.match(await page.getByTestId('demo-section-2.8').locator('.wb-code code').innerText(), /box-sizing: border-box/)
  await page.getByTestId('box-sizing-content-box-reset').click()
  await page.getByTestId('box-sizing-border-box-reset').click()

  // 2.9 normal flow: block/inline display, static position, subsequent content affected.
  assert.equal(await computed('normal-block', 'display'), 'block')
  assert.equal(await computed('normal-block', 'position'), 'static')
  assert.equal(await computed('normal-inline', 'display'), 'inline')
  const afterInitial = await rect('normal-after')
  await setRange('normal-height', 90)
  await setRange('normal-gap', 20)
  const afterGrown = await rect('normal-after')
  assert.ok(afterGrown.top > afterInitial.top, '2.9 subsequent content did not move')
  const blockBeforeOffset = await rect('normal-block')
  await setRange('normal-offset', 40)
  const blockAfterOffset = await rect('normal-block')
  assert.ok(Math.abs(blockAfterOffset.top - blockBeforeOffset.top) < 1, '2.9 static top offset moved block')
  assert.match(await page.getByTestId('demo-section-2.9').locator('.wb-code code').innerText(), /position: static/)
  await page.getByTestId('normal-reset').click()

  // 2.10 relative: visual offset, ghost original space, subsequent layout unchanged.
  assert.equal(await computed('relative-static', 'position'), 'static')
  assert.equal(await computed('relative-box', 'position'), 'relative')
  const ghostInitial = await rect('relative-ghost')
  const relativeInitial = await rect('relative-box')
  assert.ok(Math.abs(relativeInitial.top - ghostInitial.top - 28) < 2, '2.10 default top offset missing')
  const staticAfterInitial = await rect('relative-static-after')
  const relativeAfterInitial = await rect('relative-after')
  assert.ok(Math.abs(staticAfterInitial.top - relativeAfterInitial.top) < 2, '2.10 relative moved subsequent element')
  await page.getByTestId('relative-axis').selectOption('right')
  await setRange('relative-offset', 40)
  const ghostRight = await rect('relative-ghost')
  const relativeRight = await rect('relative-box')
  assert.ok(Math.abs(ghostRight.right - relativeRight.right - 40) < 2, '2.10 right offset missing')
  const relativeAfterRight = await rect('relative-after')
  assert.ok(Math.abs(relativeAfterRight.top - staticAfterInitial.top) < 2, '2.10 right offset moved subsequent element')
  assert.match(await page.getByTestId('demo-section-2.10').locator('.wb-code code').innerText(), /right: 40px/)
  await page.getByTestId('relative-reset').click()

  // Hash navigation for the new sections.
  await page.getByTestId('practice-link-2.7').click()
  await page.waitForURL('**/web-basics#chapter-2.7')
  assert.equal(
    await page.locator('.wb-practice-nav__item.is-active').innerText().then((text) => text.includes('2.7')),
    true
  )
  await page.reload({ waitUntil: 'networkidle' })
  assert.equal(
    await page.locator('.wb-practice-nav__item.is-active').innerText().then((text) => text.includes('2.7')),
    true
  )

  // Responsive checks; include controls at 390.
  for (const width of [1366, 980, 760, 390]) {
    await page.setViewportSize({ width, height: 900 })
    await open('/web-basics')
    for (const chapter of ['2.6', '2.7', '2.8', '2.9', '2.10']) {
      assert.equal(await page.getByTestId(`demo-section-${chapter}`).count(), 1)
      await noOverflow(`/web-basics ${chapter} ${width}`)
    }
    await setRange('relative-offset', 60)
    assert.equal(await computed('relative-box', 'position'), 'relative')
    await noOverflow(`/web-basics 2.10 controls ${width}`)
  }

  assert.deepEqual(consoleErrors, [], `console errors: ${consoleErrors.join(' | ')}`)
  console.log('MS06_4_BROWSER_OK')
} finally {
  await browser.close()
}
