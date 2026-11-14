<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import type { CSSProperties } from 'vue'
import DemoSection from '@/components/basics/DemoSection.vue'
import CodeBlock from '@/components/basics/CodeBlock.vue'

type BoxSizingMode = 'content-box' | 'border-box'

const props = defineProps<{
  mode: BoxSizingMode
}>()

const defaults = {
  width: 260,
  height: 140,
  padding: 12,
  borderWidth: 4,
  margin: 16
}

const width = ref(defaults.width)
const height = ref(defaults.height)
const padding = ref(defaults.padding)
const borderWidth = ref(defaults.borderWidth)
const margin = ref(defaults.margin)
const targetRef = ref<HTMLElement | null>(null)
const actual = ref({ borderBoxWidth: 0, borderBoxHeight: 0, contentWidth: 0, contentHeight: 0 })

const isContentBox = computed(() => props.mode === 'content-box')

const contentWidth = computed(() =>
  isContentBox.value
    ? width.value
    : Math.max(0, width.value - padding.value * 2 - borderWidth.value * 2)
)
const contentHeight = computed(() =>
  isContentBox.value
    ? height.value
    : Math.max(0, height.value - padding.value * 2 - borderWidth.value * 2)
)
const borderBoxWidth = computed(() =>
  isContentBox.value ? width.value + padding.value * 2 + borderWidth.value * 2 : width.value
)
const borderBoxHeight = computed(() =>
  isContentBox.value ? height.value + padding.value * 2 + borderWidth.value * 2 : height.value
)
const outerWidth = computed(() => borderBoxWidth.value + margin.value * 2)
const outerHeight = computed(() => borderBoxHeight.value + margin.value * 2)

const targetStyle = computed<CSSProperties>(() => ({
  boxSizing: props.mode,
  width: `${width.value}px`,
  height: `${height.value}px`,
  padding: `${padding.value}px`,
  borderWidth: `${borderWidth.value}px`,
  borderStyle: 'solid',
  borderColor: '#0369a1',
  margin: `${margin.value}px`
}))

async function measure() {
  await nextTick()
  if (!targetRef.value) return
  const target = targetRef.value
  actual.value = {
    borderBoxWidth: target.offsetWidth,
    borderBoxHeight: target.offsetHeight,
    contentWidth: target.clientWidth - padding.value * 2,
    contentHeight: target.clientHeight - padding.value * 2
  }
}

watch([width, height, padding, borderWidth, margin], () => {
  void measure()
})
onMounted(() => {
  void measure()
})

const code = computed(
  () => `.wb-box-sizing-target {
  box-sizing: ${props.mode};
  width: ${width.value}px;
  height: ${height.value}px;
  padding: ${padding.value}px;
  border: ${borderWidth.value}px solid #0369a1;
  margin: ${margin.value}px;
}

/* border-box width = ${borderBoxWidth.value}px */
/* content width = ${contentWidth.value}px */`
)

function reset() {
  width.value = defaults.width
  height.value = defaults.height
  padding.value = defaults.padding
  borderWidth.value = defaults.borderWidth
  margin.value = defaults.margin
}
</script>

