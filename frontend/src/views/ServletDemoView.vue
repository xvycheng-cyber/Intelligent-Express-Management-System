<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink } from 'vue-router'
import AppIcon from '@/components/common/AppIcon.vue'
import { runServletRequest, type ServletDemoResult } from '@/api/servletDemo'

type DemoRow = {
  id: string
  group: string
  title: string
  method: 'GET' | 'POST'
  path: string
  expected: string
  note: string
}

const rows: DemoRow[] = [
  {
    id: 'exact',
    group: 'Servlet 容器',
    title: '精确匹配',
    method: 'GET',
    path: '/demo/servlet/exact',
    expected: 'ExactMappingDemoServlet · 200',
    note: '精确 pattern 优先于同前缀路径映射'
  },
  {
    id: 'exact-extra',
    group: 'Servlet 容器',
    title: '精确不完全匹配',
    method: 'GET',
    path: '/demo/servlet/exact/extra',
    expected: 'PathMappingDemoServlet · 200',
    note: '多出的 /extra 使请求落到路径匹配'
  },
  {
    id: 'path',
    group: 'Servlet 容器',
    title: '路径匹配',
    method: 'GET',
    path: '/demo/servlet/prefix/a',
    expected: 'PathMappingDemoServlet · 200',
    note: 'pathInfo=/prefix/a'
  },
  {
    id: 'extension',
    group: 'Servlet 容器',
    title: '扩展名匹配',
    method: 'GET',
    path: '/demo/legacy/report.smap',
    expected: 'ExtensionMappingDemoServlet · 200',
    note: '*.smap 与目录位置无关'
  },
  {
    id: 'conflict',
    group: '映射优先级',
    title: '路径 vs 扩展名',
    method: 'GET',
    path: '/demo/servlet/legacy/report.smap',
    expected: 'PathMappingDemoServlet · 200',
    note: '路径映射优先于扩展名映射'
  },
  {
    id: 'spring',
    group: 'Spring MVC',
    title: 'Controller 对照',
    method: 'GET',
    path: '/demo/spring/info',
    expected: 'ServletMappingDemoController · 200',
    note: '请求先进入 DispatcherServlet，再匹配 Handler'
  },
  {
    id: 'exact-post',
    group: '方法边界',
    title: 'Servlet 不支持 POST',
    method: 'POST',
    path: '/demo/servlet/exact',
    expected: '405 Method Not Allowed',
    note: 'Servlet 映射不看方法，HttpServlet 分派拒绝 POST'
  },
  {
    id: 'spring-post',
    group: '方法边界',
    title: 'Spring 不支持 POST',
    method: 'POST',
    path: '/demo/spring/method-only',
    expected: '405 Method Not Allowed · R.code=40501',
    note: 'Spring MVC 路由看 HTTP 方法'
  },
  {
    id: 'default-404',
    group: '默认映射',
    title: '默认 / 边界',
    method: 'GET',
    path: '/demo/legacy/report.json',
    expected: 'DispatcherServlet → 404 · R.code=40402',
    note: '默认 / 由 DispatcherServlet 占用；不是自定义默认 Servlet'
  },
  {
    id: 'query',
    group: '参数边界',
    title: '查询参数',
    method: 'GET',
    path: '/demo/servlet/exact?trace=query-42',
    expected: 'ExactMappingDemoServlet · 200',
    note: 'query 不改变 URL Pattern 匹配'
  }
]

const results = ref<Record<string, ServletDemoResult>>({})
const activeId = ref('exact')
const busyId = ref('')
const runningAll = ref(false)
const lastRunAt = ref('')

const activeResult = computed(() => results.value[activeId.value])

function rowResult(id: string): ServletDemoResult | undefined {
  return results.value[id]
}

function statusClass(result?: ServletDemoResult): string {
  if (!result) return 'is-idle'
  if (result.error) return 'is-error'
  if (result.status >= 200 && result.status < 300) return 'is-ok'
  if (result.status === 404) return 'is-missing'
  if (result.status === 405) return 'is-method'
  return 'is-error'
}

function statusText(result?: ServletDemoResult): string {
  if (!result) return '未请求'
  if (result.error) return '请求失败'
  return `${result.status} ${result.statusText}`
}

