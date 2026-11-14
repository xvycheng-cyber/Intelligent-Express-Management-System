<script setup lang="ts">
import { computed, ref } from 'vue'
import type { CSSProperties } from 'vue'
import DemoSection from '@/components/basics/DemoSection.vue'
import CodeBlock from '@/components/basics/CodeBlock.vue'

type Mode = 'static' | 'relative' | 'absolute'
type Axis = 'top' | 'right' | 'bottom' | 'left'

const defaults = {
  mode: 'relative' as Mode,
  axis: 'top' as Axis,
  offset: 32,
  width: 200,
  height: 64
}

const mode = ref<Mode>(defaults.mode)
const axis = ref<Axis>(defaults.axis)
const offset = ref(defaults.offset)
const width = ref(defaults.width)
const height = ref(defaults.height)

const referenceText = computed(() => {
  if (mode.value === 'static') return 'static：不适用偏移，元素留在常规流'
  if (mode.value === 'absolute') return 'absolute：相对最近的定位祖先（舞台）'
  return 'relative：相对元素自身原位置'
})

const itemStyle = computed<CSSProperties>(() => {
  const base: CSSProperties = { width: `${width.value}px`, height: `${height.value}px` }
  if (mode.value === 'static') return { ...base, position: 'static' }
  const value = `${offset.value}px`
  const offsetStyle: CSSProperties = {}
  if (axis.value === 'top') offsetStyle.top = value
  if (axis.value === 'right') offsetStyle.right = value
  if (axis.value === 'bottom') offsetStyle.bottom = value
  if (axis.value === 'left') offsetStyle.left = value
  return { ...base, position: mode.value, ...offsetStyle }
})

const code = computed(
  () => `.wb-offset-stage {
  position: relative;              /* absolute 的定位参照 */
}

.wb-offset-item {
  position: ${mode.value};
  width: ${width.value}px;
  height: ${height.value}px;
${mode.value !== 'static' ? `  ${axis.value}: ${offset.value}px;\n` : ''}}

/* ${referenceText.value} */`
)

function reset() {
  mode.value = defaults.mode
  axis.value = defaults.axis
  offset.value = defaults.offset
  width.value = defaults.width
  height.value = defaults.height
}
</script>

<template>
  <DemoSection
    number="2.16"
    title="定位属性上下左右"
    target="切换 static / relative / absolute，调整 top/right/bottom/left，观察真实偏移和定位参照。"
  >
    <template #demo>
      <div class="wb-demo-controls" aria-label="2.16 定位偏移参数">
        <label class="wb-control">
          <span>定位方式</span>
          <select v-model="mode" data-testid="offset-mode">
            <option value="static">static</option>
            <option value="relative">relative</option>
            <option value="absolute">absolute</option>
          </select>
        </label>
        <label class="wb-control">
          <span>偏移属性</span>
          <select v-model="axis" data-testid="offset-axis" :disabled="mode === 'static'">
            <option value="top">top</option>
            <option value="right">right</option>
            <option value="bottom">bottom</option>
            <option value="left">left</option>
          </select>
        </label>
        <label class="wb-control">
          <span>偏移值 <b>{{ offset }}px</b></span>
          <input v-model.number="offset" data-testid="offset-value" type="range" min="0" max="80" step="2" :disabled="mode === 'static'" />
        </label>
        <label class="wb-control">
          <span>宽度 <b>{{ width }}px</b></span>
          <input v-model.number="width" data-testid="offset-width" type="range" min="120" max="300" step="10" />
        </label>
        <label class="wb-control">
          <span>高度 <b>{{ height }}px</b></span>
          <input v-model.number="height" data-testid="offset-height" type="range" min="36" max="100" step="2" />
        </label>
        <button class="wb-reset" type="button" data-testid="offset-reset" @click="reset">重置</button>
      </div>

      <div class="wb-offset-stage" data-testid="offset-stage">
        <div class="wb-offset-stage-label">定位参照舞台：position: relative</div>
        <div class="wb-offset-item" :style="itemStyle" data-testid="offset-item">
          position: {{ mode }}<br />
          <small v-if="mode !== 'static'">{{ axis }}: {{ offset }}px</small>
        </div>
        <div class="wb-offset-after" data-testid="offset-after">
          后续普通流元素：{{ mode === 'absolute' ? '会向前填补' : '相对定位不改变它的文档流位置' }}
        </div>
      </div>

      <p class="wb-demo-note">
        <strong>{{ referenceText }}</strong>。static 下偏移属性不产生定位效果；relative 保留原占位，
        absolute 脱离文档流。同一个偏移值在不同定位方式/参照下的视觉结果不同。
      </p>
    </template>

    <template #conclusion>
      <p>
        top/right/bottom/left 分别控制元素的上/右/下/左边缘偏移，但它们的“参照”由 position 决定：
        relative 相对自身原位置，absolute 相对最近的定位包含块，static 不使用这些偏移。
        同一个方向只设置一个偏移通常最清晰；多个方向同时设置会在 2.17 讨论。
      </p>
    </template>

    <template #code>
      <CodeBlock :code="code" />
    </template>
  </DemoSection>
</template>
