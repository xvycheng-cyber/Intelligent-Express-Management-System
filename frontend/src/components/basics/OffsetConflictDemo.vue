<script setup lang="ts">
import { computed, ref } from 'vue'
import type { CSSProperties } from 'vue'
import DemoSection from '@/components/basics/DemoSection.vue'
import CodeBlock from '@/components/basics/CodeBlock.vue'

type Scenario =
  | 'absolute-auto'
  | 'absolute-fixed'
  | 'absolute-vertical'
  | 'relative-both'
  | 'absolute-margin-auto'

const scenarios: { value: Scenario; label: string; explanation: string }[] = [
  {
    value: 'absolute-auto',
    label: 'absolute：left + right，width:auto',
    explanation: '左右同时设置且 width:auto 时，元素在左右之间拉伸。'
  },
  {
    value: 'absolute-fixed',
    label: 'absolute：left + right，固定 width',
    explanation: '左右同时设置但 width 固定时，left 生效，right 被忽略（过度约束取起始边）。'
  },
  {
    value: 'absolute-vertical',
    label: 'absolute：top + bottom，height:auto',
    explanation: '上下同时设置且 height:auto 时，元素在上下之间拉伸。'
  },
  {
    value: 'relative-both',
    label: 'relative：left + right',
    explanation: 'relative 同时设置 left/right 时，left 生效，right 被忽略；原占位仍保留。'
  },
  {
    value: 'absolute-margin-auto',
    label: 'absolute：固定 width + margin:auto',
    explanation: 'left/right 固定、width 固定、左右 margin:auto 时，剩余空间由 auto margin 分配，元素居中。'
  }
]

const defaults = {
  scenario: 'absolute-auto' as Scenario,
  offset: 24,
  width: 220,
  height: 70
}

const scenario = ref<Scenario>(defaults.scenario)
const offset = ref(defaults.offset)
const width = ref(defaults.width)
const height = ref(defaults.height)

const current = computed(() => scenarios.find((item) => item.value === scenario.value) ?? scenarios[0])

const itemStyle = computed<CSSProperties>(() => {
  const common: CSSProperties = {
    top: `${offset.value}px`,
    width: `${width.value}px`,
    height: `${height.value}px`
  }

  if (scenario.value === 'absolute-auto') {
    return { ...common, position: 'absolute', left: `${offset.value}px`, right: `${offset.value}px`, width: 'auto' }
  }
  if (scenario.value === 'absolute-fixed') {
    return { ...common, position: 'absolute', left: `${offset.value}px`, right: `${offset.value}px` }
  }
  if (scenario.value === 'absolute-vertical') {
    return {
      position: 'absolute',
      left: `${offset.value}px`,
      top: `${offset.value}px`,
      bottom: `${offset.value}px`,
      width: `${width.value}px`,
      height: 'auto'
    }
  }
  if (scenario.value === 'relative-both') {
    return {
      position: 'relative',
      left: `${offset.value}px`,
      right: `${offset.value}px`,
      top: `${offset.value}px`,
      width: `${width.value}px`,
      height: `${height.value}px`
    }
  }
  return {
    ...common,
    position: 'absolute',
    left: `${offset.value}px`,
    right: `${offset.value}px`,
    marginLeft: 'auto',
    marginRight: 'auto'
  }
})

const code = computed(() => {
  if (scenario.value === 'absolute-auto') {
    return `.wb-conflict-item {
  position: absolute;
  left: ${offset.value}px;
  right: ${offset.value}px;
  top: ${offset.value}px;
  width: auto;                    /* 左右之间拉伸 */
  height: ${height.value}px;
}`
  }
  if (scenario.value === 'absolute-fixed') {
    return `.wb-conflict-item {
  position: absolute;
  left: ${offset.value}px;
  right: ${offset.value}px;
  top: ${offset.value}px;
  width: ${width.value}px;         /* left 生效，right 被忽略 */
  height: ${height.value}px;
}`
  }
  if (scenario.value === 'absolute-vertical') {
    return `.wb-conflict-item {
  position: absolute;
  left: ${offset.value}px;
  top: ${offset.value}px;
  bottom: ${offset.value}px;
  width: ${width.value}px;
  height: auto;                    /* 上下之间拉伸 */
}`
  }
  if (scenario.value === 'relative-both') {
    return `.wb-conflict-item {
  position: relative;
  left: ${offset.value}px;
  right: ${offset.value}px;
  top: ${offset.value}px;
  width: ${width.value}px;         /* relative: left 生效 */
  height: ${height.value}px;
}`
  }
  return `.wb-conflict-item {
  position: absolute;
  left: ${offset.value}px;
  right: ${offset.value}px;
  top: ${offset.value}px;
  width: ${width.value}px;
  height: ${height.value}px;
  margin-left: auto;               /* 剩余空间居中 */
  margin-right: auto;
}`
})

function reset() {
  scenario.value = defaults.scenario
  offset.value = defaults.offset
  width.value = defaults.width
  height.value = defaults.height
}
</script>

<template>
  <DemoSection
    number="2.17"
    title="定位属性之间的覆盖影响"
    target="用真实 CSS 观察多方向偏移、width/height:auto、固定尺寸和 margin:auto 的覆盖/拉伸规则。"
  >
    <template #demo>
      <div class="wb-demo-controls" aria-label="2.17 定位偏移覆盖参数">
        <label class="wb-control wb-control--text">
          <span>场景</span>
          <select v-model="scenario" data-testid="conflict-scenario">
            <option v-for="item in scenarios" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
        </label>
        <label class="wb-control">
          <span>偏移量 <b>{{ offset }}px</b></span>
          <input v-model.number="offset" data-testid="conflict-offset" type="range" min="0" max="60" step="2" />
        </label>
        <label class="wb-control">
          <span>固定 width <b>{{ width }}px</b></span>
          <input v-model.number="width" data-testid="conflict-width" type="range" min="120" max="300" step="10" />
        </label>
        <label class="wb-control">
          <span>固定 height <b>{{ height }}px</b></span>
          <input v-model.number="height" data-testid="conflict-height" type="range" min="48" max="120" step="2" />
        </label>
        <button class="wb-reset" type="button" data-testid="conflict-reset" @click="reset">重置</button>
      </div>

      <div class="wb-conflict-stage" data-testid="conflict-stage">
        <div class="wb-conflict-item" :style="itemStyle" data-testid="conflict-item">
          {{ current.label }}
        </div>
      </div>

      <p class="wb-demo-note">{{ current.explanation }}</p>
      <p class="wb-demo-note">
        本演示只通过真实 CSS 的 left/right/top/bottom、width/height 和 margin 观察覆盖规则，
        不用 JavaScript 计算元素位置。四个方向同时设置时并非总是“同时生效”，过度约束会按 CSS 规则取舍。
      </p>
    </template>

    <template #conclusion>
      <p>
        absolute 同时设置 left/right 且 width:auto 时拉伸；width 固定时取 left、忽略 right。
        top/bottom 同理。relative 同时设置 left/right 时 left 生效。固定宽度 + 左右 margin:auto
        可以让绝对定位元素在 left/right 之间居中。不要把这些规则简化成“四个方向永远同时相加”。
      </p>
    </template>

    <template #code>
      <CodeBlock :code="code" />
    </template>
  </DemoSection>
</template>
