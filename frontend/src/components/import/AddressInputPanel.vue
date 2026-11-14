<script setup lang="ts">
import { computed } from 'vue'
import AppIcon from '@/components/common/AppIcon.vue'

const props = defineProps<{
  modelValue: string
  loading: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  parse: []
  clear: []
}>()

const EXAMPLE_TEXT = `菜小鸟，13800138000，浙江省杭州市西湖区湖滨小区12栋301室
张小明\t13900139000\t浙江省杭州市滨江区某某小区5栋202室
李女士	0571-88888888	浙江省杭州市拱墅区运河东路88号`

const charCount = computed(() => props.modelValue.length)
const effectiveLines = computed(
  () => props.modelValue.split(/\r\n|\r|\n/).filter((line) => line.trim().length > 0).length
)
const overCharacters = computed(() => charCount.value > 20000)
const overLines = computed(() => effectiveLines.value > 200)
const canParse = computed(() => props.modelValue.trim().length > 0)

function updateValue(value: string) {
  emit('update:modelValue', value)
}

function fillExample() {
  updateValue(EXAMPLE_TEXT)
}
</script>

<template>
  <section class="input-panel panel-card">
    <header class="panel-card__header">
      <div class="panel-card__heading">
        <span class="step-badge">01</span>
        <div>
          <h2>粘贴收件信息</h2>
          <p>每行一位联系人，支持中文逗号、英文逗号、Tab、分号和空行。</p>
        </div>
      </div>
      <div class="panel-card__meta">
        <span>{{ effectiveLines }} 条有效行</span>
        <span>·</span>
        <span>上限 200 条</span>
      </div>
    </header>

    <div class="input-panel__body">
      <el-input
        :model-value="modelValue"
        type="textarea"
        :rows="9"
        resize="vertical"
        placeholder="例如：菜小鸟，13800138000，浙江省杭州市西湖区湖滨小区12栋301室"
        aria-label="原始收件信息"
        data-testid="raw-text-input"
        @update:model-value="updateValue"
      />

      <div class="input-panel__toolbar">
        <div class="input-panel__counters" aria-live="polite">
          <span :class="{ 'is-over': overCharacters }">
            字符 {{ charCount.toLocaleString() }} / 20,000
          </span>
          <span class="divider" aria-hidden="true"></span>
          <span :class="{ 'is-over': overLines }">
            有效行 {{ effectiveLines }} / 200
          </span>
          <span v-if="overCharacters || overLines" class="limit-warning">
            已超过前端提示上限，提交时后端仍会做最终校验
          </span>
        </div>
        <div class="input-panel__actions">
          <el-button :disabled="modelValue.length === 0 || loading" @click="emit('clear')">
            清空
          </el-button>
          <el-button text type="primary" @click="fillExample">填入示例</el-button>
          <el-button
            type="primary"
            :loading="loading"
            :disabled="!canParse || loading"
            data-testid="parse-button"
            @click="emit('parse')"
          >
            <AppIcon v-if="!loading" name="spark" :size="16" />
            识别地址
          </el-button>
        </div>
      </div>

      <div class="input-panel__examples">
        <div class="examples__title">
          <AppIcon name="info" :size="15" />
          <span>支持的输入格式</span>
        </div>
        <div class="examples__chips">
          <span class="format-chip">中文逗号 <code>，</code></span>
          <span class="format-chip">英文逗号 <code>,</code></span>
          <span class="format-chip">Tab</span>
          <span class="format-chip">换行分隔</span>
        </div>
        <p>顺序不必固定；识别后仍可逐行修改。空行不会生成记录。</p>
      </div>

      <el-alert
        v-if="overCharacters || overLines"
        type="warning"
        :closable="false"
        show-icon
        title="输入内容超过页面提示上限"
        description="请减少字符或有效行后再提交；后端会以 20000 字符、200 条有效行为最终边界。"
        class="input-panel__alert"
      />
    </div>
  </section>
</template>

<style scoped lang="scss">
.panel-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
}

.panel-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 22px 24px 0;
}

.panel-card__heading {
  display: flex;
  gap: 13px;

  h2 {
    margin: 0;
    font-size: 17px;
    letter-spacing: -0.01em;
  }

  p {
    margin: 3px 0 0;
    color: var(--color-text-secondary);
    font-size: 12px;
  }
}

.step-badge {
  width: 28px;
  height: 28px;
  flex: 0 0 28px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: var(--color-primary-soft);
  color: var(--color-primary);
  font-size: 11px;
  font-weight: 800;
}

.panel-card__meta {
  display: flex;
  gap: 7px;
  padding-top: 5px;
  color: var(--color-text-muted);
  font-size: 11px;
  white-space: nowrap;
}

.input-panel__body {
  padding: 18px 24px 22px;
}

.input-panel__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-top: 12px;
}

.input-panel__counters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 7px;
  color: var(--color-text-muted);
  font-size: 11px;

  .is-over {
    color: var(--color-danger);
    font-weight: 600;
  }
}

.divider {
  width: 1px;
  height: 12px;
  background: var(--color-border);
}

.limit-warning {
  color: var(--color-danger);
}

.input-panel__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.input-panel__examples {
  margin-top: 18px;
  padding: 13px 15px;
  border-radius: var(--radius-sm);
  background: #f8fafc;
  border: 1px dashed var(--color-border);
  color: var(--color-text-secondary);
  font-size: 11px;

  p {
    margin: 8px 0 0;
  }
}

.examples__title {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--color-text-secondary);
  font-weight: 600;
}

.examples__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-top: 8px;
}

.format-chip {
  padding: 3px 8px;
  border: 1px solid var(--color-border);
  border-radius: 5px;
  background: #fff;
  color: var(--color-text-secondary);

  code {
    padding: 0 3px;
    border-radius: 3px;
    background: #eef2f7;
    color: var(--color-primary-strong);
  }
}

.input-panel__alert {
  margin-top: 14px;
}

@media (max-width: 760px) {
  .panel-card__header {
    flex-direction: column;
    gap: 6px;
    padding: 18px 16px 0;
  }

  .input-panel__body {
    padding: 14px 16px 18px;
  }

  .input-panel__toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .input-panel__actions {
    width: 100%;
    justify-content: flex-end;
    flex-wrap: wrap;
  }
}
</style>
