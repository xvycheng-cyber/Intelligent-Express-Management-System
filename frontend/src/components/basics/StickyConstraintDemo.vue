<script setup lang="ts">
import { computed, ref } from 'vue'
import DemoSection from '@/components/basics/DemoSection.vue'
import CodeBlock from '@/components/basics/CodeBlock.vue'

const defaults = {
  parentHeight: 300,
  threshold: 16
}

const parentHeight = ref(defaults.parentHeight)
const threshold = ref(defaults.threshold)
const frameRef = ref<HTMLElement | null>(null)

const stickyStyle = computed(() => ({
  position: 'sticky' as const,
  top: `${threshold.value}px`
}))

const code = computed(
  () => `.wb-constraint-parent {
  height: ${parentHeight.value}px;    /* sticky 的约束矩形 */
}

.wb-constraint-sticky {
  position: sticky;
  top: ${threshold.value}px;
}

/* 父容器底部会把 sticky 推走 */
/* sticky 不会越过父容器的边界继续吸附 */`
)

function scrollDemo() {
  if (frameRef.value) frameRef.value.scrollTop = 220
}

function scrollToTop() {
  if (frameRef.value) frameRef.value.scrollTop = 0
}

function reset() {
  parentHeight.value = defaults.parentHeight
  threshold.value = defaults.threshold
  if (frameRef.value) frameRef.value.scrollTop = 0
}
</script>

<template>
  <DemoSection
    number="2.15"
    title="粘性约束矩形"
    target="观察 sticky 在父容器（约束矩形）范围内吸附；父容器高度不足时会被底部推走。"
  >
    <template #demo>
      <div class="wb-demo-controls" aria-label="2.15 粘性约束矩形参数">
        <label class="wb-control">
          <span>父容器高度 <b>{{ parentHeight }}px</b></span>
          <input v-model.number="parentHeight" data-testid="constraint-parent-height" type="range" min="180" max="600" step="20" />
        </label>
        <label class="wb-control">
          <span>top 阈值 <b>{{ threshold }}px</b></span>
          <input v-model.number="threshold" data-testid="constraint-threshold" type="range" min="0" max="50" step="2" />
        </label>
        <button class="wb-reset" type="button" data-testid="constraint-scroll" @click="scrollDemo">滚动到边界</button>
        <button class="wb-reset" type="button" data-testid="constraint-top" @click="scrollToTop">回到顶部</button>
        <button class="wb-reset" type="button" data-testid="constraint-reset" @click="reset">重置</button>
      </div>

      <div ref="frameRef" class="wb-constraint-frame" data-testid="constraint-frame">
        <div
          class="wb-constraint-parent"
          data-testid="constraint-parent"
          :style="{ height: `${parentHeight}px` }"
        >
          <div class="wb-constraint-label">sticky 的父容器 = 约束矩形 · height: {{ parentHeight }}px</div>
          <div class="wb-constraint-sticky" :style="stickyStyle" data-testid="constraint-sticky">
            sticky<br />top: {{ threshold }}px
          </div>
          <div class="wb-constraint-lines">
            <span v-for="index in 7" :key="index">
              约束矩形内部内容 {{ index }}：sticky 只能在本父容器内吸附，父容器底部会把它推走。
            </span>
          </div>
        </div>
        <div class="wb-constraint-after">父容器之后的普通内容</div>
      </div>

      <p class="wb-demo-note">
        sticky 的吸附范围受最近父容器（约束矩形）限制：父容器足够高时可在滚动区顶部吸附，
        父容器太短时会被底部推走。这里用真实 sticky + 真实滚动容器，没有用 fixed 或 JS 模拟。
      </p>
    </template>

    <template #conclusion>
      <p>
        sticky 元素只在父容器的约束矩形内吸附，不能越过父容器边界。父容器高度、滚动距离和
        <code>top</code> 阈值共同决定可观察到的粘附/被推走现象。
      </p>
    </template>

    <template #code>
      <CodeBlock :code="code" />
    </template>
  </DemoSection>
</template>
