<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import DemoSection from '@/components/basics/DemoSection.vue'
import CodeBlock from '@/components/basics/CodeBlock.vue'

const defaults = {
  contentWidth: 240,
  contentHeight: 120,
  padding: 18,
  borderWidth: 6,
  margin: 18
}

const contentWidth = ref(defaults.contentWidth)
const contentHeight = ref(defaults.contentHeight)
const padding = ref(defaults.padding)
const borderWidth = ref(defaults.borderWidth)
const margin = ref(defaults.margin)
const targetRef = ref<HTMLElement | null>(null)
const actual = ref({ borderBoxWidth: 0, borderBoxHeight: 0 })

const targetStyle = computed(() => ({
  boxSizing: 'content-box' as const,
  width: `${contentWidth.value}px`,
  height: `${contentHeight.value}px`,
  padding: `${padding.value}px`,
  borderWidth: `${borderWidth.value}px`,
  borderStyle: 'solid',
  borderColor: '#0369a1',
  margin: `${margin.value}px`
}))

const calculated = computed(() => {
  const borderBoxWidth = contentWidth.value + padding.value * 2 + borderWidth.value * 2
  const borderBoxHeight = contentHeight.value + padding.value * 2 + borderWidth.value * 2
  return {
    contentWidth: contentWidth.value,
    contentHeight: contentHeight.value,
    borderBoxWidth,
    borderBoxHeight,
    outerWidth: borderBoxWidth + margin.value * 2,
    outerHeight: borderBoxHeight + margin.value * 2
  }
})

async function measure() {
  await nextTick()
  if (!targetRef.value) return
  actual.value = {
    borderBoxWidth: targetRef.value.offsetWidth,
    borderBoxHeight: targetRef.value.offsetHeight
  }
}

watch([contentWidth, contentHeight, padding, borderWidth, margin], () => {
  void measure()
})
onMounted(() => {
  void measure()
})

const code = computed(
  () => `.wb-box-target {
  box-sizing: content-box;
  width: ${contentWidth.value}px;     /* content 内容区 */
  height: ${contentHeight.value}px;
  padding: ${padding.value}px;       /* padding 内边距 */
  border: ${borderWidth.value}px solid #0369a1;
  margin: ${margin.value}px;         /* margin 外边距 */
}

/* 计算（未考虑相邻 margin 折叠） */
/* border-box = content + padding×2 + border×2 */
/* margin-box = border-box + margin×2 */`
)

function reset() {
  contentWidth.value = defaults.contentWidth
  contentHeight.value = defaults.contentHeight
  padding.value = defaults.padding
  borderWidth.value = defaults.borderWidth
  margin.value = defaults.margin
}
</script>

<template>
  <DemoSection
    number="2.6"
    title="盒模型各部分"
    target="调整 content、padding、border、margin，观察四层结构以及元素尺寸和外部占用空间的变化。"
  >
    <template #demo>
      <div class="wb-demo-controls" aria-label="2.6 盒模型参数">
        <label class="wb-control">
          <span>content width <b>{{ contentWidth }}px</b></span>
          <input v-model.number="contentWidth" data-testid="box-content-width" type="range" min="180" max="360" step="10" />
        </label>
        <label class="wb-control">
          <span>content height <b>{{ contentHeight }}px</b></span>
          <input v-model.number="contentHeight" data-testid="box-content-height" type="range" min="80" max="220" step="10" />
        </label>
        <label class="wb-control">
          <span>padding <b>{{ padding }}px</b></span>
          <input v-model.number="padding" data-testid="box-padding" type="range" min="0" max="30" step="2" />
        </label>
        <label class="wb-control">
          <span>border <b>{{ borderWidth }}px</b></span>
          <input v-model.number="borderWidth" data-testid="box-border" type="range" min="0" max="12" step="1" />
        </label>
        <label class="wb-control">
          <span>margin <b>{{ margin }}px</b></span>
          <input v-model.number="margin" data-testid="box-margin" type="range" min="0" max="30" step="2" />
        </label>
        <button class="wb-reset" type="button" data-testid="box-reset" @click="reset">重置</button>
      </div>

      <div class="wb-box-demo">
        <div class="wb-box-stage" data-testid="box-stage">
          <div ref="targetRef" class="wb-box-target" data-testid="box-target" :style="targetStyle">
            <div class="wb-box-content" data-testid="box-content">
              content
              <small>{{ calculated.contentWidth }} × {{ calculated.contentHeight }}</small>
            </div>
          </div>
        </div>

        <div class="wb-box-legend" aria-label="盒模型图例">
          <span><i class="wb-legend-swatch wb-legend-swatch--content"></i>content 内容区</span>
          <span><i class="wb-legend-swatch wb-legend-swatch--padding"></i>padding 内边距</span>
          <span><i class="wb-legend-swatch wb-legend-swatch--border"></i>border 边框</span>
          <span><i class="wb-legend-swatch wb-legend-swatch--margin"></i>margin 外边距（父容器背景）</span>
        </div>

        <div class="wb-metrics">
          <div class="wb-metric">
            <span>content</span>
            <strong>{{ calculated.contentWidth }} × {{ calculated.contentHeight }}</strong>
            <small>width / height 在 content-box 下指向内容区</small>
          </div>
          <div class="wb-metric">
            <span>border-box（计算）</span>
            <strong>{{ calculated.borderBoxWidth }} × {{ calculated.borderBoxHeight }}</strong>
            <small>content + padding×2 + border×2</small>
          </div>
          <div class="wb-metric">
            <span>border-box（实际）</span>
            <strong data-testid="box-actual">{{ actual.borderBoxWidth }} × {{ actual.borderBoxHeight }}</strong>
            <small>offsetWidth × offsetHeight</small>
          </div>
          <div class="wb-metric">
            <span>margin-box（计算）</span>
            <strong>{{ calculated.outerWidth }} × {{ calculated.outerHeight }}</strong>
            <small>边框盒 + margin×2；垂直 margin 可能折叠</small>
          </div>
        </div>

        <p class="wb-demo-note">
          元素自身的边框盒不包含 margin；margin 是元素外部的占用空间。相邻块级元素的垂直 margin 可能折叠，
          所以 margin-box 的简单相加不一定等于最终布局距离；本演示用 flow-root 容器隔离了折叠，便于观察单盒尺寸。
        </p>
      </div>
    </template>

    <template #conclusion>
      <p>
        盒模型从内到外是 content → padding → border → margin。<code>content-box</code> 下
        <code>width/height</code> 指 content；border-box 尺寸 = content + 2×padding + 2×border；
        margin 在边框盒之外，且相邻垂直 margin 可能折叠，不能无条件把 margin 直接相加当成实际间距。
      </p>
    </template>

    <template #code>
      <CodeBlock :code="code" />
    </template>
  </DemoSection>
</template>
