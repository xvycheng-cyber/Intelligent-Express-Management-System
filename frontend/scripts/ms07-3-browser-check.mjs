import assert from 'node:assert/strict'
import { chromium } from 'playwright'

const BASE = process.env.MS06_BASE_URL ?? 'http://127.0.0.1:5173'
const browser = await chromium.launch({ channel: 'chrome', headless: true })
const page = await browser.newPage({ viewport: { width: 1366, height: 900 } })
const consoleErrors = []
const pageErrors = []
const observedUrls = []

page.on('console', (message) => {
  if (message.type() !== 'error') return
  // 404/405 are intentional teaching results on this page; Chromium logs a
  // resource error for every non-2xx fetch. Keep all other console errors.
  if (/Failed to load resource: the server responded with a status of (404|405)/.test(message.text())) return
  consoleErrors.push(message.text())
})
page.on('pageerror', (error) => pageErrors.push(`pageerror: ${error.message}`))
page.on('request', (request) => observedUrls.push(request.url()))

async function open(path) {
  await page.goto(`${BASE}${path}`, { waitUntil: 'networkidle' })
  await page.waitForTimeout(100)
}

async function noOverflow(label) {
  const dimensions = await page.evaluate(() => ({
    scrollWidth: document.documentElement.scrollWidth,
    innerWidth: window.innerWidth
  }))
  assert.ok(dimensions.scrollWidth <= dimensions.innerWidth, `${label}: ${JSON.stringify(dimensions)}`)
}

async function waitForStatus(id, statusText) {
  await page.waitForFunction(
    ({ rowId, expected }) => document.querySelector(`[data-testid="status-${rowId}"]`)?.textContent?.includes(expected),
    { rowId: id, expected: statusText },
    { timeout: 10_000 }
  )
}

try {
  await open('/servlet-demo')
  await page.getByTestId('servlet-demo-page').waitFor()
  assert.equal(await page.getByRole('heading', { name: 'Servlet 映射演示' }).count(), 1)
  assert.equal(
    await page.locator('nav[aria-label="课程实践导航"] .side-nav__item.is-active').innerText(),
    'Servlet 映射演示'
  )
  assert.equal(await page.locator('.app-main').innerText().then((text) => text.includes('真实请求矩阵')), true)
  assert.equal(await page.locator('nav[aria-label="主导航"]').count(), 1)

  // Exact GET is a real 200 response from the embedded Servlet.
  await page.getByTestId('run-exact').click()
  await waitForStatus('exact', '200')
  assert.match(await page.getByTestId('actual-exact').innerText(), /ExactMappingDemoServlet/)
  assert.match(await page.getByTestId('active-body').innerText(), /"handler": "ExactMappingDemoServlet"/)
  assert.match(await page.getByTestId('active-elapsed').innerText(), /\d+ ms/)
  assert.equal(await page.locator('.servlet-header').filter({ hasText: 'content-type' }).count(), 1)

  // Path/extension/Spring are real distinct handlers.
  await page.getByTestId('run-path').click()
  await waitForStatus('path', '200')
  assert.match(await page.getByTestId('actual-path').innerText(), /PathMappingDemoServlet/)
  await page.getByTestId('run-extension').click()
  await waitForStatus('extension', '200')
  assert.match(await page.getByTestId('actual-extension').innerText(), /ExtensionMappingDemoServlet/)
  await page.getByTestId('run-spring').click()
  await waitForStatus('spring', '200')
  assert.match(await page.getByTestId('actual-spring').innerText(), /ServletMappingDemoController/)

  // 405 and 404 must stay real non-200 statuses.
  await page.getByTestId('run-exact-post').click()
  await waitForStatus('exact-post', '405')
  assert.match(await page.getByTestId('active-status').innerText(), /405/)
  await page.getByTestId('run-spring-post').click()
  await waitForStatus('spring-post', '405')
  assert.match(await page.getByTestId('active-body').innerText(), /40501/)
  await page.getByTestId('run-default-404').click()
  await waitForStatus('default-404', '404')
  assert.match(await page.getByTestId('active-body').innerText(), /40402/)

  // Query parameter keeps the exact mapping and is echoed by the server.
  await page.getByTestId('run-query').click()
  await waitForStatus('query', '200')
  assert.match(await page.getByTestId('active-body').innerText(), /trace=query-42/)
  assert.match(await page.getByTestId('actual-query').innerText(), /ExactMappingDemoServlet/)

  // Clear and run-all are real state transitions, not decorative buttons.
  await page.getByTestId('servlet-clear').click()
  assert.equal(await page.getByTestId('active-status').innerText(), '尚未执行')
  await page.getByTestId('servlet-run-all').click()
  await page.waitForFunction(() => {
    const button = document.querySelector('[data-testid="servlet-run-all"]')
    return button && !button.hasAttribute('disabled') && document.querySelector('[data-testid="status-query"]')?.textContent?.includes('200')
  }, { timeout: 20_000 })
  assert.equal(await page.getByTestId('status-default-404').innerText().then((text) => text.includes('404')), true)

  // Refresh keeps the route and the page is still usable.
  await page.reload({ waitUntil: 'networkidle' })
  await page.getByTestId('servlet-demo-page').waitFor()
  assert.equal(await page.getByTestId('active-status').innerText(), '尚未执行')

  // The browser must not call address business APIs from this page.
  assert.equal(observedUrls.some((url) => url.includes('/api/addresses')), false, 'servlet page called address API')

  // 404 route remains the app 404, not a fake servlet page.
  await open('/servlet-demo/not-a-real-route')
  await page.getByRole('heading', { name: '页面不存在' }).waitFor()

  // Responsive: page-level overflow must stay absent; matrix may scroll internally.
  for (const width of [1366, 980, 760, 390]) {
    await page.setViewportSize({ width, height: 900 })
    await open('/servlet-demo')
    await page.getByTestId('servlet-demo-page').waitFor()
    await noOverflow(`servlet-demo ${width}`)
    await page.getByTestId('run-exact').click()
    await waitForStatus('exact', '200')
    await noOverflow(`servlet-demo after request ${width}`)
  }

  assert.deepEqual(consoleErrors, [], `console errors: ${consoleErrors.join(' | ')}`)
  assert.deepEqual(pageErrors, [], `page errors: ${pageErrors.join(' | ')}`)
  console.log('MS07_3_BROWSER_OK')
} finally {
  await browser.close()
}
