<script setup lang="ts">
import { computed, ref } from 'vue'
import DemoSection from '@/components/basics/DemoSection.vue'
import CodeBlock from '@/components/basics/CodeBlock.vue'

const defaults = {
  blockCount: 3,
  widthMode: 'auto' as 'auto' | 'fixed',
  width: 260,
  blockHeight: 52,
  blockGap: 12,
  inlineCount: 3,
  offset: 0
}

const blockCount = ref(defaults.blockCount)
const widthMode = ref<'auto' | 'fixed'>(defaults.widthMode)
const width = ref(defaults.width)
const blockHeight = ref(defaults.blockHeight)
const blockGap = ref(defaults.blockGap)
const inlineCount = ref(defaults.inlineCount)
const offset = ref(defaults.offset)

const blockStyle = computed(() => ({
  position: 'static' as const,
  width: widthMode.value === 'auto' ? 'auto' : `${width.value}px`,
  height: `${blockHeight.value}px`,
  marginBottom: `${blockGap.value}px`,
  top: `${offset.value}px`,
  left: `${offset.value}px`
}))

const code = computed(
  () => `.wb-normal-block {
  position: static;               /* top/left 无效 */
  display: block;
  width: ${widthMode.value === 'auto' ? 'auto' : `${width.value}px`};
  height: ${blockHeight.value}px;
  margin-bottom: ${blockGap.value}px;
  top: ${offset.value}px;          /* static 下无效 */
  left: ${offset.value}px;
}

.wb-normal-inline {
  display: inline;                 /* 行内文字流 */
}`
)

function reset() {
  blockCount.value = defaults.blockCount
  widthMode.value = defaults.widthMode
  width.value = defaults.width
  blockHeight.value = defaults.blockHeight
  blockGap.value = defaults.blockGap
  inlineCount.value = defaults.inlineCount
  offset.value = defaults.offset
}
</script>

<template>
  <DemoSection
    number="2.9"
    title="常规流布局（静态定位）"
    target="观察 block 的纵向堆叠、inline 的文字行排列、后续元素受前序尺寸影响，以及 static 下 top/left 无效。"
  >
    <template #demo>
      <div class="wb-demo-controls" aria-label="2.9 常规流参数">
        <label class="wb-control">
          <span>区块数 <b>{{ blockCount }}</b></span>
          <input v-model.number="blockCount" data-testid="normal-block-count" type="range" min="1" max="4" step="1" />
        </label>
        <label class="wb-control">
          <span>区块宽度</span>
          <select v-model="widthMode" data-testid="normal-width-mode">
            <option value="auto">auto（填满）</option>
            <option value="fixed">固定宽度</option>
          </select>
        </label>
        <label class="wb-control">
          <span>固定宽度 <b>{{ width }}px</b></span>
          <input v-model.number="width" data-testid="normal-width" type="range" min="120" max="360" step="10" :disabled="widthMode === 'auto'" />
        </label>
        <label class="wb-control">
          <span>区块高度 <b>{{ blockHeight }}px</b></span>
          <input v-model.number="blockHeight" data-testid="normal-height" type="range" min="30" max="90" step="2" />
        </label>
        <label class="wb-control">
          <span>区块间距 <b>{{ blockGap }}px</b></span>
          <input v-model.number="blockGap" data-testid="normal-gap" type="range" min="0" max="24" step="1" />
        </label>
        <label class="wb-control">
          <span>行内元素数 <b>{{ inlineCount }}</b></span>
          <input v-model.number="inlineCount" data-testid="normal-inline-count" type="range" min="1" max="5" step="1" />
        </label>
        <label class="wb-control">
          <span>尝试 top/left <b>{{ offset }}px</b></span>
          <input v-model.number="offset" data-testid="normal-offset" type="range" min="0" max="40" step="2" />
        </label>
        <button class="wb-reset" type="button" data-testid="normal-reset" @click="reset">重置</button>
      </div>

      <div class="wb-normal-stage" data-testid="normal-stage">
        <div
          v-for="index in blockCount"
          :key="index"
          class="wb-normal-block"
          data-testid="normal-block"
          :style="blockStyle"
        >
          常规流区块 {{ index }}：display:block / position:static
        </div>

        <p class="wb-normal-paragraph">
          行内文字流：
          <template v-for="index in inlineCount" :key="index">
            <span class="wb-normal-inline" data-testid="normal-inline">行内 {{ index }}</span>
            <span v-if="index < inlineCount">、</span>
          </template>
          继续跟随文字排列。
        </p>

        <div class="wb-normal-after" data-testid="normal-after">
          后续块：它只能接在前面的常规流内容之后；前面的高度/间距变化会推移它。
        </div>

        <p class="wb-demo-note">
          这里没有使用 absolute、fixed、transform 或 JavaScript 计算坐标。block 在常规流中纵向堆叠，
          inline 在文字行中横向排列；static 元素即使设置 <code>top/left</code> 也不会移动，
          后面的内容仍按前面的真实占位依次排布。
        </p>
      </div>
    </template>

    <template #conclusion>
      <p>
        常规流（normal flow）中，block 元素从上到下排列，inline 元素在文字行内排列，元素正常占据文档流空间。
        元素尺寸和间距变化会实际影响后续元素的位置。`position: static` 是默认值，`top/right/bottom/left`
        对 static 元素不产生定位效果；这与 absolute/fixed 脱离文档流是不同问题。
      </p>
    </template>

    <template #code>
      <CodeBlock :code="code" />
    </template>
  </DemoSection>
</template>
