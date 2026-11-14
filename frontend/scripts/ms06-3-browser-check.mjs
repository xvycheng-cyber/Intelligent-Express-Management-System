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
  await page.waitForTimeout(60)
}

async function rect(testId, index = 0) {
  return page.getByTestId(testId).nth(index).evaluate((element) => {
    const box = element.getBoundingClientRect()
    return { width: box.width, height: box.height, top: box.top, left: box.left, right: box.right }
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

  // Page navigation and truthful statuses.
  assert.equal(await page.locator('.wb-chapter').count(), 19)
  for (const chapter of ['2.1', '2.2', '2.3', '2.4', '2.5']) {
    assert.equal(
      await page.getByTestId(`chapter-${chapter}`).locator('.wb-status').innerText(),
      '已实现'
    )
    assert.equal(await page.getByTestId(`demo-section-${chapter}`).count(), 1)
  }
  assert.equal(await page.getByTestId('chapter-2.6').locator('.wb-status').innerText(), '已实现')
  assert.equal(await page.getByTestId('chapter-2.18').locator('.wb-status').innerText(), '已实现')
  assert.equal(await page.getByTestId('chapter-2.18').locator('.wb-status').innerText(), '已实现')
  assert.equal(await page.locator('.wb-practice-nav__foot .wb-status').innerText(), '待实现')

  for (const chapter of ['2.1', '2.2', '2.3', '2.4', '2.5']) {
    assert.equal(await page.getByTestId(`practice-link-${chapter}`).count(), 1)
  }

  // 2.1 inline: computed display, ignored size, wrapping and reset.
  assert.equal(await computed('inline-item', 'display'), 'inline')
  assert.equal(await page.getByTestId('inline-item').count(), 3)
  await setRange('inline-width', 280)
  await setRange('inline-height', 100)
  const inlineBox = await rect('inline-item')
  assert.ok(inlineBox.width < 220, `inline width unexpectedly applied: ${inlineBox.width}`)
  assert.ok(inlineBox.height < 90, `inline height unexpectedly applied: ${inlineBox.height}`)
  assert.match(await page.getByTestId('demo-section-2.1').locator('.wb-code code').innerText(), /width: 280px/)
  await setRange('inline-count', 5)
  assert.equal(await page.getByTestId('inline-item').count(), 5)
  await page.getByTestId('inline-long-text').click()
  const inlineRects = await page.getByTestId('inline-item').first().evaluate((element) => element.getClientRects().length)
  assert.ok(inlineRects > 1, `long inline text did not wrap: ${inlineRects}`)
  await page.getByTestId('inline-reset').click()
  assert.equal(await page.getByTestId('inline-item').count(), 3)
  assert.equal(await page.getByTestId('inline-text').inputValue(), '行内元素')

  // 2.2 block: real block flow, auto/fixed widths, centering and reset.
  assert.equal(await computed('block-item', 'display'), 'block')
  const autoBlock = await rect('block-item')
  assert.ok(autoBlock.width > 300, `block auto width too small: ${autoBlock.width}`)
  await page.getByTestId('block-width-mode').selectOption('fixed')
  await setRange('block-width', 220)
  const fixedBlock = await rect('block-item')
  assert.ok(Math.abs(fixedBlock.width - 220) < 2, `fixed block width: ${fixedBlock.width}`)
  await page.getByTestId('block-centered').check()
  const centeredBlock = await rect('block-item')
  assert.ok(centeredBlock.left > autoBlock.left + 40, 'block did not center with auto margins')
  assert.match(await page.getByTestId('demo-section-2.2').locator('.wb-code code').innerText(), /width: 220px/)
  await page.getByTestId('block-reset').click()
  assert.equal(await computed('block-item', 'display'), 'block')
  const resetBlock = await rect('block-item')
  assert.ok(resetBlock.width > 300, 'block reset did not restore auto width')

  // 2.3 inline-block: width/height, whitespace gap, native wrap and inline compare.
  assert.equal(await computed('inline-block-item', 'display'), 'inline-block')
  const ibFirst = await rect('inline-block-item', 0)
  const ibSecond = await rect('inline-block-item', 1)
  assert.ok(Math.abs(ibFirst.width - 120) < 2, `inline-block width: ${ibFirst.width}`)
  assert.ok(Math.abs(ibFirst.height - 60) < 2, `inline-block height: ${ibFirst.height}`)
  assert.ok(ibSecond.left - ibFirst.right > 0, 'default whitespace gap missing')
  await page.getByTestId('inline-block-whitespace').uncheck()
  const ibNoSpace = [await rect('inline-block-item', 0), await rect('inline-block-item', 1)]
  assert.ok(ibNoSpace[1].left - ibNoSpace[0].right < 1, 'whitespace removal did not collapse gap')
  await page.getByTestId('inline-block-whitespace').check()
  await setRange('inline-block-container', 260)
  const tops = []
  for (let index = 0; index < 4; index += 1) tops.push((await rect('inline-block-item', index)).top)
  assert.ok(new Set(tops.map((top) => Math.round(top))).size > 1, 'inline-block did not wrap natively')
  await page.getByTestId('inline-block-mode').selectOption('inline')
  assert.equal(await computed('inline-block-item', 'display'), 'inline')
  const inlineCompare = await rect('inline-block-item', 0)
  assert.ok(inlineCompare.width < 110, `inline compare width: ${inlineCompare.width}`)
  await page.getByTestId('inline-block-reset').click()
  assert.equal(await computed('inline-block-item', 'display'), 'inline-block')

  // 2.4 flex: direction changes axes, alignment, wrap and reset.
  assert.equal(await computed('flex-stage', 'display'), 'flex')
  assert.equal(await computed('flex-stage', 'flex-direction'), 'row')
  assert.equal(await computed('flex-stage', 'flex-wrap'), 'wrap')
  assert.equal(await page.getByTestId('flex-item').count(), 4)
  await page.getByTestId('flex-direction').selectOption('column')
  assert.equal(await computed('flex-stage', 'flex-direction'), 'column')
  assert.match(await page.locator('.wb-flex-axis--main').innerText(), /主轴 ↓/)
  await page.getByTestId('flex-justify').selectOption('center')
  await page.getByTestId('flex-align').selectOption('center')
  assert.equal(await computed('flex-stage', 'justify-content'), 'center')
  assert.equal(await computed('flex-stage', 'align-items'), 'center')
  await page.getByTestId('flex-wrap').uncheck()
  assert.equal(await computed('flex-stage', 'flex-wrap'), 'nowrap')
  await setRange('flex-count', 6)
  assert.equal(await page.getByTestId('flex-item').count(), 6)
  assert.match(await page.getByTestId('demo-section-2.4').locator('.wb-code code').innerText(), /flex-direction: column/)
  await page.getByTestId('flex-reset').click()
  assert.equal(await computed('flex-stage', 'flex-direction'), 'row')
  assert.equal(await page.getByTestId('flex-item').count(), 4)

  // 2.5 grid: real tracks, fr ratio, gaps, span and reset.
  assert.equal(await computed('grid-stage', 'display'), 'grid')
  assert.equal(await computed('grid-stage', 'grid-template-columns').then((value) => value.split(' ').length), 3)
  const defaultGridColumns = await computed('grid-stage', 'grid-template-columns')
  assert.equal(defaultGridColumns.split(' ').length, 3)
  await page.getByTestId('grid-columns').selectOption('1fr 2fr 1fr')
  const gridWidths = []
  for (let index = 0; index < 3; index += 1) gridWidths.push((await rect('grid-item', index)).width)
  assert.ok(gridWidths[1] > gridWidths[0] * 1.6, `fr ratio not applied: ${gridWidths.join(',')}`)
  await page.getByTestId('grid-first-span').check()
  assert.match(await computed('grid-item', 'grid-column'), /span 2/)
  await setRange('grid-column-gap', 20)
  await setRange('grid-row-gap', 8)
  assert.equal(await computed('grid-stage', 'column-gap'), '20px')
  assert.equal(await computed('grid-stage', 'row-gap'), '8px')
  assert.match(await page.getByTestId('demo-section-2.5').locator('.wb-code code').innerText(), /1fr 2fr 1fr/)
  await page.getByTestId('grid-reset').click()
  assert.equal(await computed('grid-stage', 'display'), 'grid')

  // Comparison table is present and distinguishes one-dimensional vs two-dimensional models.
  const comparison = page.getByTestId('display-comparison')
  assert.equal(await comparison.count(), 1)
  assert.match(await comparison.innerText(), /inline/)
  assert.match(await comparison.innerText(), /grid/)
  assert.match(await comparison.innerText(), /一维/)
  assert.match(await comparison.innerText(), /二维/)

  // Hash navigation, active state and refresh.
  await page.getByTestId('practice-link-2.4').click()
  await page.waitForURL('**/web-basics#chapter-2.4')
  assert.equal(await page.locator('.wb-practice-nav__item.is-active').innerText().then((text) => text.includes('2.4')), true)
  await page.reload({ waitUntil: 'networkidle' })
  assert.equal(await page.locator('.wb-practice-nav__item.is-active').innerText().then((text) => text.includes('2.4')), true)
  await page.getByTestId('practice-link-2.1').click()
  await page.waitForURL('**/web-basics#chapter-2.1')
  assert.equal(await page.getByTestId('demo-section-2.1').count(), 1)

  // Responsive: no page-level overflow, controls and demos remain reachable.
  for (const width of [1366, 980, 760, 390]) {
    await page.setViewportSize({ width, height: 900 })
    await open('/web-basics')
    for (const chapter of ['2.1', '2.2', '2.3', '2.4', '2.5']) {
      assert.equal(await page.getByTestId(`demo-section-${chapter}`).count(), 1)
      await noOverflow(`/web-basics ${chapter} ${width}`)
    }
    await setRange('flex-count', 5)
    assert.equal(await page.getByTestId('flex-item').count(), 5)
    await noOverflow(`/web-basics controls ${width}`)
  }

  assert.deepEqual(consoleErrors, [], `console errors: ${consoleErrors.join(' | ')}`)
  console.log('MS06_3_BROWSER_OK')
} finally {
  await browser.close()
}
