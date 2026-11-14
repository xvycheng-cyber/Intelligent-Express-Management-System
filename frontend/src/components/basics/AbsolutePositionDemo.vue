<script setup lang="ts">
import { computed, ref } from 'vue'
import type { CSSProperties } from 'vue'
import DemoSection from '@/components/basics/DemoSection.vue'
import CodeBlock from '@/components/basics/CodeBlock.vue'

type Mode = 'static' | 'absolute'
type Axis = 'top' | 'right' | 'bottom' | 'left'

const defaults = {
  mode: 'absolute' as Mode,
  axis: 'right' as Axis,
  offset: 28,
  itemWidth: 190,
  itemHeight: 58
}

const mode = ref<Mode>(defaults.mode)
const axis = ref<Axis>(defaults.axis)
const offset = ref(defaults.offset)
const itemWidth = ref(defaults.itemWidth)
const itemHeight = ref(defaults.itemHeight)

const itemStyle = computed<CSSProperties>(() => {
  const base: CSSProperties = {
    width: `${itemWidth.value}px`,
    height: `${itemHeight.value}px`
  }
  if (mode.value === 'static') {
    return { ...base, position: 'static' }
  }
  const value = `${offset.value}px`
  const offsetStyle: CSSProperties = {}
  if (axis.value === 'top') offsetStyle.top = value
  if (axis.value === 'right') offsetStyle.right = value
  if (axis.value === 'bottom') offsetStyle.bottom = value
  if (axis.value === 'left') offsetStyle.left = value
  return { ...base, position: 'absolute', ...offsetStyle }
})

const code = computed(
  () => `.wb-absolute-item {
  position: ${mode.value};
  width: ${itemWidth.value}px;
  height: ${itemHeight.value}px;
${mode.value === 'absolute' ? `  ${axis.value}: ${offset.value}px;\n` : ''}}

/* 绝对定位元素不保留正常文档流占位 */
/* 后续元素会向前填补 */`
)

function reset() {
  mode.value = defaults.mode
  axis.value = defaults.axis
  offset.value = defaults.offset
  itemWidth.value = defaults.itemWidth
  itemHeight.value = defaults.itemHeight
}
</script>

<template>
  <DemoSection
    number="2.11"
    title="绝对定位"
    target="切换 static / absolute，观察元素脱流、原位置占位消失，以及后续元素如何前移。"
  >
    <template #demo>
      <div class="wb-demo-controls" aria-label="2.11 绝对定位参数">
        <label class="wb-control">
          <span>定位方式</span>
          <select v-model="mode" data-testid="absolute-mode">
            <option value="static">static（对照）</option>
            <option value="absolute">absolute</option>
          </select>
        </label>
        <label class="wb-control">
          <span>偏移方向</span>
          <select v-model="axis" data-testid="absolute-axis" :disabled="mode === 'static'">
            <option value="top">top</option>
            <option value="right">right</option>
            <option value="bottom">bottom</option>
            <option value="left">left</option>
          </select>
        </label>
        <label class="wb-control">
          <span>偏移量 <b>{{ offset }}px</b></span>
          <input v-model.number="offset" data-testid="absolute-offset" type="range" min="0" max="80" step="2" :disabled="mode === 'static'" />
        </label>
        <label class="wb-control">
          <span>元素宽度 <b>{{ itemWidth }}px</b></span>
          <input v-model.number="itemWidth" data-testid="absolute-width" type="range" min="120" max="300" step="10" />
        </label>
        <label class="wb-control">
          <span>元素高度 <b>{{ itemHeight }}px</b></span>
          <input v-model.number="itemHeight" data-testid="absolute-height" type="range" min="36" max="90" step="2" />
        </label>
        <button class="wb-reset" type="button" data-testid="absolute-reset" @click="reset">重置</button>
      </div>

      <div class="wb-absolute-stage" data-testid="absolute-stage">
        <div
          v-if="mode === 'absolute'"
          class="wb-absolute-origin"
          data-testid="absolute-ghost"
          aria-hidden="true"
          :style="{ width: `${itemWidth}px`, height: `${itemHeight}px` }"
        ></div>
        <div class="wb-absolute-item" data-testid="absolute-item" :style="itemStyle">
          {{ mode === 'absolute' ? 'position: absolute' : 'position: static' }}
        </div>
        <div class="wb-absolute-follow" data-testid="absolute-after">
          后续元素：{{ mode === 'absolute' ? '已经向前填补，不再为绝对定位元素留空' : '排在 normal flow 元素之后' }}
        </div>
        <div class="wb-absolute-filler">后续内容 2</div>
      </div>

      <p class="wb-demo-note">
        演示容器的 <code>position: relative</code> 仅用于给 absolute 提供包含块；绝对定位元素本身不占正常流空间。
        虚线框是原位置的视觉标注，不参与布局；后续元素由浏览器真实前移。
      </p>
    </template>

    <template #conclusion>
      <p>
        <code>position: absolute</code> 使元素脱离正常文档流，原位置不再保留占位空间；后续元素会按脱流后的结果排列。
        它的定位参照不是“最近的父元素”本身，而是最近的、已建立包含块的祖先；本演示容器为 absolute 提供了相对定位的包含块。
      </p>
    </template>

    <template #code>
      <CodeBlock :code="code" />
    </template>
  </DemoSection>
</template>
