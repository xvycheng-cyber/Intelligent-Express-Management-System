<script setup lang="ts">
import { computed } from 'vue'
import AppIcon from '@/components/common/AppIcon.vue'
import type { ParsedRow } from '@/types/address'
import { isRowEdited } from '@/utils/addressPayload'
import { canSubmitRow, validateRow } from '@/utils/addressValidation'

const props = defineProps<{
  modelValue: boolean
  rows: ParsedRow[]
  loading: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: []
}>()

const submitRows = computed(() => props.rows.filter((row) => canSubmitRow(row)))
const unresolvedRows = computed(() => props.rows.filter((row) => !canSubmitRow(row)))
const editedCount = computed(() => props.rows.filter((row) => isRowEdited(row)).length)
const invalidParsedCount = computed(() => props.rows.filter((row) => row.status === 'INVALID').length)

const unresolvedDetails = computed(() =>
  unresolvedRows.value.map((row) => ({
    lineNo: row.lineNo,
    reasons: reasonsFor(row)
  }))
)

function reasonsFor(row: ParsedRow): string[] {
  const local = validateRow(row)
  if (local.length > 0) return local
  if (row.status === 'INVALID') {
    return row.errors.length
      ? row.errors.map((error) => `${error.code} · ${error.message}`)
      : ['原解析状态为 INVALID，请先修正并修改字段']
  }
  return ['当前字段未通过校验']
}

function close() {
  if (!props.loading) emit('update:modelValue', false)
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    width="min(720px, calc(100vw - 32px))"
    class="save-dialog"
    :close-on-click-modal="!loading"
    :close-on-press-escape="!loading"
    :show-close="!loading"
    align-center
    @update:model-value="emit('update:modelValue', $event)"
  >
    <template #header>
      <div class="dialog-heading">
        <span class="dialog-heading__icon"><AppIcon name="save" :size="20" /></span>
        <div>
          <h3>确认保存地址</h3>
          <p>请确认提交数量与异常行；无效记录不会进入保存请求。</p>
        </div>
      </div>
    </template>

    <div class="dialog-summary">
      <div class="summary-item">
        <span>有效记录</span>
        <strong>{{ submitRows.length }}</strong>
      </div>
      <div class="summary-item">
        <span>人工修改</span>
        <strong>{{ editedCount }}</strong>
      </div>
      <div class="summary-item summary-item--warning">
        <span>原解析 INVALID</span>
        <strong>{{ invalidParsedCount }}</strong>
      </div>
      <div class="summary-item summary-item--danger">
        <span>待修正行</span>
        <strong>{{ unresolvedRows.length }}</strong>
      </div>
    </div>

    <el-alert
      v-if="unresolvedRows.length"
      type="warning"
      :closable="false"
      show-icon
      title="存在未修正的记录"
      :description="`以下 ${unresolvedRows.length} 行不会提交，请返回表格修正。`"
      class="dialog-alert"
    />

    <div v-if="unresolvedDetails.length" class="unresolved-list" data-testid="unresolved-list">
      <div v-for="detail in unresolvedDetails" :key="detail.lineNo" class="unresolved-item">
        <span class="unresolved-line">第 {{ detail.lineNo }} 行</span>
        <div class="unresolved-reasons">
          <span v-for="reason in detail.reasons" :key="reason">{{ reason }}</span>
        </div>
      </div>
    </div>

    <div v-else class="ready-note">
      <AppIcon name="check" :size="16" />
      全部 {{ submitRows.length }} 条记录已通过前端字段校验，可以提交。
    </div>

    <div class="submit-count">
      实际提交 <strong>{{ submitRows.length }}</strong> 条
      <span v-if="unresolvedRows.length"> · 跳过 {{ unresolvedRows.length }} 条未修正记录</span>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button :disabled="loading" @click="close">返回修改</el-button>
        <el-button
          type="primary"
          :loading="loading"
          :disabled="submitRows.length === 0 || loading"
          data-testid="confirm-save"
          @click="emit('confirm')"
        >
          <AppIcon v-if="!loading" name="save" :size="16" />
          确认保存{{ submitRows.length ? `（${submitRows.length}）` : '' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
.dialog-heading {
  display: flex;
  align-items: flex-start;
  gap: 12px;

  &__icon {
    width: 38px;
    height: 38px;
    flex: 0 0 38px;
    display: grid;
    place-items: center;
    border-radius: 10px;
    background: var(--color-primary-soft);
    color: var(--color-primary);
  }

  h3 {
    margin: 0;
    color: var(--color-text);
    font-size: 17px;
  }

  p {
    margin: 3px 0 0;
    color: var(--color-text-secondary);
    font-size: 12px;
  }
}

.dialog-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 9px;
}

.summary-item {
  padding: 12px 13px;
  border: 1px solid var(--color-border);
  border-radius: 9px;
  background: #f8fafc;

  span {
    display: block;
    color: var(--color-text-muted);
    font-size: 10px;
  }

  strong {
    display: block;
    margin-top: 2px;
    color: var(--color-text);
    font-size: 22px;
    line-height: 1.2;
    font-variant-numeric: tabular-nums;
  }

  &--warning strong {
    color: var(--color-warning);
  }

  &--danger strong {
    color: var(--color-danger);
  }
}

.dialog-alert {
  margin-top: 14px;
}

.unresolved-list {
  display: flex;
  flex-direction: column;
  max-height: 190px;
  margin-top: 12px;
  overflow-y: auto;
  border: 1px solid #fee2e2;
  border-radius: 9px;
  background: #fffafa;
}

.unresolved-item {
  display: flex;
  gap: 10px;
  padding: 9px 12px;
  border-bottom: 1px solid #fee2e2;

  &:last-child {
    border-bottom: 0;
  }
}

.unresolved-line {
  flex: 0 0 64px;
  color: var(--color-danger);
  font-size: 11px;
  font-weight: 600;
}

.unresolved-reasons {
  display: flex;
  flex-direction: column;
  gap: 2px;
  color: var(--color-text-secondary);
  font-size: 11px;
}

.ready-note {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-top: 14px;
  padding: 11px 13px;
  border-radius: 9px;
  background: var(--color-success-soft);
  color: var(--color-success);
  font-size: 12px;
}

.submit-count {
  margin-top: 13px;
  padding-top: 12px;
  border-top: 1px dashed var(--color-border);
  color: var(--color-text-secondary);
  font-size: 12px;
  text-align: right;

  strong {
    color: var(--color-primary-strong);
    font-size: 18px;
  }
}

.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 9px;
}

@media (max-width: 620px) {
  .dialog-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