<template>
  <DemoSection
    :number="isContentBox ? '2.7' : '2.8'"
    :title="isContentBox ? '标准盒模型' : '替代盒模型'"
    :target="isContentBox
      ? '验证 content-box 的 width/height 只表示内容区，边框盒还需要加上 padding 和 border。'
      : '验证 border-box 的 width/height 已经包含 content、padding 和 border；margin 不包含在内。'"
  >
    <template #demo>
      <div class="wb-demo-controls" aria-label="盒模型尺寸参数">
        <label class="wb-control">
          <span>CSS width <b>{{ width }}px</b></span>
          <input v-model.number="width" :data-testid="`box-sizing-${mode}-width`" type="range" min="220" max="360" step="10" />
        </label>
        <label class="wb-control">
          <span>CSS height <b>{{ height }}px</b></span>
          <input v-model.number="height" :data-testid="`box-sizing-${mode}-height`" type="range" min="100" max="220" step="10" />
        </label>
        <label class="wb-control">
          <span>padding <b>{{ padding }}px</b></span>
          <input v-model.number="padding" :data-testid="`box-sizing-${mode}-padding`" type="range" min="0" max="30" step="2" />
        </label>
        <label class="wb-control">
          <span>border <b>{{ borderWidth }}px</b></span>
          <input v-model.number="borderWidth" :data-testid="`box-sizing-${mode}-border`" type="range" min="0" max="12" step="1" />
        </label>
        <label class="wb-control">
          <span>margin <b>{{ margin }}px</b></span>
          <input v-model.number="margin" :data-testid="`box-sizing-${mode}-margin`" type="range" min="0" max="30" step="2" />
        </label>
        <button class="wb-reset" type="button" :data-testid="`box-sizing-${mode}-reset`" @click="reset">重置</button>
      </div>

      <div class="wb-box-demo">
        <div class="wb-box-stage">
          <div
            ref="targetRef"
            class="wb-box-sizing-target"
            :class="`is-${mode}`"
            :data-testid="`box-sizing-${mode}-target`"
            :style="targetStyle"
          >
            <div
              class="wb-box-content"
              :data-testid="`box-sizing-${mode}-content`"
              :style="{ width: `${contentWidth}px`, height: `${contentHeight}px` }"
            >
              content
              <small>{{ contentWidth }} × {{ contentHeight }}</small>
            </div>
          </div>
        </div>

        <div class="wb-box-legend">
          <span><i class="wb-legend-swatch wb-legend-swatch--content"></i>content：{{ contentWidth }} × {{ contentHeight }}</span>
          <span><i class="wb-legend-swatch wb-legend-swatch--padding"></i>padding：{{ padding }}px</span>
          <span><i class="wb-legend-swatch wb-legend-swatch--border"></i>border：{{ borderWidth }}px</span>
          <span><i class="wb-legend-swatch wb-legend-swatch--margin"></i>margin：{{ margin }}px</span>
        </div>

        <div class="wb-metrics">
          <div class="wb-metric">
            <span>CSS width × height</span>
            <strong>{{ width }} × {{ height }}</strong>
            <small>{{ isContentBox ? '只包含 content' : '包含 content + padding + border' }}</small>
          </div>
          <div class="wb-metric">
            <span>content（计算）</span>
            <strong>{{ contentWidth }} × {{ contentHeight }}</strong>
            <small>{{ isContentBox ? '与 CSS width/height 相同' : 'CSS 尺寸减去 padding/border' }}</small>
          </div>
          <div class="wb-metric">
            <span>border-box（实际）</span>
            <strong :data-testid="`box-sizing-${mode}-actual`">{{ actual.borderBoxWidth }} × {{ actual.borderBoxHeight }}</strong>
            <small>offsetWidth × offsetHeight</small>
          </div>
          <div class="wb-metric">
            <span>margin-box（计算）</span>
            <strong>{{ outerWidth }} × {{ outerHeight }}</strong>
            <small>border-box + margin×2；margin 不属于边框盒</small>
          </div>
        </div>

        <p class="wb-demo-note">
          {{ isContentBox
            ? 'content-box：width/height 只作用于 content；padding 和 border 会把边框盒撑大。'
            : 'border-box：width/height 是边框盒尺寸；padding 和 border 从内部挤占 content，margin 仍在边框盒外。' }}
          margin 不包含在 box-sizing 的 width/height 内。
        </p>
      </div>
    </template>

    <template #conclusion>
      <p v-if="isContentBox">
        <code>box-sizing: content-box</code> 下，CSS <code>width/height</code> 就是 content 尺寸；
        实际边框盒宽 = width + 2×padding + 2×border。margin 再增加元素外部空间，但垂直 margin 可能折叠。
      </p>
      <p v-else>
        <code>box-sizing: border-box</code> 下，CSS <code>width/height</code> 已经包含 content、padding 和 border；
        content 尺寸 = width − 2×padding − 2×border。margin 仍不包含在边框盒尺寸内。
      </p>
    </template>

    <template #code>
      <CodeBlock :code="code" />
    </template>
  </DemoSection>
</template>
