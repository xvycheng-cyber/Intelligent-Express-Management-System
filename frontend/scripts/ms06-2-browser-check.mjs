import assert from 'node:assert/strict'
import { chromium } from 'playwright'

const BASE = process.env.MS06_BASE_URL ?? 'http://127.0.0.1:5173'
const browser = await chromium.launch({ channel: 'chrome', headless: true })
const page = await browser.newPage({ viewport: { width: 1366, height: 768 } })
const consoleErrors = []
page.on('console', (message) => {
  if (message.type() === 'error') consoleErrors.push(message.text())
})
page.on('pageerror', (error) => consoleErrors.push(`pageerror: ${error.message}`))

async function noPageOverflow(label) {
  await page.waitForTimeout(120)
  const dimensions = await page.evaluate(() => ({
    scrollWidth: document.documentElement.scrollWidth,
    innerWidth: window.innerWidth
  }))
  assert.ok(
    dimensions.scrollWidth <= dimensions.innerWidth,
    `${label} overflow: ${dimensions.scrollWidth} > ${dimensions.innerWidth}`
  )
}

async function open(path) {
  await page.goto(`${BASE}${path}`, { waitUntil: 'networkidle' })
  await page.waitForTimeout(120)
}

try {
  // 1) Direct route + practice navigation + chapter skeleton
  await open('/web-basics')
  await page.getByTestId('web-basics-page').waitFor()
  assert.equal(await page.locator('.wb-page__heading h1').innerText(), 'Web 基础实践')
  assert.equal(await page.locator('.wb-chapter').count(), 19)
  assert.equal(await page.getByTestId('practice-nav').count(), 1)
  assert.equal(await page.locator('.wb-practice-nav__foot').innerText(), '2.20 Servlet 映射演示\n已实现')
  assert.equal(
    await page.locator('nav[aria-label="课程实践导航"] .side-nav__item.is-active').innerText(),
    'Web 基础实践'
  )
  assert.equal(await page.getByTestId('chapter-2.18').count(), 1)
  assert.match(await page.getByTestId('chapter-2.18').innerText(), /快递通系统原创线框|原创线框/)
  assert.equal(await page.locator('.side-nav__item.is-disabled').filter({ hasText: 'Servlet' }).count(), 0)
  assert.ok(await page.locator('a[href="/servlet-demo"]').count() >= 1)

  // 2) 2.18 entry and two distinct modes
  await page.getByTestId('chapter-2.18').click()
  await page.waitForURL('**/web-basics/wireframe')
  await page.getByTestId('wireframe-page').waitFor()
  assert.equal(await page.locator('.wb-page__heading h1').innerText(), '快递通系统原创线框设计')
  assert.equal(await page.getByTestId('wireframe-plan-panel').count(), 1)
  assert.equal(await page.getByTestId('wireframe-implemented-panel').count(), 0)
  assert.match(await page.getByTestId('wireframe-plan-panel').innerText(), /低保真/)
  assert.equal(
    await page.locator('nav[aria-label="课程实践导航"] .side-nav__item.is-active').innerText(),
    'Web 基础实践'
  )

  await page.getByTestId('wireframe-mode-implemented').click()
  await page.getByTestId('wireframe-implemented-panel').waitFor()
  assert.equal(await page.getByTestId('wireframe-plan-panel').count(), 0)
  assert.equal(await page.getByTestId('wireframe-preview-import').count(), 1)
  assert.equal(await page.locator('.import-page').count(), 1)
  assert.equal(await page.locator('.wb-wireframe-panel.is-implemented').count(), 1)
  assert.equal(await page.locator('.wb-wireframe-panel.is-plan').count(), 0)

  // 3) Refresh keeps the distinct implemented mode; direct plan URL works.
  await page.reload({ waitUntil: 'networkidle' })
  assert.equal(await page.getByTestId('wireframe-implemented-panel').count(), 1)
  await open('/web-basics/wireframe?mode=plan')
  assert.equal(await page.getByTestId('wireframe-plan-panel').count(), 1)

  // 4) Servlet demo is a real route; an unknown subpath is still the app 404.
  await open('/servlet-demo/not-a-real-route')
  await page.getByRole('heading', { name: '页面不存在' }).waitFor()

  // 5) Business page regression at desktop.
  await open('/import')
  await page.getByRole('heading', { name: '批量录入收件地址' }).waitFor()
  assert.equal(
    await page.locator('nav[aria-label="主导航"] .side-nav__item.is-active').innerText(),
    '批量地址录入'
  )
  assert.equal(await page.locator('.side-nav__item.is-active').count(), 1)
  await noPageOverflow('/import 1366')

  await open('/addresses')
  await page.getByRole('heading', { name: '收件地址簿' }).waitFor()
  assert.equal(
    await page.locator('nav[aria-label="主导航"] .side-nav__item.is-active').innerText(),
    '地址簿管理'
  )
  await noPageOverflow('/addresses 1366')

  // 6) Responsive checks for both practice pages and business pages.
  for (const width of [1366, 980, 760, 390]) {
    await page.setViewportSize({ width, height: 900 })
    await open('/web-basics')
    await page.getByTestId('web-basics-page').waitFor()
    await noPageOverflow(`/web-basics ${width}`)

    await open('/web-basics/wireframe')
    await page.getByTestId('wireframe-page').waitFor()
    await noPageOverflow(`/web-basics/wireframe ${width}`)

    await open('/import')
    await page.getByRole('heading', { name: '批量录入收件地址' }).waitFor()
    await noPageOverflow(`/import ${width}`)

    await open('/addresses')
    await page.getByRole('heading', { name: '收件地址簿' }).waitFor()
    await noPageOverflow(`/addresses ${width}`)
  }

  assert.deepEqual(consoleErrors, [], `console errors: ${consoleErrors.join(' | ')}`)
  console.log('MS06_2_BROWSER_OK')
} finally {
  await browser.close()
}
