<script setup lang="ts">
import { computed, ref } from 'vue'
import DemoSection from '@/components/basics/DemoSection.vue'
import CodeBlock from '@/components/basics/CodeBlock.vue'

const defaults = {
  text: '行内元素',
  count: 3,
  fontSize: 16,
  width: 240,
  height: 64,
  paddingY: 9,
  paddingX: 10,
  borderWidth: 2,
  marginX: 6,
  marginY: 8,
  textColor: '#0f172a',
  background: '#fef3c7',
  borderColor: '#0369a1'
}

const text = ref(defaults.text)
const count = ref(defaults.count)
const fontSize = ref(defaults.fontSize)
const width = ref(defaults.width)
const height = ref(defaults.height)
const paddingY = ref(defaults.paddingY)
const paddingX = ref(defaults.paddingX)
const borderWidth = ref(defaults.borderWidth)
const marginX = ref(defaults.marginX)
const marginY = ref(defaults.marginY)
const textColor = ref(defaults.textColor)
const background = ref(defaults.background)
const borderColor = ref(defaults.borderColor)

const inlineStyle = computed(() => ({
  color: textColor.value,
  backgroundColor: background.value,
  fontSize: `${fontSize.value}px`,
  width: `${width.value}px`,
  height: `${height.value}px`,
  padding: `${paddingY.value}px ${paddingX.value}px`,
  border: `${borderWidth.value}px solid ${borderColor.value}`,
  margin: `${marginY.value}px ${marginX.value}px`
}))

const overlapStyle = computed(() => ({
  ...inlineStyle.value,
  paddingTop: '12px',
  paddingBottom: '12px',
  borderTopWidth: '3px',
  borderBottomWidth: '3px'
}))

const code = computed(
  () => `.wb-inline-demo__item {
  display: inline;
  width: ${width.value}px;  /* 不生效 */
  height: ${height.value}px; /* 不生效 */
  padding: ${paddingY.value}px ${paddingX.value}px;
  border: ${borderWidth.value}px solid ${borderColor.value};
  margin: ${marginY.value}px ${marginX.value}px;
  font-size: ${fontSize.value}px;
  color: ${textColor.value};
  background: ${background.value};
}`
)

function reset() {
  text.value = defaults.text
  count.value = defaults.count
  fontSize.value = defaults.fontSize
  width.value = defaults.width
  height.value = defaults.height
  paddingY.value = defaults.paddingY
  paddingX.value = defaults.paddingX
  borderWidth.value = defaults.borderWidth
  marginX.value = defaults.marginX
  marginY.value = defaults.marginY
  textColor.value = defaults.textColor
  background.value = defaults.background
  borderColor.value = defaults.borderColor
}

function fillLongText() {
  text.value = '这是一段故意写得很长的行内元素文本，用来观察浏览器在容器宽度不足时如何自然换行，而不是用脚本或固定宽度模拟换行。'
  count.value = 2
}
</script>

