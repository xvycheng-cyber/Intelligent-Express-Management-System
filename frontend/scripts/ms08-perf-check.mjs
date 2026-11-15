import { mkdirSync, writeFileSync } from 'node:fs'
import { cpus, totalmem } from 'node:os'
import { fileURLToPath } from 'node:url'
import { performance } from 'node:perf_hooks'

const API = process.env.MS08_API_URL ?? 'http://127.0.0.1:8080/api'
const outputDir =
  process.env.MS08_RESULTS_DIR ??
  fileURLToPath(new URL('../../docs/test-results/ms-08/', import.meta.url))
mkdirSync(outputDir, { recursive: true })

function percentile(values, p) {
  const sorted = [...values].sort((a, b) => a - b)
  const index = Math.min(sorted.length - 1, Math.ceil((p / 100) * sorted.length) - 1)
  return sorted[index]
}

function median(values) {
  const sorted = [...values].sort((a, b) => a - b)
  const middle = Math.floor(sorted.length / 2)
  return sorted.length % 2 === 0
    ? (sorted[middle - 1] + sorted[middle]) / 2
    : sorted[middle]
}

function summarize(name, values, target) {
  const average = values.reduce((sum, value) => sum + value, 0) / values.length
  return {
    name,
    count: values.length,
    targetMs: target,
    averageMs: Number(average.toFixed(2)),
    medianMs: Number(median(values).toFixed(2)),
    p95Ms: Number(percentile(values, 95).toFixed(2)),
    maxMs: Number(Math.max(...values).toFixed(2)),
    minMs: Number(Math.min(...values).toFixed(2)),
    pass: percentile(values, 95) <= target
  }
}

async function timed(path, options) {
  const started = performance.now()
  const response = await fetch(`${API}${path}`, options)
  const text = await response.text()
  return { status: response.status, text, elapsed: performance.now() - started }
}

const lines = Array.from({ length: 100 }, (_, index) => {
  const phone = `177${String(10000000 + index).slice(-8)}`
  return `性能测试${index + 1}，${phone}，浙江省杭州市西湖区性能路${index + 1}号`
}).join('\n')

for (let index = 0; index < 2; index += 1) {
  await timed('/addresses/parse', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ rawText: lines })
  })
  await timed('/addresses?page=1&size=10')
}

const parseTimes = []
const listTimes = []
for (let index = 0; index < 30; index += 1) {
  const parse = await timed('/addresses/parse', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ rawText: lines })
  })
  if (parse.status !== 200) throw new Error(`parse HTTP ${parse.status}: ${parse.text.slice(0, 200)}`)
  parseTimes.push(parse.elapsed)

  const list = await timed('/addresses?page=1&size=10')
  if (list.status !== 200) throw new Error(`list HTTP ${list.status}: ${list.text.slice(0, 200)}`)
  listTimes.push(list.elapsed)
}

const report = {
  generatedAt: new Date().toISOString(),
  api: API,
  runtime: {
    node: process.version,
    cpu: cpus()[0]?.model ?? 'unknown',
    cpuCount: cpus().length,
    totalMemoryMB: Math.round(totalmem() / 1024 / 1024)
  },
  sample: {
    parserLines: 100,
    queryPageSize: 10,
    warmupRuns: 2,
    measuredRuns: 30
  },
  parser: summarize('100-line parse over HTTP', parseTimes, 300),
  list: summarize('address list page (10000-row dataset)', listTimes, 500),
  samples: {
    parserMs: parseTimes,
    listMs: listTimes
  }
}

writeFileSync(`${outputDir}/perf-results.json`, JSON.stringify(report, null, 2))
console.log(JSON.stringify(report, null, 2))
console.log('MS08_PERF_OK')
