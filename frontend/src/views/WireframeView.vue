<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import ImportView from '@/views/ImportView.vue'
import AddressListView from '@/views/AddressListView.vue'

type WireframeMode = 'plan' | 'implemented'
type PreviewPage = 'import' | 'addresses'

const route = useRoute()
const router = useRouter()
const mode = ref<WireframeMode>(route.query.mode === 'implemented' ? 'implemented' : 'plan')
const previewPage = ref<PreviewPage>(route.query.page === 'addresses' ? 'addresses' : 'import')

const modeLabel = computed(() => (mode.value === 'plan' ? 'Plan · 设计板' : 'Implemented · 真实页面'))

const navItems = [
  { label: '批量地址录入', note: '已实现' },
  { label: '地址簿管理', note: '已实现' },
  { label: 'Web 基础实践', note: '进行中' },
  { label: 'Servlet 映射演示', note: '待实现' }
]

function setMode(next: WireframeMode) {
  mode.value = next
  void router.replace({ query: { ...route.query, mode: next } })
}

function setPreviewPage(next: PreviewPage) {
  previewPage.value = next
  void router.replace({ query: { ...route.query, mode: 'implemented', page: next } })
}
</script>

<template>
  <div class="wb-page wb-wireframe-page" data-testid="wireframe-page">
    <header class="wb-page__header">
      <div class="wb-page__heading">
        <span class="wb-kicker">2.18 · 原创系统线框</span>
        <h1>快递通系统原创线框设计</h1>
        <p>
          课程原题为“使用线框风格仿制XXX网页的布局”；经项目负责人批准，本项目改为使用线框风格
          展示快递通自身的页面布局。左侧 Plan 是设计板，Implemented 是真实运行的页面组件，两者严格分开。
        </p>
      </div>
      <div class="wb-page__actions">
        <RouterLink class="wb-button wb-button--ghost" to="/web-basics">返回实践目录</RouterLink>
        <RouterLink class="wb-button wb-button--ghost" to="/import">打开真实录入页</RouterLink>
      </div>
    </header>

    <div class="wb-wireframe-toolbar">
      <div class="wb-wireframe-toolbar__copy">
        <span class="wb-kicker">模式</span>
        <strong>{{ modeLabel }}</strong>
      </div>
      <div class="wb-wireframe-tabs" role="group" aria-label="线框展示模式">
        <button
          class="wb-wireframe-tab"
          :class="{ 'is-active': mode === 'plan' }"
          type="button"
          :aria-pressed="mode === 'plan'"
          data-testid="wireframe-mode-plan"
          @click="setMode('plan')"
        >
          Plan 设计板
        </button>
        <button
          class="wb-wireframe-tab"
          :class="{ 'is-active': mode === 'implemented' }"
          type="button"
          :aria-pressed="mode === 'implemented'"
          data-testid="wireframe-mode-implemented"
          @click="setMode('implemented')"
        >
          Implemented 真实页面
        </button>
      </div>
    </div>

    <section v-if="mode === 'plan'" class="wb-wireframe-panel is-plan" data-testid="wireframe-plan-panel">
      <header class="wb-wireframe-panel__head">
        <div>
          <span class="wb-kicker">PLAN</span>
          <h2>快递通系统页面布局设计板</h2>
        </div>
        <span class="wb-status wb-status--skeleton">原创线框</span>
      </header>
      <p class="wb-wireframe-panel__note">
        低保真设计板：只表达系统框架、区域层级、对齐和主要操作，不代表最终视觉。没有使用外部网页、
        Logo、Banner、图片或字体。
      </p>

      <div class="wb-plan-board" data-testid="wireframe-plan-board">
        <div class="wb-plan-shell">
          <div class="wb-plan-topbar">
            <span class="wb-plan-brand">快递通 · Express Address</span>
            <span class="wb-plan-topbar__nav">工作台 / 课程实践</span>
            <span class="wb-plan-topbar__status">本机演示 · 服务状态</span>
          </div>
          <div class="wb-plan-body">
            <aside class="wb-plan-sidebar">
              <span class="wb-plan-sidebar__label">工作台</span>
              <div v-for="item in navItems.slice(0, 2)" :key="item.label" class="wb-plan-nav">
                <span>{{ item.label }}</span>
                <small>{{ item.note }}</small>
              </div>
              <span class="wb-plan-sidebar__label wb-plan-sidebar__label--spaced">课程实践</span>
              <div v-for="item in navItems.slice(2)" :key="item.label" class="wb-plan-nav">
                <span>{{ item.label }}</span>
                <small>{{ item.note }}</small>
              </div>
            </aside>

            <main class="wb-plan-main">
              <div class="wb-plan-page" data-testid="plan-import-page">
                <div class="wb-plan-page__head">
                  <div>
                    <span class="wb-plan-page__kicker">智能录入</span>
                    <strong>批量地址录入</strong>
                  </div>
                  <div class="wb-plan-actions"><span>新建</span><span>刷新</span></div>
                </div>
                <div class="wb-plan-workflow">
                  <span>01 粘贴原文</span><span>02 智能识别</span><span>03 人工核对</span><span>04 确认保存</span>
                </div>
                <div class="wb-plan-grid">
                  <div class="wb-plan-block wb-plan-block--input">
                    <span class="wb-plan-label">多行文本输入区</span>
                    <div class="wb-plan-lines"><i></i><i></i><i></i><i></i></div>
                  </div>
                  <div class="wb-plan-block">
                    <span class="wb-plan-label">解析结果预览 / 可编辑表格</span>
                    <div class="wb-plan-table"><i></i><i></i><i></i></div>
                  </div>
                </div>
                <div class="wb-plan-block wb-plan-block--save">
                  <span class="wb-plan-label">确认保存 / 异常行提示</span>
                  <span class="wb-plan-button">保存全部有效记录</span>
                </div>
              </div>

              <div class="wb-plan-page" data-testid="plan-address-page">
                <div class="wb-plan-page__head">
                  <div>
                    <span class="wb-plan-page__kicker">地址簿</span>
                    <strong>地址簿管理</strong>
                  </div>
                  <div class="wb-plan-actions"><span>新建地址</span><span>刷新</span></div>
                </div>
                <div class="wb-plan-metrics"><span>总记录</span><span>当前页</span><span>已选中</span></div>
                <div class="wb-plan-search"><span>姓名 / 电话 / 地址搜索</span><span>搜索</span><span>重置</span></div>
                <div class="wb-plan-block">
                  <span class="wb-plan-label">地址列表 / 每行编辑、删除</span>
                  <div class="wb-plan-table wb-plan-table--wide"><i></i><i></i><i></i><i></i></div>
                </div>
                <div class="wb-plan-pagination"><span>共 N 条</span><span>上一页</span><span>1 2 3</span><span>下一页</span></div>
              </div>

              <div class="wb-plan-relation">
                <span class="wb-plan-label">导航关系</span>
                <span>批量地址录入 → 地址簿管理</span>
                <span>Web 基础实践（当前进行中）</span>
                <span>Servlet 映射演示（待实现，不创建虚假路由）</span>
              </div>
            </main>
          </div>
        </div>
      </div>

      <div class="wb-wireframe-legend">
        <span><i class="wb-wireframe-legend__box"></i> 内容占位</span>
        <span><i class="wb-wireframe-legend__line"></i> 容器边界</span>
        <span>灰阶低保真 · 无外部素材 / 真实业务数据</span>
      </div>
    </section>

    <section v-else class="wb-wireframe-panel is-implemented" data-testid="wireframe-implemented-panel">
      <header class="wb-wireframe-panel__head">
        <div>
          <span class="wb-kicker">IMPLEMENTED</span>
          <h2>真实页面运行效果</h2>
        </div>
        <span class="wb-status wb-status--ready">真实页面</span>
      </header>
      <p class="wb-wireframe-panel__note">
        下方是真实 Vue 页面组件，不是截图；外层是真实 AppLayout。预览为只读，点击右侧链接可进入实际页面操作。
      </p>

      <div class="wb-live-toolbar">
        <div class="wb-live-tabs" role="group" aria-label="真实页面选择">
          <button
            class="wb-live-tab"
            :class="{ 'is-active': previewPage === 'import' }"
            type="button"
            data-testid="preview-page-import"
            @click="setPreviewPage('import')"
          >
            智能录入页
          </button>
          <button
            class="wb-live-tab"
            :class="{ 'is-active': previewPage === 'addresses' }"
            type="button"
            data-testid="preview-page-addresses"
            @click="setPreviewPage('addresses')"
          >
            地址簿页
          </button>
        </div>
        <RouterLink
          class="wb-button wb-button--ghost"
          :to="previewPage === 'import' ? '/import' : '/addresses'"
        >
          打开真实页面 →
        </RouterLink>
      </div>

      <div
        class="wb-live-preview"
        data-testid="wireframe-live-preview"
        aria-label="真实页面只读预览"
        inert
      >
        <ImportView v-if="previewPage === 'import'" data-testid="wireframe-preview-import" />
        <AddressListView v-else data-testid="wireframe-preview-addresses" />
      </div>

      <div class="wb-plan-relation wb-plan-relation--live">
        <span class="wb-plan-label">与 Plan 的对应关系</span>
        <span>顶部系统栏 / 左侧导航 / 主内容区</span>
        <span>{{ previewPage === 'import' ? '智能录入：输入 → 解析 → 预览 → 保存' : '地址簿：搜索 → 列表 → 操作 → 分页' }}</span>
        <span>真实 DOM / 真实布局；无截图、无静态图片替代</span>
      </div>
    </section>

    <footer class="wb-page__footer">
      <span>2.18 只使用本系统原创线框；外部网页、学校首页、Logo、Banner 均不进入本项目。</span>
      <RouterLink to="/web-basics">返回实践目录 →</RouterLink>
    </footer>
  </div>
</template>
