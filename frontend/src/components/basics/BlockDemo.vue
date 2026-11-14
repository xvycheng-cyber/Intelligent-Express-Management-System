<script setup lang="ts">
import { computed, ref } from 'vue'
import DemoSection from '@/components/basics/DemoSection.vue'
import CodeBlock from '@/components/basics/CodeBlock.vue'

const defaults = {
  count: 3,
  widthMode: 'auto' as 'auto' | 'fixed',
  width: 240,
  height: 56,
  gap: 12,
  background: '#dbeafe',
  centered: false
}

const count = ref(defaults.count)
const widthMode = ref<'auto' | 'fixed'>(defaults.widthMode)
const width = ref(defaults.width)
const height = ref(defaults.height)
const gap = ref(defaults.gap)
const background = ref(defaults.background)
const centered = ref(defaults.centered)

const blockStyle = computed(() => ({
  width: widthMode.value === 'auto' ? 'auto' : `${width.value}px`,
  height: `${height.value}px`,
  marginBottom: `${gap.value}px`,
  marginLeft: widthMode.value === 'fixed' && centered.value ? 'auto' : '0',
  marginRight: widthMode.value === 'fixed' && centered.value ? 'auto' : '0',
  background: background.value
}))

const code = computed(
  () => `.wb-block-demo__item {
  display: block;                  /* 独占一行，默认填满可用宽度 */
  width: ${widthMode.value === 'auto' ? 'auto' : `${width.value}px`};
  height: ${height.value}px;
  margin-bottom: ${gap.value}px;
  ${widthMode.value === 'fixed' && centered.value ? 'margin-left: auto;\n  margin-right: auto;\n  ' : ''}background: ${background.value};
}`
)

function reset() {
  count.value = defaults.count
  widthMode.value = defaults.widthMode
  width.value = defaults.width
  height.value = defaults.height
  gap.value = defaults.gap
  background.value = defaults.background
  centered.value = defaults.centered
}
</script>

<template>
  <DemoSection
    number="2.2"
    title="区块显示类型"
    target="观察 block 元素独占一行、默认填充宽度，以及宽度和间距的控制方式。"
  >
    <template #demo>
      <div class="wb-demo-controls" aria-label="2.2 区块显示类型参数">
        <label class="wb-control">
          <span>区块数 <b>{{ count }}</b></span>
          <input v-model.number="count" data-testid="block-count" type="range" min="1" max="4" step="1" />
        </label>
        <label class="wb-control">
          <span>宽度模式</span>
          <select v-model="widthMode" data-testid="block-width-mode">
            <option value="auto">auto（填充可用宽度）</option>
            <option value="fixed">固定宽度</option>
          </select>
        </label>
        <label class="wb-control">
          <span>固定宽度 <b>{{ width }}px</b></span>
          <input v-model.number="width" data-testid="block-width" type="range" min="100" max="420" step="10" :disabled="widthMode === 'auto'" />
        </label>
        <label class="wb-control">
          <span>高度 <b>{{ height }}px</b></span>
          <input v-model.number="height" data-testid="block-height" type="range" min="28" max="100" step="4" />
        </label>
        <label class="wb-control">
          <span>上下间距 <b>{{ gap }}px</b></span>
          <input v-model.number="gap" data-testid="block-gap" type="range" min="0" max="24" step="1" />
        </label>
        <label class="wb-control wb-control--check">
          <input v-model="centered" data-testid="block-centered" type="checkbox" :disabled="widthMode === 'auto'" />
          <span>固定宽度时水平居中（margin: auto）</span>
        </label>
        <label class="wb-control wb-control--color">
          <span>背景色</span>
          <input v-model="background" data-testid="block-background" type="color" />
        </label>
        <button class="wb-reset" type="button" data-testid="block-reset" @click="reset">重置</button>
      </div>

      <div class="wb-demo-stage wb-block-demo" data-testid="block-stage">
        <div
          v-for="index in count"
          :key="index"
          class="wb-block-demo__item"
          data-testid="block-item"
          :style="blockStyle"
        >
          区块 {{ index }}：display: block
        </div>
        <p class="wb-demo-note">
          块级元素从新行开始，<code>width: auto</code> 时会占用包含块的可用宽度；固定宽度后可用
          <code>margin-left/right: auto</code> 水平居中。相邻块的上下 margin 在常规流中可能发生折叠。
        </p>
      </div>
    </template>

    <template #conclusion>
      <p>
        <code>display: block</code> 的元素独占一行，默认宽度撑满包含块。它可以设置宽高、外边距和内边距；
        多个块在常规流中从上到下排列。固定宽度时，左右 <code>margin: auto</code> 可以水平居中。
      </p>
    </template>

    <template #code>
      <CodeBlock :code="code" />
    </template>
  </DemoSection>
</template>
