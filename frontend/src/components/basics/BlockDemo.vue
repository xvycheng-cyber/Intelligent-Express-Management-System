<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import DemoSection from '@/components/basics/DemoSection.vue'
import CodeBlock from '@/components/basics/CodeBlock.vue'

const defaults = {
  count: 3,
  widthMode: 'auto' as 'auto' | 'fixed',
  width: 240,
  height: 56,
  gap: 12,
  background: '#dbeafe',
  centered: false,
  collapseBottom: 24,
  collapseTop: 16
}

const count = ref(defaults.count)
const widthMode = ref<'auto' | 'fixed'>(defaults.widthMode)
const width = ref(defaults.width)
const height = ref(defaults.height)
const gap = ref(defaults.gap)
const background = ref(defaults.background)
const centered = ref(defaults.centered)
const collapseBottom = ref(defaults.collapseBottom)
const collapseTop = ref(defaults.collapseTop)
const normalFirstRef = ref<HTMLElement | null>(null)
const normalSecondRef = ref<HTMLElement | null>(null)
const flexFirstRef = ref<HTMLElement | null>(null)
const flexSecondRef = ref<HTMLElement | null>(null)
const measurements = ref({
  normalGap: 0,
  contrastGap: 0,
  computedBottom: '0px',
  computedTop: '0px'
})

const normalGap = computed(() => Math.max(collapseBottom.value, collapseTop.value))
const contrastGap = computed(() => collapseBottom.value + collapseTop.value)

function measure() {
  nextTick(() => {
    const normalFirst = normalFirstRef.value
    const normalSecond = normalSecondRef.value
    const flexFirst = flexFirstRef.value
    const flexSecond = flexSecondRef.value
    if (!normalFirst || !normalSecond || !flexFirst || !flexSecond) return
    const normalFirstRect = normalFirst.getBoundingClientRect()
    const normalSecondRect = normalSecond.getBoundingClientRect()
    const flexFirstRect = flexFirst.getBoundingClientRect()
    const flexSecondRect = flexSecond.getBoundingClientRect()
    measurements.value = {
      normalGap: Math.max(0, normalSecondRect.top - normalFirstRect.bottom),
      contrastGap: Math.max(0, flexSecondRect.top - flexFirstRect.bottom),
      computedBottom: getComputedStyle(normalFirst).marginBottom,
      computedTop: getComputedStyle(normalSecond).marginTop
    }
  })
}

watch([collapseBottom, collapseTop], measure)

function onResize() {
  measure()
}

onMounted(() => {
  measure()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
})

const blockStyle = computed(() => ({
  width: widthMode.value === 'auto' ? 'auto' : `${width.value}px`,
  height: `${height.value}px`,
  marginBottom: `${gap.value}px`,
  marginLeft: widthMode.value === 'fixed' && centered.value ? 'auto' : '0',
  marginRight: widthMode.value === 'fixed' && centered.value ? 'auto' : '0',
  background: background.value
}))

const collapseFirstStyle = computed(() => ({
  marginBottom: `${collapseBottom.value}px`
}))

const collapseSecondStyle = computed(() => ({
  marginTop: `${collapseTop.value}px`
}))

const code = computed(
  () => `.wb-block-demo__item {
  display: block;                  /* 独占一行，默认填满可用宽度 */
  width: ${widthMode.value === 'auto' ? 'auto' : `${width.value}px`};
  height: ${height.value}px;
  margin-bottom: ${gap.value}px;
  ${widthMode.value === 'fixed' && centered.value ? 'margin-left: auto;\n  margin-right: auto;\n  ' : ''}background: ${background.value};
}

/* 正常文档流：兄弟 block 的垂直 margin 可折叠 */
.wb-margin-normal .first  { margin-bottom: ${collapseBottom.value}px; }
.wb-margin-normal .second { margin-top: ${collapseTop.value}px; }
/* 实际间距 = max(${collapseBottom.value}, ${collapseTop.value}) = ${normalGap.value}px */

/* 对照：flex items 的 margin 不进行相邻折叠 */
.wb-margin-contrast {
  display: flex;
  flex-direction: column;
}
.wb-margin-contrast .first  { margin-bottom: ${collapseBottom.value}px; }
.wb-margin-contrast .second { margin-top: ${collapseTop.value}px; }
/* flex 对照间距 = ${collapseBottom.value} + ${collapseTop.value} = ${contrastGap.value}px */`
)

function reset() {
  count.value = defaults.count
  widthMode.value = defaults.widthMode
  width.value = defaults.width
  height.value = defaults.height
  gap.value = defaults.gap
  background.value = defaults.background
  centered.value = defaults.centered
  collapseBottom.value = defaults.collapseBottom
  collapseTop.value = defaults.collapseTop
  measure()
}
</script>

