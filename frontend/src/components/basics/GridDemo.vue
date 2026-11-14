<script setup lang="ts">
import { computed, ref } from 'vue'
import DemoSection from '@/components/basics/DemoSection.vue'
import CodeBlock from '@/components/basics/CodeBlock.vue'

const columnOptions = [
  'repeat(3, 1fr)',
  '1fr 1fr 1fr',
  '1fr 2fr 1fr',
  '120px 1fr 1fr'
] as const

type ColumnPattern = (typeof columnOptions)[number]

const defaults = {
  count: 6,
  columns: 'repeat(3, 1fr)' as ColumnPattern,
  rows: 2,
  rowGap: 12,
  columnGap: 12,
  stageWidth: 620,
  firstSpan: false
}

const count = ref(defaults.count)
const columns = ref<ColumnPattern>(defaults.columns)
const rows = ref(defaults.rows)
const rowGap = ref(defaults.rowGap)
const columnGap = ref(defaults.columnGap)
const stageWidth = ref(defaults.stageWidth)
const firstSpan = ref(defaults.firstSpan)

const stageStyle = computed(() => ({
  width: `${stageWidth.value}px`,
  gridTemplateColumns: columns.value,
  gridTemplateRows: `repeat(${rows.value}, 58px)`,
  columnGap: `${columnGap.value}px`,
  rowGap: `${rowGap.value}px`
}))

const firstItemStyle = computed(() => ({
  gridColumn: firstSpan.value ? 'span 2' : 'auto'
}))

const code = computed(
  () => `.wb-grid-demo__stage {
  display: grid;
  width: ${stageWidth.value}px;
  grid-template-columns: ${columns.value};
  grid-template-rows: repeat(${rows.value}, 58px);
  column-gap: ${columnGap.value}px;
  row-gap: ${rowGap.value}px;
}

.wb-grid-demo__item:first-child {
  grid-column: ${firstSpan.value ? 'span 2' : 'auto'};
}`
)

function reset() {
  count.value = defaults.count
  columns.value = defaults.columns
  rows.value = defaults.rows
  rowGap.value = defaults.rowGap
  columnGap.value = defaults.columnGap
  stageWidth.value = defaults.stageWidth
  firstSpan.value = defaults.firstSpan
}
</script>

<template>
  <DemoSection
    number="2.5"
    title="网格显示类型"
    target="观察 grid 的二维行列、fr 空间分配、gap 与跨列；对比固定列宽和比例列宽。"
  >
    <template #demo>
      <div class="wb-demo-controls" aria-label="2.5 网格显示类型参数">
        <label class="wb-control">
          <span>项目数 <b>{{ count }}</b></span>
          <input v-model.number="count" data-testid="grid-count" type="range" min="3" max="9" step="1" />
        </label>
        <label class="wb-control">
          <span>grid-template-columns</span>
          <select v-model="columns" data-testid="grid-columns">
            <option v-for="option in columnOptions" :key="option" :value="option">{{ option }}</option>
          </select>
        </label>
        <label class="wb-control">
          <span>行轨道数 <b>{{ rows }}</b></span>
          <input v-model.number="rows" data-testid="grid-rows" type="range" min="1" max="3" step="1" />
        </label>
        <label class="wb-control">
          <span>列间距 <b>{{ columnGap }}px</b></span>
          <input v-model.number="columnGap" data-testid="grid-column-gap" type="range" min="0" max="24" step="1" />
        </label>
        <label class="wb-control">
          <span>行间距 <b>{{ rowGap }}px</b></span>
          <input v-model.number="rowGap" data-testid="grid-row-gap" type="range" min="0" max="24" step="1" />
        </label>
        <label class="wb-control">
          <span>容器宽度 <b>{{ stageWidth }}px</b></span>
          <input v-model.number="stageWidth" data-testid="grid-stage-width" type="range" min="320" max="720" step="10" />
        </label>
        <label class="wb-control wb-control--check">
          <input v-model="firstSpan" data-testid="grid-first-span" type="checkbox" />
          <span>首项跨 2 列</span>
        </label>
        <button class="wb-reset" type="button" data-testid="grid-reset" @click="reset">重置</button>
      </div>

      <div class="wb-demo-stage wb-grid-demo">
        <div class="wb-grid-viewport">
          <div class="wb-grid-demo__stage" data-testid="grid-stage" :style="stageStyle">
            <div
              v-for="index in count"
              :key="index"
              class="wb-grid-demo__item"
              data-testid="grid-item"
              :style="index === 1 ? firstItemStyle : undefined"
            >
              项目 {{ index }}
            </div>
          </div>
        </div>
        <p class="wb-demo-note">
          网格同时定义行和列，<code>fr</code> 按可用空间比例分配轨道，不是简单百分比；
          <code>repeat(3, 1fr)</code> 与手写三个 <code>1fr</code> 等价。改列模式或容器宽度，观察轨道重新分配；
          勾选“首项跨 2 列”观察 <code>grid-column: span 2</code>。窄屏下演示容器可横向滚动，页面本身不会横向溢出。
        </p>
      </div>
    </template>

    <template #conclusion>
      <p>
        Grid 是<strong>二维布局容器</strong>：项目同时沿行和列排列。列轨道由
        <code>grid-template-columns</code> 定义，行轨道由 <code>grid-template-rows</code> 定义；
        <code>fr</code> 表示可用空间的比例份额，固定列宽则不参与比例分配。Grid 与 Flex 的差异不是“谁更高级”，
        而是一维与二维布局模型的选择。
      </p>
    </template>

    <template #code>
      <CodeBlock :code="code" />
    </template>
  </DemoSection>
</template>