function actualHandler(result?: ServletDemoResult): string {
  if (!result) return '—'
  if (result.demoHandler) return result.demoHandler
  if (result.json && typeof result.json === 'object' && 'handler' in result.json) {
    return String((result.json as { handler?: unknown }).handler ?? '—')
  }
  if (result.status === 404) return '未匹配 Handler（DispatcherServlet 404）'
  if (result.status === 405) return '未返回 handler（405 方法分派）'
  if (result.error) return '—'
  return '响应未返回 handler'
}

function prettyBody(result?: ServletDemoResult): string {
  if (!result) return ''
  if (result.error) return result.error
  if (result.json) return JSON.stringify(result.json, null, 2)
  return result.bodyText || '（空响应正文）'
}

function resultHeaders(result?: ServletDemoResult): Array<[string, string]> {
  if (!result) return []
  return [
    ['content-type', result.contentType || '—'],
    ['allow', result.allow || '—'],
    ['x-demo-handler', result.demoHandler || '—'],
    ['x-demo-mapping-type', result.demoMappingType || '—']
  ]
}

async function execute(row: DemoRow) {
  const result = await runServletRequest(row.method, row.path)
  results.value = { ...results.value, [row.id]: result }
  activeId.value = row.id
  lastRunAt.value = new Date().toLocaleTimeString()
}

async function runOne(row: DemoRow) {
  if (busyId.value || runningAll.value) return
  busyId.value = row.id
  try {
    await execute(row)
  } finally {
    busyId.value = ''
  }
}

async function runAll() {
  if (busyId.value || runningAll.value) return
  runningAll.value = true
  try {
    for (const row of rows) {
      busyId.value = row.id
      await execute(row)
    }
  } finally {
    busyId.value = ''
    runningAll.value = false
  }
}

function clearResults() {
  if (busyId.value || runningAll.value) return
  results.value = {}
  activeId.value = rows[0].id
  lastRunAt.value = ''
}
</script>

