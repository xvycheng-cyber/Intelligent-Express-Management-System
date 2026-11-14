<script setup lang="ts">
import { computed, ref } from 'vue'
import DemoSection from '@/components/basics/DemoSection.vue'
import CodeBlock from '@/components/basics/CodeBlock.vue'

const defaults = {
  count: 4,
  width: 120,
  height: 60,
  containerWidth: 620,
  marginRight: 0,
  showWhitespace: true,
  displayMode: 'inline-block' as 'inline' | 'inline-block'
}

const count = ref(defaults.count)
const width = ref(defaults.width)
const height = ref(defaults.height)
const containerWidth = ref(defaults.containerWidth)
const marginRight = ref(defaults.marginRight)
const showWhitespace = ref(defaults.showWhitespace)
const displayMode = ref<'inline' | 'inline-block'>(defaults.displayMode)

const itemStyle = computed(() => ({
  display: displayMode.value,
  width: `${width.value}px`,
  height: `${height.value}px`,
  marginRight: `${marginRight.value}px`
}))

const code = computed(
  () => `.wb-inline-block-demo__item {
  display: ${displayMode.value};
  width: ${width.value}px;
  height: ${height.value}px;
  margin-right: ${marginRight.value}px;
}

/* 元素之间保留一个真实空白字符（不是 flex gap） */
.wb-inline-block-demo__space { white-space: pre; }`
)

function reset() {
  count.value = defaults.count
  width.value = defaults.width
  height.value = defaults.height
  containerWidth.value = defaults.containerWidth
  marginRight.value = defaults.marginRight
  showWhitespace.value = defaults.showWhitespace
  displayMode.value = defaults.displayMode
}
</script>

<template>
  <DemoSection
    number="2.3"
    title="行内区块显示类型"
    target="观察 inline-block 的宽高生效、元素之间的空白间隙和原生换行。"
  >
    <template #demo>
      <div class="wb-demo-controls" aria-label="2.3 行内区块显示类型参数">
        <label class="wb-control">
          <span>元素数 <b>{{ count }}</b></span>
          <input v-model.number="count" data-testid="inline-block-count" type="range" min="2" max="8" step="1" />
        </label>
        <label class="wb-control">
          <span>元素宽度 <b>{{ width }}px</b></span>
          <input v-model.number="width" data-testid="inline-block-width" type="range" min="70" max="180" step="5" />
        </label>
        <label class="wb-control">
          <span>元素高度 <b>{{ height }}px</b></span>
          <input v-model.number="height" data-testid="inline-block-height" type="range" min="36" max="100" step="4" />
        </label>
        <label class="wb-control">
          <span>容器宽度 <b>{{ containerWidth }}px</b></span>
          <input v-model.number="containerWidth" data-testid="inline-block-container" type="range" min="220" max="720" step="10" />
        </label>
        <label class="wb-control">
          <span>右外边距 <b>{{ marginRight }}px</b></span>
          <input v-model.number="marginRight" data-testid="inline-block-margin" type="range" min="0" max="20" step="1" />
        </label>
        <label class="wb-control">
          <span>display</span>
          <select v-model="displayMode" data-testid="inline-block-mode">
            <option value="inline-block">inline-block</option>
            <option value="inline">inline（对照）</option>
          </select>
        </label>
        <label class="wb-control wb-control--check">
          <input v-model="showWhitespace" data-testid="inline-block-whitespace" type="checkbox" />
          <span>保留元素间空白字符</span>
        </label>
        <button class="wb-reset" type="button" data-testid="inline-block-reset" @click="reset">重置</button>
      </div>

      <div class="wb-demo-stage wb-inline-block-demo">
        <div class="wb-inline-block-viewport">
          <div
            class="wb-inline-block-stage"
            data-testid="inline-block-stage"
            :style="{ width: `${containerWidth}px` }"
          >
            <template v-for="index in count" :key="index">
              <span class="wb-inline-block-demo__item" data-testid="inline-block-item" :style="itemStyle">
                项目 {{ index }}
              </span>
              <span
                v-if="showWhitespace && index < count"
                class="wb-inline-block-demo__space"
                data-testid="inline-block-space"
                aria-hidden="true"
              >{{ ' ' }}</span>
            </template>
          </div>
        </div>
        <p class="wb-demo-note">
          默认能看到元素之间的空白间隙，它不是 <code>gap</code>，而是 HTML 中两个行内元素之间的空白文本节点。
          把容器调窄，或增加元素数量，浏览器会让 inline-block 自然地换行；这里没有使用 flex 或 JavaScript 计算位置。
          窄屏下演示容器可横向滚动，页面本身不会横向溢出。
        </p>
      </div>
    </template>

    <template #conclusion>
      <p>
        <code>display: inline-block</code> 让元素像行内内容一样参与同一行排版，但它的
        <code>width</code>/<code>height</code> 会生效，也能设置上下 padding/border。元素之间的空白字符会占据宽度，
        容器不足时浏览器按行内排版规则自然换行。它与 <code>inline</code> 的关键差异是宽高生效，与 <code>flex</code>
        的差异是它仍属于行内格式化上下文，不是弹性布局容器。
      </p>
    </template>

    <template #code>
      <CodeBlock :code="code" />
    </template>
  </DemoSection>
</template>