<template>
  <DemoSection
    number="2.1"
    title="行内显示类型"
    target="观察普通非替换 inline 元素的宽高、内外边距和换行行为。"
  >
    <template #demo>
      <div class="wb-demo-controls" aria-label="2.1 行内显示类型参数">
        <label class="wb-control wb-control--text">
          <span>演示文本</span>
          <input v-model="text" type="text" data-testid="inline-text" />
        </label>
        <label class="wb-control">
          <span>元素数 <b>{{ count }}</b></span>
          <input v-model.number="count" data-testid="inline-count" type="range" min="1" max="5" step="1" />
        </label>
        <label class="wb-control">
          <span>字号 <b>{{ fontSize }}px</b></span>
          <input v-model.number="fontSize" data-testid="inline-font-size" type="range" min="12" max="28" step="1" />
        </label>
        <label class="wb-control">
          <span>尝试 width <b>{{ width }}px</b></span>
          <input v-model.number="width" data-testid="inline-width" type="range" min="60" max="300" step="10" />
        </label>
        <label class="wb-control">
          <span>尝试 height <b>{{ height }}px</b></span>
          <input v-model.number="height" data-testid="inline-height" type="range" min="20" max="100" step="4" />
        </label>
        <label class="wb-control">
          <span>上下 padding <b>{{ paddingY }}px</b></span>
          <input v-model.number="paddingY" data-testid="inline-padding-y" type="range" min="0" max="16" step="1" />
        </label>
        <label class="wb-control">
          <span>左右 padding <b>{{ paddingX }}px</b></span>
          <input v-model.number="paddingX" data-testid="inline-padding-x" type="range" min="0" max="20" step="1" />
        </label>
        <label class="wb-control">
          <span>边框 <b>{{ borderWidth }}px</b></span>
          <input v-model.number="borderWidth" data-testid="inline-border" type="range" min="0" max="6" step="1" />
        </label>
        <label class="wb-control">
          <span>上下 margin <b>{{ marginY }}px</b></span>
          <input v-model.number="marginY" data-testid="inline-margin-y" type="range" min="0" max="12" step="1" />
        </label>
        <label class="wb-control">
          <span>左右 margin <b>{{ marginX }}px</b></span>
          <input v-model.number="marginX" data-testid="inline-margin-x" type="range" min="0" max="20" step="1" />
        </label>
        <label class="wb-control wb-control--color">
          <span>文字色</span>
          <input v-model="textColor" data-testid="inline-color" type="color" />
        </label>
        <label class="wb-control wb-control--color">
          <span>背景色</span>
          <input v-model="background" data-testid="inline-background" type="color" />
        </label>
        <label class="wb-control wb-control--color">
          <span>边框色</span>
          <input v-model="borderColor" type="color" />
        </label>
        <button class="wb-reset" type="button" data-testid="inline-reset" @click="reset">重置</button>
        <button class="wb-reset" type="button" data-testid="inline-long-text" @click="fillLongText">填入长文本</button>
      </div>

      <div class="wb-demo-stage wb-inline-demo" data-testid="inline-stage">
        <p class="wb-inline-demo__line">
          第一行：普通文字
          <template v-for="index in count" :key="index">
            <span class="wb-inline-demo__item" data-testid="inline-item" :style="inlineStyle">
              {{ text }}
            </span>
            <span v-if="index < count" class="wb-inline-demo__space" aria-hidden="true"> </span>
          </template>
          这段文字在行内元素后面继续排列。如果字号或左右内边距增大，左右方向会把相邻文字推开。
        </p>
        <div class="wb-inline-demo__tight" aria-label="紧凑行高下的垂直重叠对照">
          <p>上一行文字：普通行盒</p>
          <p>
            紧凑行：<span class="wb-inline-demo__item" data-testid="inline-overlap-item" :style="overlapStyle">上下 padding</span>
          </p>
          <p>下一行文字：观察上下边框和 padding 是否压到相邻行</p>
        </div>
        <p class="wb-demo-note">
          关键区别：水平方向的 padding / border / margin 会推挤同一行文字；垂直方向的内边距和边框会“画出来”但不会撑开行框，
          因此可能与相邻行重叠。宽高对普通非替换 inline 元素无效。紧凑行对照固定使用 12px 上下 padding、3px 上下边框，
          是为了放大重叠现象；上方主演示仍由控件控制。
        </p>
      </div>
    </template>

    <template #conclusion>
      <p>
        <code>display: inline</code> 的元素与文字在同一行流中，宽度由内容决定。普通非替换行内元素的
        <code>width</code>/<code>height</code> 不参与布局；垂直 margin 不参与布局，垂直 padding/border 只影响绘制，
        可能覆盖相邻行。水平 padding/border/margin 会实际占据行内空间。
      </p>
    </template>

    <template #code>
      <CodeBlock :code="code" />
    </template>
  </DemoSection>
</template>