<template>
  <div class="servlet-page" data-testid="servlet-demo-page">
    <header class="servlet-head">
      <div>
        <span class="wb-kicker">2.20 · Servlet URI 映射</span>
        <h1>Servlet 映射演示</h1>
        <p>
          一次请求先由 Servlet 容器按 <code>url-pattern</code> 选中 Servlet；只有进入
          <code>DispatcherServlet</code> 后，Spring MVC 才按 Controller 映射进行二次分发。
          本页所有结果来自真实 HTTP 请求。
        </p>
      </div>
      <div class="servlet-head__actions">
        <RouterLink class="servlet-button servlet-button--ghost" to="/web-basics">返回课程实践</RouterLink>
        <RouterLink class="servlet-button servlet-button--ghost" to="/import">进入快递业务</RouterLink>
      </div>
    </header>

    <section class="servlet-hero" aria-labelledby="servlet-flow-title">
      <div class="servlet-hero__copy">
        <span class="wb-kicker">CONTAINER → DISPATCHER</span>
        <h2 id="servlet-flow-title">先容器，后 Spring</h2>
        <p>
          Tomcat 先做 URL Pattern 匹配。Spring MVC 的 <code>@GetMapping</code> 不是容器映射；
          它只是 <code>DispatcherServlet</code> 收到请求后的 Handler 选择。
        </p>
      </div>
      <div class="servlet-flow" aria-label="Servlet 映射优先级">
        <div class="servlet-flow__step">
          <span>01</span>
          <strong>精确匹配</strong>
          <small>/demo/servlet/exact</small>
        </div>
        <AppIcon class="servlet-flow__arrow" name="chevron" :size="16" />
        <div class="servlet-flow__step">
          <span>02</span>
          <strong>路径匹配</strong>
          <small>/demo/servlet/*</small>
        </div>
        <AppIcon class="servlet-flow__arrow" name="chevron" :size="16" />
        <div class="servlet-flow__step">
          <span>03</span>
          <strong>扩展名</strong>
          <small>*.smap</small>
        </div>
        <AppIcon class="servlet-flow__arrow" name="chevron" :size="16" />
        <div class="servlet-flow__step servlet-flow__step--default">
          <span>04</span>
          <strong>默认 /</strong>
          <small>DispatcherServlet</small>
        </div>
      </div>
    </section>

    <section class="servlet-card" aria-labelledby="servlet-compare-title">
      <header class="servlet-card__head">
        <div>
          <span class="wb-kicker">两层规则</span>
          <h2 id="servlet-compare-title">容器映射 vs Spring MVC</h2>
        </div>
        <span class="servlet-badge">真实运行</span>
      </header>
      <div class="servlet-compare">
        <div class="servlet-compare__col">
          <h3>Servlet 容器</h3>
          <ul>
            <li>匹配单位：<code>url-pattern</code></li>
            <li>发生在请求进入 Servlet 之前</li>
            <li>精确 → 最长路径 → 扩展名 → 默认 <code>/</code></li>
            <li>同一个 Servlet 可接收多种 HTTP 方法</li>
          </ul>
          <p class="servlet-compare__example">本项目：<code>/demo/servlet/*</code>、<code>*.smap</code></p>
        </div>
        <div class="servlet-compare__col servlet-compare__col--accent">
          <h3>Spring MVC</h3>
          <ul>
            <li>匹配单位：Handler 路径 + 方法 + 参数</li>
            <li>发生在 DispatcherServlet 内部</li>
            <li>支持路径变量、路径通配</li>
            <li>方法不匹配时返回 405</li>
          </ul>
          <p class="servlet-compare__example">本项目：<code>/demo/spring/info</code>、<code>/demo/spring/echo/{name}</code></p>
        </div>
      </div>
    </section>

    <section class="servlet-card" aria-labelledby="servlet-mapping-title">
      <header class="servlet-card__head">
        <div>
          <span class="wb-kicker">映射样本</span>
          <h2 id="servlet-mapping-title">三种真实 Servlet 映射</h2>
        </div>
      </header>
      <div class="servlet-mapping-grid">
        <article class="servlet-mapping">
          <span class="servlet-mapping__type">EXACT</span>
          <h3>精确匹配</h3>
          <code>/demo/servlet/exact</code>
          <p>只有完全相同的路径命中；精确 pattern 优先于同前缀路径映射。</p>
        </article>
        <article class="servlet-mapping">
          <span class="servlet-mapping__type">PATH</span>
          <h3>路径匹配</h3>
          <code>/demo/servlet/*</code>
          <p>匹配该前缀下的剩余路径，剩余部分进入 <code>pathInfo</code>。</p>
        </article>
        <article class="servlet-mapping">
          <span class="servlet-mapping__type">EXTENSION</span>
          <h3>扩展名匹配</h3>
          <code>*.smap</code>
          <p>按扩展名选择 Servlet，与目录位置无关；路径映射仍优先。</p>
        </article>
      </div>
    </section>

    <section class="servlet-card servlet-lab" aria-labelledby="servlet-lab-title">
      <header class="servlet-card__head servlet-card__head--wrap">
        <div>
          <span class="wb-kicker">REAL REQUESTS</span>
          <h2 id="servlet-lab-title">真实请求矩阵</h2>
          <p class="servlet-card__sub">点击单行执行，或运行全部。404/405 也保留服务器原始状态和正文。</p>
        </div>
        <div class="servlet-lab__actions">
          <span v-if="lastRunAt" class="servlet-run-time">最近运行 {{ lastRunAt }}</span>
          <button class="servlet-button servlet-button--ghost" type="button" :disabled="runningAll || !!busyId" data-testid="servlet-clear" @click="clearResults">
            清空结果
          </button>
          <button class="servlet-button servlet-button--primary" type="button" :disabled="runningAll || !!busyId" data-testid="servlet-run-all" @click="runAll">
            {{ runningAll ? '运行中…' : '运行全部请求' }}
          </button>
        </div>
      </header>

      <div class="servlet-matrix-wrap">
        <table class="servlet-matrix">
          <thead>
            <tr>
              <th scope="col">请求</th>
              <th scope="col">预期</th>
              <th scope="col">上次状态</th>
              <th scope="col">实际命中</th>
              <th scope="col">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in rows" :key="row.id" :class="{ 'is-active': activeId === row.id }">
              <td data-label="请求">
                <div class="servlet-req">
                  <span class="servlet-method" :class="`is-${row.method.toLowerCase()}`">{{ row.method }}</span>
                  <code>{{ row.path }}</code>
                </div>
                <small>{{ row.title }} · {{ row.group }}</small>
              </td>
              <td data-label="预期">{{ row.expected }}</td>
              <td data-label="上次状态">
                <span class="servlet-status" :class="statusClass(rowResult(row.id))" :data-testid="`status-${row.id}`">
                  {{ statusText(rowResult(row.id)) }}
                </span>
              </td>
              <td data-label="实际命中">
                <span class="servlet-actual" :data-testid="`actual-${row.id}`">
                  {{ actualHandler(rowResult(row.id)) }}
                </span>
              </td>
              <td data-label="操作">
                <button
                  class="servlet-mini-button"
                  type="button"
                  :disabled="!!busyId || runningAll"
                  :data-testid="`run-${row.id}`"
                  :aria-label="`执行 ${row.method} ${row.path}`"
                  @click="runOne(row)"
                >
                  {{ busyId === row.id ? '请求中…' : '执行' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <p class="servlet-lab__hint">
        <AppIcon name="info" :size="14" />
        实际命中优先读取响应 JSON 的 <code>handler</code>；404/405 响应没有成功 handler 时，页面如实显示状态与原始正文。
      </p>
    </section>

    <section class="servlet-detail-grid" aria-label="最近一次请求详情">
      <article class="servlet-card servlet-detail">
        <header class="servlet-card__head">
          <div>
            <span class="wb-kicker">RESPONSE</span>
            <h2>最近一次请求</h2>
          </div>
          <span class="servlet-badge" :class="statusClass(activeResult)" data-testid="active-status">
            {{ activeResult ? statusText(activeResult) : '尚未执行' }}
          </span>
        </header>

        <div v-if="activeResult" class="servlet-response">
          <div class="servlet-response__meta">
            <div><span>方法 / 路径</span><strong>{{ activeResult.method }} {{ activeResult.url }}</strong></div>
            <div><span>耗时</span><strong data-testid="active-elapsed">{{ activeResult.elapsedMs }} ms</strong></div>
            <div><span>Content-Type</span><strong>{{ activeResult.contentType || '—' }}</strong></div>
            <div><span>实际命中</span><strong data-testid="active-handler">{{ actualHandler(activeResult) }}</strong></div>
          </div>
          <div class="servlet-headers">
            <span v-for="[key, value] in resultHeaders(activeResult)" :key="key" class="servlet-header">
              <code>{{ key }}</code>{{ value }}
            </span>
          </div>
          <pre class="servlet-body" data-testid="active-body">{{ prettyBody(activeResult) }}</pre>
        </div>
        <div v-else class="servlet-empty">
          <AppIcon name="route" :size="22" />
          <p>选择矩阵中的一条请求，查看真实状态码、响应头和正文。</p>
        </div>
      </article>

      <aside class="servlet-card servlet-boundary">
        <span class="wb-kicker">BOUNDARY</span>
        <h2>404 / 405 / 默认映射</h2>
        <div class="servlet-boundary__item">
          <strong>405 Method Not Allowed</strong>
          <p>自定义 Servlet 只实现 <code>doGet</code>，POST 不会被改成 200；Spring 的 <code>method-only</code> 也返回 405。</p>
        </div>
        <div class="servlet-boundary__item">
          <strong>404 未匹配</strong>
          <p><code>/demo/legacy/report.json</code> 不匹配 <code>*.smap</code>，进入默认 <code>/</code> 的 DispatcherServlet；没有 Handler 时返回 404。</p>
        </div>
        <div class="servlet-boundary__item servlet-boundary__item--warning">
          <strong>为什么没有自定义 <code>/</code>？</strong>
          <p>当前 <code>/</code> 已由 DispatcherServlet 使用。新增自定义 <code>/</code> 会抢占 Spring 和业务路由，因此这里只展示默认映射边界，不新增冲突 Servlet。</p>
        </div>
        <RouterLink class="servlet-text-link" to="/web-basics">回顾 2.1～2.19 布局实践 →</RouterLink>
      </aside>
    </section>

    <footer class="servlet-footer">
      <span>MS-07 · Servlet URI 映射演示 · 真实 HTTP 请求，不使用 Mock 命中结果</span>
      <code>container → DispatcherServlet → Handler</code>
    </footer>
  </div>
</template>

<style scoped lang="scss">
.servlet-page {
  width: min(1240px, 100%);
  margin: 0 auto;
  padding-bottom: 12px;
}

.servlet-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 28px;
  margin-bottom: 18px;
}

.servlet-head h1 {
  margin: 5px 0 8px;
  color: var(--color-text);
  font-size: clamp(26px, 3vw, 34px);
  line-height: 1.18;
  letter-spacing: -0.025em;
}

.servlet-head p {
  max-width: 780px;
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.8;
}

.servlet-head__actions {
  display: flex;
  flex: 0 0 auto;
  gap: 8px;
}

.servlet-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 36px;
  padding: 0 13px;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
  text-decoration: none;
  transition: background-color 160ms ease, border-color 160ms ease, color 160ms ease;
}

.servlet-button--primary {
  background: var(--color-primary);
  color: #fff;
}

.servlet-button--primary:hover:not(:disabled) {
  background: var(--color-primary-strong);
}

.servlet-button--ghost {
  border-color: var(--color-border-strong);
  background: var(--color-surface);
  color: var(--color-text-secondary);
}

.servlet-button--ghost:hover:not(:disabled) {
  border-color: var(--color-primary);
  color: var(--color-primary-strong);
}

.servlet-button:disabled {
  cursor: not-allowed;
  opacity: 0.56;
}

.servlet-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 14px;
  padding: 22px;
  border: 1px solid #1e3a5f;
  border-radius: var(--radius-lg);
  background:
    linear-gradient(135deg, rgba(3, 105, 161, 0.23), transparent 45%),
    var(--color-sidebar);
  color: #e2e8f0;
  box-shadow: var(--shadow-card);
}

.servlet-hero .wb-kicker {
  color: #7dd3fc;
}

.servlet-hero h2 {
  margin: 5px 0 6px;
  color: #fff;
  font-size: 20px;
}

.servlet-hero p {
  max-width: 520px;
  margin: 0;
  color: #94a3b8;
  font-size: 12px;
  line-height: 1.8;
}

.servlet-hero code {
  color: #bae6fd;
}

.servlet-flow {
  display: flex;
  align-items: center;
  gap: 5px;
  flex: 0 1 560px;
}

.servlet-flow__step {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 3px;
  min-width: 84px;
  padding: 10px;
  border: 1px solid #334155;
  border-radius: var(--radius-sm);
  background: rgba(255, 255, 255, 0.035);
}

.servlet-flow__step > span {
  color: #38bdf8;
  font-family: ui-monospace, SFMono-Regular, Consolas, monospace;
  font-size: 9px;
}

.servlet-flow__step strong {
  color: #f8fafc;
  font-size: 11px;
}

.servlet-flow__step small {
  overflow: hidden;
  color: #94a3b8;
  font-family: ui-monospace, SFMono-Regular, Consolas, monospace;
  font-size: 9px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.servlet-flow__step--default {
  border-color: #0ea5e9;
  background: rgba(14, 165, 233, 0.12);
}

.servlet-flow__arrow {
  flex: 0 0 auto;
  align-self: center;
  color: #475569;
  transform: rotate(-90deg);
}

.servlet-card {
  margin-bottom: 14px;
  padding: 18px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-base);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}

.servlet-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.servlet-card__head--wrap {
  flex-wrap: wrap;
}

.servlet-card__head h2 {
  margin: 3px 0 0;
  color: var(--color-text);
  font-size: 17px;
}

.servlet-card__sub {
  margin: 5px 0 0;
  color: var(--color-text-muted);
  font-size: 11px;
}

.servlet-badge {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 9px;
  border-radius: 999px;
  background: var(--color-primary-soft);
  color: var(--color-primary-strong);
  font-size: 10px;
  font-weight: 700;
  white-space: nowrap;
}

.servlet-compare {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.servlet-compare__col {
  padding: 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: #f8fafc;
}

.servlet-compare__col--accent {
  border-color: #bae6fd;
  background: #f0f9ff;
}

.servlet-compare h3 {
  margin: 0 0 9px;
  color: var(--color-text);
  font-size: 13px;
}

.servlet-compare ul {
  margin: 0;
  padding-left: 17px;
  color: var(--color-text-secondary);
  font-size: 11px;
  line-height: 1.75;
}

.servlet-compare__example {
  margin: 10px 0 0;
  color: var(--color-text-muted);
  font-size: 10px;
  line-height: 1.6;
}

.servlet-compare code,
.servlet-mapping code,
.servlet-boundary code,
.servlet-lab__hint code {
  overflow-wrap: anywhere;
  font-family: ui-monospace, SFMono-Regular, Consolas, monospace;
}

.servlet-mapping-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.servlet-mapping {
  min-width: 0;
  padding: 13px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: #f8fafc;
}

.servlet-mapping__type {
  color: var(--color-primary);
  font-family: ui-monospace, SFMono-Regular, Consolas, monospace;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.servlet-mapping h3 {
  margin: 4px 0 6px;
  color: var(--color-text);
  font-size: 13px;
}

.servlet-mapping code {
  display: block;
  color: var(--color-primary-strong);
  font-size: 10px;
}

.servlet-mapping p {
  margin: 8px 0 0;
  color: var(--color-text-muted);
  font-size: 10px;
  line-height: 1.7;
}

.servlet-lab {
  padding-bottom: 14px;
}

.servlet-lab__actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 7px;
}

.servlet-run-time {
  color: var(--color-text-muted);
  font-size: 10px;
}

.servlet-matrix-wrap {
  overflow-x: auto;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}

.servlet-matrix {
  width: 100%;
  min-width: 760px;
  border-collapse: collapse;
  font-size: 11px;
}

.servlet-matrix th {
  padding: 9px 10px;
  border-bottom: 1px solid var(--color-border);
  background: #f8fafc;
  color: var(--color-text-muted);
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-align: left;
  text-transform: uppercase;
}

.servlet-matrix td {
  padding: 10px;
  border-bottom: 1px solid #edf2f7;
  color: var(--color-text-secondary);
  vertical-align: middle;
}

.servlet-matrix tr:last-child td {
  border-bottom: 0;
}

.servlet-matrix tbody tr.is-active {
  background: #f0f9ff;
}

.servlet-matrix tbody tr:hover {
  background: #f8fbfe;
}

.servlet-req {
  display: flex;
  align-items: center;
  gap: 7px;
  min-width: 0;
}

.servlet-req code {
  overflow: hidden;
  color: var(--color-text);
  font-size: 10px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.servlet-matrix td small {
  display: block;
  margin-top: 3px;
  color: var(--color-text-muted);
  font-size: 9px;
}

.servlet-method {
  flex: 0 0 auto;
  padding: 2px 5px;
  border-radius: 4px;
  background: #e0f2fe;
  color: #075985;
  font-family: ui-monospace, SFMono-Regular, Consolas, monospace;
  font-size: 9px;
  font-weight: 700;
}

.servlet-method.is-post {
  background: #fef3c7;
  color: #92400e;
}

.servlet-status {
  display: inline-block;
  padding: 3px 6px;
  border-radius: 4px;
  font-family: ui-monospace, SFMono-Regular, Consolas, monospace;
  font-size: 9px;
  font-weight: 700;
  white-space: nowrap;
}

.servlet-status.is-idle {
  background: #f1f5f9;
  color: var(--color-text-muted);
}

.servlet-status.is-ok {
  background: var(--color-success-soft);
  color: var(--color-success);
}

.servlet-status.is-missing {
  background: #fff7ed;
  color: #c2410c;
}

.servlet-status.is-method {
  background: var(--color-warning-soft);
  color: var(--color-warning);
}

.servlet-status.is-error {
  background: var(--color-danger-soft);
  color: var(--color-danger);
}

.servlet-actual {
  display: block;
  max-width: 190px;
  color: var(--color-text-secondary);
  font-size: 10px;
  line-height: 1.5;
}

.servlet-mini-button {
  min-height: 28px;
  padding: 0 9px;
  border: 1px solid var(--color-border-strong);
  border-radius: 5px;
  background: var(--color-surface);
  color: var(--color-primary-strong);
  cursor: pointer;
  font-size: 10px;
  font-weight: 700;
  white-space: nowrap;
}

.servlet-mini-button:hover:not(:disabled) {
  border-color: var(--color-primary);
  background: var(--color-primary-soft);
}

.servlet-mini-button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.servlet-lab__hint {
  display: flex;
  align-items: flex-start;
  gap: 5px;
  margin: 10px 0 0;
  color: var(--color-text-muted);
  font-size: 10px;
  line-height: 1.7;
}

.servlet-detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(280px, 0.75fr);
  gap: 14px;
}

.servlet-detail-grid > .servlet-card {
  margin-bottom: 0;
}

.servlet-response__meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 7px;
  margin-bottom: 10px;
}

.servlet-response__meta > div {
  min-width: 0;
  padding: 8px;
  border: 1px solid var(--color-border);
  border-radius: 5px;
  background: #f8fafc;
}

.servlet-response__meta span {
  display: block;
  margin-bottom: 2px;
  color: var(--color-text-muted);
  font-size: 9px;
}

.servlet-response__meta strong {
  display: block;
  overflow-wrap: anywhere;
  color: var(--color-text-secondary);
  font-size: 10px;
  font-weight: 600;
}

.servlet-headers {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-bottom: 10px;
}

.servlet-header {
  padding: 3px 6px;
  border-radius: 4px;
  background: var(--color-surface-soft);
  color: var(--color-text-muted);
  font-size: 9px;
}

.servlet-header code {
  color: var(--color-primary-strong);
  font-family: ui-monospace, SFMono-Regular, Consolas, monospace;
}

.servlet-body {
  max-height: 260px;
  margin: 0;
  overflow: auto;
  padding: 11px;
  border: 1px solid #1e293b;
  border-radius: 6px;
  background: #0f172a;
  color: #cbd5e1;
  font-family: ui-monospace, SFMono-Regular, Consolas, monospace;
  font-size: 10px;
  line-height: 1.65;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.servlet-empty {
  display: flex;
  min-height: 150px;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 7px;
  color: var(--color-text-muted);
  text-align: center;
}

.servlet-empty p {
  max-width: 280px;
  margin: 0;
  font-size: 11px;
  line-height: 1.7;
}

.servlet-boundary h2 {
  margin: 3px 0 10px;
  color: var(--color-text);
  font-size: 17px;
}

.servlet-boundary__item {
  padding: 9px 0;
  border-top: 1px solid var(--color-border);
}

.servlet-boundary__item strong {
  color: var(--color-text-secondary);
  font-size: 11px;
}

.servlet-boundary__item p {
  margin: 3px 0 0;
  color: var(--color-text-muted);
  font-size: 10px;
  line-height: 1.7;
}

.servlet-boundary__item--warning {
  margin-top: 4px;
  padding: 10px;
  border: 1px solid #fde68a;
  border-radius: 6px;
  background: #fffbeb;
}

.servlet-text-link {
  display: inline-block;
  margin-top: 10px;
  color: var(--color-primary);
  font-size: 10px;
  font-weight: 700;
  text-decoration: none;
}

.servlet-text-link:hover {
  text-decoration: underline;
}

.servlet-footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 2px 0;
  color: var(--color-text-muted);
  font-size: 9px;
}

.servlet-footer code {
  font-family: ui-monospace, SFMono-Regular, Consolas, monospace;
}

@media (max-width: 980px) {
  .servlet-head {
    flex-direction: column;
    gap: 12px;
  }

  .servlet-head__actions {
    flex-wrap: wrap;
  }

  .servlet-hero {
    align-items: stretch;
    flex-direction: column;
  }

  .servlet-flow {
    flex-basis: auto;
    flex-wrap: wrap;
  }

  .servlet-flow__arrow {
    transform: rotate(0);
  }

  .servlet-detail-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 760px) {
  .servlet-compare,
  .servlet-mapping-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .servlet-card {
    padding: 14px;
  }

  .servlet-response__meta {
    grid-template-columns: minmax(0, 1fr);
  }

  .servlet-footer {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .servlet-flow__step {
    min-width: calc(50% - 12px);
  }

  .servlet-flow__arrow {
    display: none;
  }

  .servlet-lab__actions {
    align-items: stretch;
    flex-direction: column;
  }

  .servlet-button {
    width: 100%;
  }

  .servlet-matrix {
    min-width: 680px;
  }
}
</style>
