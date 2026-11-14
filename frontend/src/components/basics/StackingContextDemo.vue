<script setup lang="ts">
import { computed, ref } from 'vue'
import type { CSSProperties } from 'vue'
import DemoSection from '@/components/basics/DemoSection.vue'
import CodeBlock from '@/components/basics/CodeBlock.vue'

const defaults = {
  contextOn: true,
  childZ: 999,
  bZ: 2
}

const contextOn = ref(defaults.contextOn)
const childZ = ref(defaults.childZ)
const bZ = ref(defaults.bZ)

const contextStyle = computed<CSSProperties>(() => ({
  position: 'relative',
  zIndex: contextOn.value ? 1 : 'auto'
}))

const aStyle = computed<CSSProperties>(() => ({
  position: 'absolute',
  top: '20px',
  left: '20px',
  zIndex: childZ.value
}))

const bStyle = computed<CSSProperties>(() => ({
  position: 'absolute',
  top: '60px',
  left: '100px',
  zIndex: bZ.value
}))

const code = computed(
  () => `.wb-stack-context {
  position: relative;
  z-index: ${contextOn.value ? 1 : 'auto'};  /* ${contextOn.value ? '建立堆叠上下文' : '不建立'} */
}

.wb-stack-a {
  position: absolute;
  z-index: ${childZ.value};             /* 子元素，即使很大也被父上下文限制 */
}

.wb-stack-b {
  position: absolute;
  z-index: ${bZ.value};                 /* 外部元素 */
}`
)

function reset() {
  contextOn.value = defaults.contextOn
  childZ.value = defaults.childZ
  bZ.value = defaults.bZ
}
</script>

<template>
  <DemoSection
    number="2.17+"
    title="补充：元素覆盖与 z-index"
    target="对照同一堆叠上下文与不同堆叠上下文，验证 z-index 和父级堆叠上下文的隔离效果。"
  >
    <template #demo>
      <div class="wb-demo-controls" aria-label="2.17 补充堆叠上下文参数">
        <label class="wb-control wb-control--check">
          <input v-model="contextOn" data-testid="stack-context-toggle" type="checkbox" />
          <span>父级建立堆叠上下文（z-index: 1）</span>
        </label>
        <label class="wb-control">
          <span>A 子元素 z-index <b>{{ childZ }}</b></span>
          <input v-model.number="childZ" data-testid="stack-child-z" type="range" min="0" max="999" step="1" />
        </label>
        <label class="wb-control">
          <span>B 外部元素 z-index <b>{{ bZ }}</b></span>
          <input v-model.number="bZ" data-testid="stack-b-z" type="range" min="0" max="10" step="1" />
        </label>
        <button class="wb-reset" type="button" data-testid="stack-reset" @click="reset">重置</button>
      </div>

      <div class="wb-stack-stage" data-testid="stack-stage">
        <div class="wb-stack-context" :style="contextStyle" data-testid="stack-context">
          <span class="wb-stack-context-label">父级上下文 z-index: {{ contextOn ? 1 : 'auto' }}</span>
          <div class="wb-stack-card wb-stack-a" :style="aStyle" data-testid="stack-a">
            A 子元素<br />z-index: {{ childZ }}
          </div>
        </div>
        <div class="wb-stack-card wb-stack-b" :style="bStyle" data-testid="stack-b">
          B 外部元素<br />z-index: {{ bZ }}
        </div>
      </div>

      <p class="wb-demo-note">
        当父级 z-index:1 建立堆叠上下文时，A 的子 z-index:999 只在父上下文内部有效，不能越过父上下文去压过
        外部 z-index:2 的 B；关闭父上下文后，A 与 B 进入同一层级比较，A 的 999 才会压过 B。
        z-index 只对有定位/参与堆叠的元素有意义，且受堆叠上下文隔离。
      </p>
    </template>

    <template #conclusion>
      <p>
        z-index 不是“数值大就一定在最上面”的全局规则。父元素一旦建立堆叠上下文，子元素的 z-index
        只能在内部比较，不能直接跨越到外部上下文；外部上下文的层级优先。改变 DOM 顺序不是 z-index 演示，
        本实验只改变真实 z-index 和堆叠上下文。
      </p>
    </template>

    <template #code>
      <CodeBlock :code="code" />
    </template>
  </DemoSection>
</template>
