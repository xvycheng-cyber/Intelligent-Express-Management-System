export interface ServletDemoResult {
  method: string
  url: string
  status: number
  statusText: string
  elapsedMs: number
  contentType: string
  allow: string
  demoHandler: string
  demoMappingType: string
  bodyText: string
  json: unknown
  error: string
}

const TIMEOUT_MS = 8_000

/**
 * Run one real request against the demo Servlet/Spring endpoints.
 * Native fetch is intentional: Axios would throw on 404/405 and hide the
 * server body that this teaching page needs to display.
 */
export async function runServletRequest(method: string, url: string): Promise<ServletDemoResult> {
  const started = performance.now()
  const controller = new AbortController()
  const timer = window.setTimeout(() => controller.abort(), TIMEOUT_MS)

  try {
    const response = await fetch(url, {
      method,
      headers: { Accept: 'application/json' },
      signal: controller.signal
    })
    const bodyText = await response.text()
    let json: unknown = null
    if (bodyText.trim().startsWith('{') || bodyText.trim().startsWith('[')) {
      try {
        json = JSON.parse(bodyText)
      } catch {
        json = null
      }
    }
    return {
      method,
      url,
      status: response.status,
      statusText: response.statusText,
      elapsedMs: Math.round(performance.now() - started),
      contentType: response.headers.get('content-type') ?? '',
      allow: response.headers.get('allow') ?? '',
      demoHandler: response.headers.get('x-demo-handler') ?? '',
      demoMappingType: response.headers.get('x-demo-mapping-type') ?? '',
      bodyText,
      json,
      error: ''
    }
  } catch (error) {
    const message = error instanceof DOMException && error.name === 'AbortError'
      ? '请求超时（8s）'
      : error instanceof Error
        ? error.message
        : '网络错误'
    return {
      method,
      url,
      status: 0,
      statusText: 'NETWORK_ERROR',
      elapsedMs: Math.round(performance.now() - started),
      contentType: '',
      allow: '',
      demoHandler: '',
      demoMappingType: '',
      bodyText: '',
      json: null,
      error: message
    }
  } finally {
    window.clearTimeout(timer)
  }
}
