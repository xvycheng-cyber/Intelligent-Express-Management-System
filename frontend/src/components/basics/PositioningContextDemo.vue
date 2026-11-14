<script setup lang="ts">
import { computed, ref } from 'vue'
import type { CSSProperties } from 'vue'
import DemoSection from '@/components/basics/DemoSection.vue'
import CodeBlock from '@/components/basics/CodeBlock.vue'

type ParentPosition = 'static' | 'relative'

const defaults = {
  parentPosition: 'static' as ParentPosition,
  top: 14,
  left: 14
}

const parentPosition = ref<ParentPosition>(defaults.parentPosition)
const top = ref(defaults.top)
const left = ref(defaults.left)

const parentStyle = computed<CSSProperties>(() => ({
  position: parentPosition.value
}))

const childStyle = computed<CSSProperties>(() => ({
  position: 'absolute',
  top: `${top.value}px`,
  left: `${left.value}px`
}))

const code = computed(
  () => `.wb-context-outer {
  position: relative;              /* 外层定位祖先 */
}

.wb-context-parent {
  position: ${parentPosition.value}; /* 切换点 */
}

.wb-context-child {
  position: absolute;
  top: ${top.value}px;
  left: ${left.value}px;
}`
)

function reset() {
  parentPosition.value = defaults.parentPosition
  top.value = defaults.top
  left.value = defaults.left
}
</script>

<template>
  <DemoSection
    number="2.12"
    title="定位上下文"
    target="切换父级 static/relative，观察 absolute 子元素以哪个祖先作为包含块；不要把“最近父元素”误当成“定位祖先”。"
  >
    <template #demo>
      <div class="wb-demo-controls" aria-label="2.12 定位上下文参数">
        <label class="wb-control">
          <span>父元素 position</span>
          <select v-model="parentPosition" data-testid="context-parent-position">
            <option value="static">static</option>
            <option value="relative">relative</option>
          </select>
        </label>
        <label class="wb-control">
          <span>child top <b>{{ top }}px</b></span>
          <input v-model.number="top" data-testid="context-top" type="range" min="0" max="60" step="2" />
        </label>
        <label class="wb-control">
          <span>child left <b>{{ left }}px</b></span>
          <input v-model.number="left" data-testid="context-left" type="range" min="0" max="80" step="2" />
        </label>
        <button class="wb-reset" type="button" data-testid="context-reset" @click="reset">重置</button>
      </div>

      <div class="wb-context-outer" data-testid="context-outer">
        <div class="wb-context-outer-label">outer：position: relative（外层包含块）</div>
        <div
          class="wb-context-parent"
          :class="`is-${parentPosition}`"
          :style="parentStyle"
          data-testid="context-parent"
        >
          <span class="wb-context-parent-label">parent：position: {{ parentPosition }}</span>
          <div class="wb-context-sibling">普通流兄弟元素</div>
          <div class="wb-context-child" :style="childStyle" data-testid="context-child">
            absolute child<br />
            top: {{ top }} / left: {{ left }}
          </div>
        </div>
      </div>

      <p class="wb-demo-note">
        absolute 子元素寻找最近的、能够建立包含块的祖先。父元素 static 时它不是定位祖先，子元素会以
        outer 的 relative 为参照；把父元素切为 relative 后，子元素改为以 parent 为参照，位置立即变化。
        这演示了“最近的父元素”不等于“最近的定位包含块”。
      </p>
    </template>

    <template #conclusion>
      <p>
        absolute 元素的定位参照是最近的定位祖先（或其它建立包含块的祖先）；没有定位祖先时才使用初始包含块。
        static 父元素不会仅仅因为“是父元素”就成为 absolute 的包含块。本演示有两层容器：outer 和 parent，
        切换 parent 的 position 可以清楚看到参照变化。
      </p>
    </template>

    <template #code>
      <CodeBlock :code="code" />
    </template>
  </DemoSection>
</template>
