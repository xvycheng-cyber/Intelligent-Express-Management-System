<script setup lang="ts">
import { computed, ref } from 'vue'
import DemoSection from '@/components/basics/DemoSection.vue'
import CodeBlock from '@/components/basics/CodeBlock.vue'

const defaults = {
  threshold: 24,
  frameHeight: 240
}

const threshold = ref(defaults.threshold)
const frameHeight = ref(defaults.frameHeight)
const frameRef = ref<HTMLElement | null>(null)

const stickyStyle = computed(() => ({
  position: 'sticky' as const,
  top: `${threshold.value}px`
}))

const code = computed(
  () => `.wb-sticky-frame {
  height: ${frameHeight.value}px;
  overflow: auto;                  /* 真实滚动容器 */
}

.wb-sticky-item {
  position: sticky;
  top: ${threshold.value}px;       /* 到达阈值后吸附 */
}`
)

function scrollPastThreshold() {
  if (frameRef.value) frameRef.value.scrollTop = 180
}

function scrollToTop() {
  if (frameRef.value) frameRef.value.scrollTop = 0
}

function reset() {
  threshold.value = defaults.threshold
  frameHeight.value = defaults.frameHeight
  if (frameRef.value) frameRef.value.scrollTop = 0
}
</script>

<template>
  <DemoSection
    number="2.14"
    title="粘性定位"
    target="观察 sticky 在阈值前随内容滚动、到达 top 阈值后吸附，以及真实滚动容器中的行为。"
  >
    <template #demo>
      <div class="wb-demo-controls" aria-label="2.14 粘性定位参数">
        <label class="wb-control">
          <span>top 阈值 <b>{{ threshold }}px</b></span>
          <input v-model.number="threshold" data-testid="sticky-threshold" type="range" min="0" max="80" step="2" />
        </label>
        <label class="wb-control">
          <span>滚动区高度 <b>{{ frameHeight }}px</b></span>
          <input v-model.number="frameHeight" data-testid="sticky-frame-height" type="range" min="180" max="300" step="10" />
        </label>
        <button class="wb-reset" type="button" data-testid="sticky-scroll" @click="scrollPastThreshold">滚动到阈值后</button>
        <button class="wb-reset" type="button" data-testid="sticky-top" @click="scrollToTop">回到顶部</button>
        <button class="wb-reset" type="button" data-testid="sticky-reset" @click="reset">重置</button>
      </div>

      <div
        ref="frameRef"
        class="wb-sticky-frame"
        data-testid="sticky-frame"
        :style="{ height: `${frameHeight}px` }"
      >
        <div class="wb-sticky-content">
          <div class="wb-sticky-head">滚动容器顶部内容</div>
          <div class="wb-sticky-item" :style="stickyStyle" data-testid="sticky-item">
            sticky · top: {{ threshold }}px
          </div>
          <p v-for="index in 8" :key="index" class="wb-sticky-line">
            第 {{ index }} 行滚动内容：sticky 元素在到达阈值前保持普通文档流位置，达到阈值后吸附在滚动容器顶部。
          </p>
        </div>
      </div>

      <p class="wb-demo-note">
        这里使用的是真实 <code>position: sticky</code> 和真实滚动容器，不是 fixed 或 JS 监听滚动。
        sticky 需要滚动容器有足够内容；它的吸附范围还受父容器边界限制（见 2.15）。
      </p>
    </template>

    <template #conclusion>
      <p>
        <code>position: sticky</code> 在滚动到 <code>top</code> 等阈值前表现为普通流元素，到达阈值后吸附。
        它不脱离正常文档流，吸附发生在最近的滚动容器内；容器内容不足时无法观察到粘附。
      </p>
    </template>

    <template #code>
      <CodeBlock :code="code" />
    </template>
  </DemoSection>
</template>