<template>
  <DemoSection
    number="2.2"
    title="区块显示类型"
    target="观察 block 元素独占一行、默认填充宽度，以及宽度和间距的控制方式。"
  >
    <template #demo>
      <div class="wb-demo-controls" aria-label="2.2 区块显示类型参数">
        <label class="wb-control">
          <span>区块数 <b>{{ count }}</b></span>
          <input v-model.number="count" data-testid="block-count" type="range" min="1" max="4" step="1" />
        </label>
        <label class="wb-control">
          <span>宽度模式</span>
          <select v-model="widthMode" data-testid="block-width-mode">
            <option value="auto">auto（填充可用宽度）</option>
            <option value="fixed">固定宽度</option>
          </select>
        </label>
        <label class="wb-control">
          <span>固定宽度 <b>{{ width }}px</b></span>
          <input v-model.number="width" data-testid="block-width" type="range" min="100" max="420" step="10" :disabled="widthMode === 'auto'" />
        </label>
        <label class="wb-control">
          <span>高度 <b>{{ height }}px</b></span>
          <input v-model.number="height" data-testid="block-height" type="range" min="28" max="100" step="4" />
        </label>
        <label class="wb-control">
          <span>上下间距 <b>{{ gap }}px</b></span>
          <input v-model.number="gap" data-testid="block-gap" type="range" min="0" max="24" step="1" />
        </label>
        <label class="wb-control wb-control--check">
          <input v-model="centered" data-testid="block-centered" type="checkbox" :disabled="widthMode === 'auto'" />
          <span>固定宽度时水平居中（margin: auto）</span>
        </label>
        <label class="wb-control wb-control--color">
          <span>背景色</span>
          <input v-model="background" data-testid="block-background" type="color" />
        </label>
        <button class="wb-reset" type="button" data-testid="block-reset" @click="reset">重置</button>
      </div>

      <div class="wb-demo-stage wb-block-demo" data-testid="block-stage">
        <div
          v-for="index in count"
          :key="index"
          class="wb-block-demo__item"
          data-testid="block-item"
          :style="blockStyle"
        >
          区块 {{ index }}：display: block
        </div>
        <p class="wb-demo-note">
          块级元素从新行开始，<code>width: auto</code> 时会占用包含块的可用宽度；固定宽度后可用
          <code>margin-left/right: auto</code> 水平居中。
        </p>
      </div>

      <div class="wb-margin-lab" data-testid="margin-collapse-lab">
        <div class="wb-margin-lab__head">
          <div>
            <span class="wb-kicker">MARGIN COLLAPSE</span>
            <h4>相邻垂直 margin 折叠实验</h4>
          </div>
          <span class="wb-status wb-status--ready">真实布局</span>
        </div>

        <div class="wb-demo-controls" aria-label="2.2 margin collapse 参数">
          <label class="wb-control">
            <span>第一个 block 的 margin-bottom <b>{{ collapseBottom }}px</b></span>
            <input v-model.number="collapseBottom" data-testid="margin-bottom" type="range" min="0" max="80" step="1" />
          </label>
          <label class="wb-control">
            <span>第二个 block 的 margin-top <b>{{ collapseTop }}px</b></span>
            <input v-model.number="collapseTop" data-testid="margin-top" type="range" min="0" max="80" step="1" />
          </label>
        </div>

        <div class="wb-margin-lab__grid">
          <section class="wb-margin-lab__panel">
            <h5>A. 正常文档流（block）</h5>
            <div class="wb-margin-normal" data-testid="margin-normal-stage">
              <div ref="normalFirstRef" class="wb-margin-box first" data-testid="margin-normal-first" :style="collapseFirstStyle">
                第一个 block<br /><small>margin-bottom: {{ collapseBottom }}px</small>
              </div>
              <div ref="normalSecondRef" class="wb-margin-box second" data-testid="margin-normal-second" :style="collapseSecondStyle">
                第二个 block<br /><small>margin-top: {{ collapseTop }}px</small>
              </div>
            </div>
            <p class="wb-margin-lab__result">
              实际边框盒间距：<strong data-testid="margin-actual-normal">{{ measurements.normalGap.toFixed(1) }}px</strong><br />
              预期：max({{ collapseBottom }}, {{ collapseTop }}) =
              <strong data-testid="margin-expected-normal">{{ normalGap }}px</strong>
            </p>
          </section>

          <section class="wb-margin-lab__panel">
            <h5>B. flex 对照（不折叠）</h5>
            <div class="wb-margin-contrast" data-testid="margin-contrast-stage">
              <div ref="flexFirstRef" class="wb-margin-box first" data-testid="margin-contrast-first" :style="collapseFirstStyle">
                第一个 flex item<br /><small>margin-bottom: {{ collapseBottom }}px</small>
              </div>
              <div ref="flexSecondRef" class="wb-margin-box second" data-testid="margin-contrast-second" :style="collapseSecondStyle">
                第二个 flex item<br /><small>margin-top: {{ collapseTop }}px</small>
              </div>
            </div>
            <p class="wb-margin-lab__result">
              实际边框盒间距：<strong data-testid="margin-actual-contrast">{{ measurements.contrastGap.toFixed(1) }}px</strong><br />
              预期：{{ collapseBottom }} + {{ collapseTop }} =
              <strong data-testid="margin-expected-contrast">{{ contrastGap }}px</strong>
            </p>
          </section>
        </div>

        <p class="wb-demo-note">
          正常文档流中，两个相邻 block 的正垂直 margin 会折叠，间距取较大值，而不是相加；
          flex 容器中的项目不进行这种相邻 margin 折叠，因此出现两个 margin 相加的对照结果。
          本实验只演示相邻兄弟 block 的基本场景；父子 margin、空 block、不同格式化上下文等不属于本实验的完整范围。
          两个实验容器都有 padding/border，但没有在实验元素之间插入其它元素，也没有把正常流实验放进 flex/grid。
        </p>
      </div>
    </template>

    <template #conclusion>
      <p>
        <code>display: block</code> 的元素独占一行，默认宽度撑满包含块。它可以设置宽高、外边距和内边距；
        多个块在常规流中从上到下排列。固定宽度时，左右 <code>margin: auto</code> 可以水平居中。
      </p>
    </template>

    <template #code>
      <CodeBlock :code="code" />
    </template>
  </DemoSection>
</template>
