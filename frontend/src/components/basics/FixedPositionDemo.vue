<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import type { CSSProperties } from 'vue'
import DemoSection from '@/components/basics/DemoSection.vue'
import CodeBlock from '@/components/basics/CodeBlock.vue'

type Axis = 'top' | 'right' | 'bottom' | 'left'

const defaults = {
  showFixed: true,
  axis: 'bottom' as Axis,
  offset: 18
}

const showFixed = ref(defaults.showFixed)
const axis = ref<Axis>(defaults.axis)
const offset = ref(defaults.offset)
const scrollRef = ref<HTMLElement | null>(null)
const sectionRef = ref<HTMLElement | null>(null)
const inView = ref(false)
let observer: IntersectionObserver | null = null

const fixedVisible = computed(() => showFixed.value && inView.value)

const fixedStyle = computed<CSSProperties>(() => {
  const value = `${offset.value}px`
  switch (axis.value) {
    case 'top':
      return { position: 'fixed', top: value, right: value }
    case 'right':
      return { position: 'fixed', right: value, bottom: value }
    case 'left':
      return { position: 'fixed', left: value, bottom: value }
    case 'bottom':
    default:
      return { position: 'fixed', right: value, bottom: value }
  }
})

onMounted(() => {
  if (!sectionRef.value) return
  observer = new IntersectionObserver(
    (entries) => {
      inView.value = entries[0]?.isIntersecting ?? false
    },
    { threshold: 0.12 }
  )
  observer.observe(sectionRef.value)
})

onBeforeUnmount(() => {
  observer?.disconnect()
  observer = null
})

const code = computed(
  () => `.wb-fixed-probe {
  position: fixed;                 /* 相对视口 */
${axis.value === 'top' ? `  top: ${offset.value}px;\n  right: ${offset.value}px;\n` : ''}${axis.value === 'right' ? `  right: ${offset.value}px;\n  bottom: ${offset.value}px;\n` : ''}${axis.value === 'bottom' ? `  right: ${offset.value}px;\n  bottom: ${offset.value}px;\n` : ''}${axis.value === 'left' ? `  left: ${offset.value}px;\n  bottom: ${offset.value}px;\n` : ''}  width: 142px;
  height: 34px;
  pointer-events: none;
}

.wb-fixed-absolute {
  position: absolute;              /* 相对滚动内容 */
  top: 0;
  right: 0;
}`
)

function reset() {
  showFixed.value = defaults.showFixed
  axis.value = defaults.axis
  offset.value = defaults.offset
  if (scrollRef.value) scrollRef.value.scrollTop = 0
}

function scrollDemo() {
  if (scrollRef.value) scrollRef.value.scrollTop = 150
}
</script>

<template>
  <div ref="sectionRef" class="wb-fixed-demo">
    <DemoSection
      number="2.13"
      title="固定定位"
      target="观察 fixed 元素在页面滚动时仍相对视口不动，并与滚动容器内的 absolute 元素作对比。"
    >
    <template #demo>
      <div class="wb-demo-controls" aria-label="2.13 固定定位参数">
        <label class="wb-control wb-control--check">
          <input v-model="showFixed" data-testid="fixed-show" type="checkbox" />
          <span>显示 fixed 演示徽标（不拦截点击）</span>
        </label>
        <label class="wb-control">
          <span>偏移方向</span>
          <select v-model="axis" data-testid="fixed-axis">
            <option value="top">top</option>
            <option value="right">right</option>
            <option value="bottom">bottom</option>
            <option value="left">left</option>
          </select>
        </label>
        <label class="wb-control">
          <span>偏移量 <b>{{ offset }}px</b></span>
          <input v-model.number="offset" data-testid="fixed-offset" type="range" min="8" max="48" step="2" />
        </label>
        <button class="wb-reset" type="button" data-testid="fixed-scroll" @click="scrollDemo">滚动演示区</button>
        <button class="wb-reset" type="button" data-testid="fixed-reset" @click="reset">重置</button>
      </div>

      <div class="wb-fixed-scroll" ref="scrollRef" data-testid="fixed-scroll-frame">
        <div class="wb-fixed-scroll-head">滚动容器：滚动它，观察 absolute 对比元素</div>
        <div class="wb-fixed-absolute" data-testid="fixed-absolute">absolute 对比项</div>
        <ol class="wb-fixed-list">
          <li v-for="index in 8" :key="index">滚动内容 {{ index }}：position: absolute 的对比项会随容器内容滚动。</li>
        </ol>
      </div>

      <div
        v-if="fixedVisible"
        class="wb-fixed-probe"
        :style="fixedStyle"
        data-testid="fixed-probe"
        aria-label="fixed 演示徽标"
      >
        fixed · 相对视口
      </div>

      <p class="wb-demo-note">
        页面滚动时，fixed 徽标保持相对视口的位置；滚动容器内的 absolute 对比项会随内容一起滚动。
        fixed 默认相对视口，但如果祖先有 <code>transform</code> 等属性，包含块可能改变；本演示没有添加 transform。
      </p>
    </template>

    <template #conclusion>
      <p>
        <code>position: fixed</code> 脱离正常文档流，默认相对视口定位，页面滚动时视觉位置不变；
        absolute 则相对其定位包含块，页面/容器滚动时会随包含块移动。fixed 同样不保留原文档流占位。
      </p>
    </template>

    <template #code>
      <CodeBlock :code="code" />
    </template>
  </DemoSection>
  </div>
</template>
