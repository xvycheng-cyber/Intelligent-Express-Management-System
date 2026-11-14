<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import DemoSection from '@/components/basics/DemoSection.vue'

const highlight = ref(true)
const viewportWidth = ref(0)
const shellRef = ref<HTMLElement | null>(null)
const measurements = ref({
  shellWidth: 0,
  sidebarWidth: 0,
  mainWidth: 0,
  topbarHeight: 0
})

const layoutMode = computed(() => {
  if (viewportWidth.value <= 640) return '窄屏：侧栏上下堆叠，顶栏连接/用户信息精简'
  if (viewportWidth.value <= 980) return '中窄屏：侧栏变为顶部横向导航'
  return '宽屏：左侧固定导航 + 右侧工作区'
})

function applyHighlight() {
  if (!shellRef.value) return
  shellRef.value.classList.toggle('wb-layout-highlight', highlight.value)
}

function measure() {
  nextTick(() => {
    const shell = document.querySelector<HTMLElement>('.app-shell')
    const sidebar = document.querySelector<HTMLElement>('.sidebar')
    const topbar = document.querySelector<HTMLElement>('.topbar')
    const main = document.querySelector<HTMLElement>('.app-main')
    if (!shell || !sidebar || !topbar || !main) return
    measurements.value = {
      shellWidth: Math.round(shell.getBoundingClientRect().width),
      sidebarWidth: Math.round(sidebar.getBoundingClientRect().width),
      mainWidth: Math.round(main.getBoundingClientRect().width),
      topbarHeight: Math.round(topbar.getBoundingClientRect().height)
    }
  })
}

function refresh() {
  viewportWidth.value = window.innerWidth
  applyHighlight()
  measure()
}

function toggleHighlight() {
  highlight.value = !highlight.value
}

watch(highlight, () => {
  applyHighlight()
  measure()
})

onMounted(() => {
  shellRef.value = document.querySelector<HTMLElement>('.app-shell')
  viewportWidth.value = window.innerWidth
  applyHighlight()
  measure()
  window.addEventListener('resize', refresh)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', refresh)
  shellRef.value?.classList.remove('wb-layout-highlight')
})
</script>

<template>
  <DemoSection
    number="2.19"
    title="布局原则思考"
    target="用真实 AppLayout 的 topbar、sidebar、app-main 做高亮对照，观察区域层级、尺寸关系与响应式变化。"
  >
    <template #demo>
      <div class="wb-layout-controls">
        <label class="wb-control wb-control--check">
          <input
            :checked="highlight"
            data-testid="layout-highlight-toggle"
            type="checkbox"
            @change="toggleHighlight"
          />
          <span>在真实系统布局上显示区域高亮</span>
        </label>
        <button class="wb-reset" type="button" data-testid="layout-refresh" @click="refresh">刷新测量</button>
        <span class="wb-layout-mode" data-testid="layout-mode">{{ layoutMode }}</span>
      </div>

      <div class="wb-layout-legend" aria-label="真实布局区域图例">
        <span><i class="wb-layout-swatch wb-layout-swatch--topbar"></i>顶部栏 · <code>.topbar</code>：面包屑 / 服务状态</span>
        <span><i class="wb-layout-swatch wb-layout-swatch--sidebar"></i>侧边导航 · <code>.sidebar</code>：工作台 / 课程实践</span>
        <span><i class="wb-layout-swatch wb-layout-swatch--main"></i>主内容 · <code>.app-main</code>：页面标题 / 操作 / 内容</span>
      </div>

      <div class="wb-layout-metrics">
        <div class="wb-metric">
          <span>shell 宽度</span>
          <strong>{{ measurements.shellWidth }}px</strong>
          <small>真实 <code>.app-shell</code></small>
        </div>
        <div class="wb-metric">
          <span>sidebar 宽度</span>
          <strong>{{ measurements.sidebarWidth }}px</strong>
          <small>真实 <code>.sidebar</code>；窄屏会改变</small>
        </div>
        <div class="wb-metric">
          <span>主内容宽度</span>
          <strong>{{ measurements.mainWidth }}px</strong>
          <small>真实 <code>.app-main</code></small>
        </div>
        <div class="wb-metric">
          <span>topbar 高度</span>
          <strong>{{ measurements.topbarHeight }}px</strong>
          <small>真实 <code>.topbar</code></small>
        </div>
      </div>

      <div class="wb-layout-map">
        <div class="wb-layout-map__row">
          <span class="wb-layout-map__class">.app-shell</span>
          <span>整体 flex / 响应式容器；宽屏左右结构，≤980px 改为上下结构。</span>
        </div>
        <div class="wb-layout-map__row">
          <span class="wb-layout-map__class">.topbar</span>
          <span>顶部系统栏：面包屑、服务状态、本机演示；≤640px 精简状态项。</span>
        </div>
        <div class="wb-layout-map__row">
          <span class="wb-layout-map__class">.sidebar</span>
          <span>侧边导航：236px 固定宽；≤980px 变成顶部横向导航。</span>
        </div>
        <div class="wb-layout-map__row">
          <span class="wb-layout-map__class">.app-main</span>
          <span>主内容：min(1440px, 100%)，页面标题、操作区和内容卡片在内。</span>
        </div>
      </div>

      <p class="wb-demo-note">
        高亮是添加在真实 DOM 上的 outline 类，不使用截图，也不改变盒模型尺寸；outline 不占布局空间，
        因此不会移动页面内容。调整浏览器宽度，模式说明、sidebar 宽度和 topbar 精简状态会随真实布局变化。
      </p>
    </template>

    <template #conclusion>
      <p>
        页面布局原则来自真实结构：`.app-shell` 负责整体排列，`.sidebar` 与 `.workspace`
        构成主次关系，`.topbar` 提供全局定位与状态，`.app-main` 承载业务内容并限制可读宽度。
        响应式不是另一套虚构布局，而是这些真实容器在 980px/640px 断点下的行为。
      </p>
    </template>
  </DemoSection>
</template>
