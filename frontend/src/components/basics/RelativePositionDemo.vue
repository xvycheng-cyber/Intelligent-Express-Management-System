<script setup lang="ts">
import { computed, ref } from 'vue'
import type { CSSProperties } from 'vue'
import DemoSection from '@/components/basics/DemoSection.vue'
import CodeBlock from '@/components/basics/CodeBlock.vue'

type OffsetAxis = 'top' | 'right' | 'bottom' | 'left'

const defaults = {
  axis: 'top' as OffsetAxis,
  offset: 28
}

const axis = ref<OffsetAxis>(defaults.axis)
const offset = ref(defaults.offset)

const relativeStyle = computed<CSSProperties>(() => {
  const value = `${offset.value}px`
  switch (axis.value) {
    case 'right':
      return { right: value }
    case 'bottom':
      return { bottom: value }
    case 'left':
      return { left: value }
    case 'top':
    default:
      return { top: value }
  }
})

const code = computed(
  () => `.wb-relative-static {
  position: static;
}

.wb-relative-box.is-relative {
  position: relative;
  ${axis.value}: ${offset.value}px;
}

/* 原位置仍由文档流保留；后续元素不会被重新排布 */`
)

function reset() {
  axis.value = defaults.axis
  offset.value = defaults.offset
}
</script>

<template>
  <DemoSection
    number="2.10"
    title="相对定位"
    target="对照 static 与 relative，观察四个方向的视觉偏移、原始位置占位和后续元素是否移动。"
  >
    <template #demo>
      <div class="wb-demo-controls" aria-label="2.10 相对定位参数">
        <label class="wb-control">
          <span>偏移方向</span>
          <select v-model="axis" data-testid="relative-axis">
            <option value="top">top</option>
            <option value="right">right</option>
            <option value="bottom">bottom</option>
            <option value="left">left</option>
          </select>
        </label>
        <label class="wb-control">
          <span>偏移量 <b>{{ offset }}px</b></span>
          <input v-model.number="offset" data-testid="relative-offset" type="range" min="0" max="60" step="2" />
        </label>
        <button class="wb-reset" type="button" data-testid="relative-reset" @click="reset">重置</button>
      </div>

      <div class="wb-relative-demo" data-testid="relative-demo">
        <div class="wb-relative-lane">
          <h4>static 对照</h4>
          <div class="wb-relative-box wb-relative-static" data-testid="relative-static">
            position: static
          </div>
          <div class="wb-relative-after" data-testid="relative-static-after">
            后续元素（static 页）
          </div>
        </div>

        <div class="wb-relative-lane">
          <h4>relative</h4>
          <div class="wb-relative-origin">
            <div class="wb-relative-ghost" data-testid="relative-ghost">原位置</div>
            <div
              class="wb-relative-box is-relative"
              data-testid="relative-box"
              :style="relativeStyle"
            >
              position: relative<br />
              <small>{{ axis }}: {{ offset }}px</small>
            </div>
          </div>
          <div class="wb-relative-after" data-testid="relative-after">
            后续元素（relative 页）
          </div>
        </div>
      </div>

      <p class="wb-demo-note">
        右侧虚线框是元素未偏移时的原始占位；relative 元素只做视觉偏移，原位置仍保留，后续元素继续按原位置布局。
        方向使用 <code>{{ axis }}: {{ offset }}px</code>；没有用 JavaScript 修改坐标，也没有 <code>position: absolute</code>。
      </p>
    </template>

    <template #conclusion>
      <p>
        <code>position: relative</code> 的 <code>top/right/bottom/left</code> 让元素相对自身原位置视觉偏移，
        但它仍属于正常文档流，原始空间保留，后续元素不会因为视觉偏移而重新排布。static 元素不接受这些偏移属性。
      </p>
    </template>

    <template #code>
      <CodeBlock :code="code" />
    </template>
  </DemoSection>
</template>
