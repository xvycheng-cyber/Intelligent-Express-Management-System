<script setup lang="ts">
import { computed, ref } from 'vue'
import type { CSSProperties } from 'vue'
import DemoSection from '@/components/basics/DemoSection.vue'
import CodeBlock from '@/components/basics/CodeBlock.vue'

type Direction = 'row' | 'row-reverse' | 'column' | 'column-reverse'
type Justify = 'flex-start' | 'center' | 'flex-end' | 'space-between' | 'space-around' | 'space-evenly'
type Align = 'stretch' | 'flex-start' | 'center' | 'flex-end' | 'baseline'

const defaults = {
  count: 4,
  direction: 'row' as Direction,
  justify: 'flex-start' as Justify,
  align: 'stretch' as Align,
  wrap: true,
  gap: 12,
  stageWidth: 620
}

const count = ref(defaults.count)
const direction = ref<Direction>(defaults.direction)
const justify = ref<Justify>(defaults.justify)
const align = ref<Align>(defaults.align)
const wrap = ref(defaults.wrap)
const gap = ref(defaults.gap)
const stageWidth = ref(defaults.stageWidth)

const isColumn = computed(() => direction.value.startsWith('column'))
const axisMain = computed(() => (isColumn.value ? '主轴 ↓' : '主轴 →'))
const axisCross = computed(() => (isColumn.value ? '交叉轴 →' : '交叉轴 ↓'))

const stageStyle = computed<CSSProperties>(() => ({
  width: `${stageWidth.value}px`,
  flexDirection: direction.value,
  justifyContent: justify.value,
  alignItems: align.value,
  flexWrap: wrap.value ? 'wrap' : 'nowrap',
  gap: `${gap.value}px`
}))

const itemStyle = computed(() => ({
  flexBasis: `${isColumn.value ? 46 : 82}px`
}))

const code = computed(
  () => `.wb-flex-demo__stage {
  display: flex;
  width: ${stageWidth.value}px;
  flex-direction: ${direction.value};
  justify-content: ${justify.value};
  align-items: ${align.value};
  flex-wrap: ${wrap.value ? 'wrap' : 'nowrap'};
  gap: ${gap.value}px;
}

.wb-flex-demo__item {
  flex: 0 0 auto;
  flex-basis: ${isColumn.value ? 46 : 82}px;
  min-height: 48px;
}`
)

function reset() {
  count.value = defaults.count
  direction.value = defaults.direction
  justify.value = defaults.justify
  align.value = defaults.align
  wrap.value = defaults.wrap
  gap.value = defaults.gap
  stageWidth.value = defaults.stageWidth
}
</script>

<template>
  <DemoSection
    number="2.4"
    title="弹性显示类型"
    target="观察 flex 的主轴/交叉轴、方向、对齐、换行和 gap；主轴方向会随 flex-direction 改变。"
  >
    <template #demo>
      <div class="wb-demo-controls" aria-label="2.4 弹性显示类型参数">
        <label class="wb-control">
          <span>项目数 <b>{{ count }}</b></span>
          <input v-model.number="count" data-testid="flex-count" type="range" min="2" max="6" step="1" />
        </label>
        <label class="wb-control">
          <span>flex-direction</span>
          <select v-model="direction" data-testid="flex-direction">
            <option value="row">row</option>
            <option value="row-reverse">row-reverse</option>
            <option value="column">column</option>
            <option value="column-reverse">column-reverse</option>
          </select>
        </label>
        <label class="wb-control">
          <span>justify-content（主轴）</span>
          <select v-model="justify" data-testid="flex-justify">
            <option value="flex-start">flex-start</option>
            <option value="center">center</option>
            <option value="flex-end">flex-end</option>
            <option value="space-between">space-between</option>
            <option value="space-around">space-around</option>
            <option value="space-evenly">space-evenly</option>
          </select>
        </label>
        <label class="wb-control">
          <span>align-items（交叉轴）</span>
          <select v-model="align" data-testid="flex-align">
            <option value="stretch">stretch</option>
            <option value="flex-start">flex-start</option>
            <option value="center">center</option>
            <option value="flex-end">flex-end</option>
            <option value="baseline">baseline</option>
          </select>
        </label>
        <label class="wb-control wb-control--check">
          <input v-model="wrap" data-testid="flex-wrap" type="checkbox" />
          <span>允许换行（flex-wrap: wrap）</span>
        </label>
        <label class="wb-control">
          <span>gap <b>{{ gap }}px</b></span>
          <input v-model.number="gap" data-testid="flex-gap" type="range" min="0" max="24" step="1" />
        </label>
        <label class="wb-control">
          <span>容器宽度 <b>{{ stageWidth }}px</b></span>
          <input v-model.number="stageWidth" data-testid="flex-stage-width" type="range" min="300" max="720" step="10" />
        </label>
        <button class="wb-reset" type="button" data-testid="flex-reset" @click="reset">重置</button>
      </div>

      <div class="wb-demo-stage wb-flex-demo">
        <div class="wb-flex-viewport">
          <div
            class="wb-flex-demo__stage"
            data-testid="flex-stage"
            :style="stageStyle"
          >
            <span class="wb-flex-axis wb-flex-axis--main" aria-hidden="true">{{ axisMain }}</span>
            <span class="wb-flex-axis wb-flex-axis--cross" aria-hidden="true">{{ axisCross }}</span>
            <div
              v-for="index in count"
              :key="index"
              class="wb-flex-demo__item"
              data-testid="flex-item"
              :style="itemStyle"
            >
              项目 {{ index }}
            </div>
          </div>
        </div>
        <p class="wb-demo-note">
          主轴方向由 <code>flex-direction</code> 决定：row 时水平、column 时垂直。
          <code>justify-content</code> 沿主轴分配空间；<code>align-items</code> 沿交叉轴对齐项目。
          把 direction 切到 column 后再观察两个轴标签和对齐结果。窄屏下演示容器可横向滚动，页面本身不会横向溢出。
        </p>
      </div>
    </template>

    <template #conclusion>
      <p>
        Flex 是<strong>一维布局容器</strong>：项目沿一条主轴排列。主轴由 <code>flex-direction</code> 决定，
        <code>justify-content</code> 控制主轴分配，<code>align-items</code> 控制交叉轴对齐；方向改变后，
        两条轴也随之改变，不能把“水平居中/垂直居中”套用到所有方向。
      </p>
    </template>

    <template #code>
      <CodeBlock :code="code" />
    </template>
  </DemoSection>
</template>
