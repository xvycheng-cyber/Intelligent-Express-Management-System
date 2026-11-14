<script setup lang="ts">
import AppIcon from '@/components/common/AppIcon.vue'
import type { ParsedRow } from '@/types/address'
import { isRowEdited } from '@/utils/addressPayload'
import { canSubmitRow, nameLength, validateRow } from '@/utils/addressValidation'

defineProps<{
  rows: ParsedRow[]
}>()

const emit = defineEmits<{
  restore: [row: ParsedRow]
}>()

function rowState(row: ParsedRow): { label: string; type: 'success' | 'warning' | 'danger' | 'info' } {
  const issues = validateRow(row)
  if (issues.length > 0) {
    return { label: isRowEdited(row) ? '需修正' : '待修正', type: 'danger' }
  }
  if (row.status === 'VALID') return { label: '已识别', type: 'success' }
  return isRowEdited(row) ? { label: '已修正待提交', type: 'warning' } : { label: '待修正', type: 'danger' }
}

function rowIssues(row: ParsedRow): string[] {
  const local = validateRow(row)
  if (local.length > 0) return local
  if (row.status === 'INVALID' && !isRowEdited(row)) {
    return row.errors.map((error) => `${error.code} · ${error.message}`)
  }
  return []
}

function rowClassName({ row }: { row: ParsedRow }): string {
  return row.status === 'INVALID' ? 'row-invalid' : ''
}

function errorCodes(row: ParsedRow): string {
  return row.errors.map((error) => error.code).join(' / ')
}

function canRestore(row: ParsedRow): boolean {
  return isRowEdited(row)
}

function canSubmit(row: ParsedRow): boolean {
  return canSubmitRow(row)
}

function nameState(row: ParsedRow): 'error' | undefined {
  return validateRow(row).some((issue) => issue.startsWith('姓名')) ? 'error' : undefined
}

function phoneState(row: ParsedRow): 'error' | undefined {
  return validateRow(row).some((issue) => issue.startsWith('联系电话')) ? 'error' : undefined
}

function addressState(row: ParsedRow): 'error' | undefined {
  return validateRow(row).some((issue) => issue.startsWith('详细地址')) ? 'error' : undefined
}
</script>

<template>
  <section class="result-panel panel-card">
    <header class="result-panel__header">
      <div class="result-panel__heading">
        <span class="step-badge">02</span>
        <div>
          <h2>核对解析结果</h2>
          <p>可直接修改姓名、电话和地址。无效记录修正并通过校验后才能提交。</p>
        </div>
      </div>
      <span class="result-panel__hint">
        <AppIcon name="info" :size="14" />
        原始行与解析值可展开查看
      </span>
    </header>

    <el-table
      :data="rows"
      row-key="rowKey"
      class="result-table"
      :row-class-name="rowClassName"
      data-testid="parse-result-table"
    >
      <el-table-column type="expand">
        <template #default="{ row }">
          <div class="raw-detail">
            <div class="raw-detail__block">
              <span class="raw-detail__label">原始行</span>
              <code>{{ row.rawLine || '（空）' }}</code>
            </div>
            <div class="raw-detail__block">
              <span class="raw-detail__label">解析原始值</span>
              <span>姓名：{{ row.original.receiverName || '—' }} / 电话：{{ row.original.phone || '—' }} / 地址：{{ row.original.fullAddress || '—' }}</span>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="行号" width="70" align="center">
        <template #default="{ row }">
          <span class="row-number">{{ row.lineNo }}</span>
        </template>
      </el-table-column>

      <el-table-column label="联系人姓名" width="170">
        <template #default="{ row }">
          <el-input
            v-model="row.receiverName"
            size="small"
            :class="{ 'field-error': nameState(row) }"
            aria-label="联系人姓名"
            data-testid="name-input"
          />
          <div class="field-counter" :class="{ 'is-over': nameLength(row.receiverName) > 50 }">
            {{ nameLength(row.receiverName) }} / 50 码点
          </div>
        </template>
      </el-table-column>

      <el-table-column label="联系电话" width="190">
        <template #default="{ row }">
          <el-input
            v-model="row.phone"
            size="small"
            :class="{ 'field-error': phoneState(row) }"
            aria-label="联系电话"
            data-testid="phone-input"
          />
        </template>
      </el-table-column>

      <el-table-column label="详细地址" min-width="300">
        <template #default="{ row }">
          <el-input
            v-model="row.fullAddress"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 4 }"
            resize="none"
            :class="{ 'field-error': addressState(row) }"
            aria-label="详细地址"
            data-testid="address-input"
          />
          <div class="field-counter">{{ row.fullAddress.trim().length }} / 255</div>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="116" align="center">
        <template #default="{ row }">
          <el-tag :type="rowState(row).type" effect="light" size="small">
            {{ rowState(row).label }}
          </el-tag>
          <span v-if="canSubmit(row)" class="submit-dot" title="可提交" aria-label="可提交"></span>
        </template>
      </el-table-column>

      <el-table-column label="校验 / 错误原因" min-width="210">
        <template #default="{ row }">
          <div v-if="rowIssues(row).length" class="issue-list">
            <div v-for="issue in rowIssues(row)" :key="issue" class="issue">
              {{ issue }}
            </div>
          </div>
          <span v-else class="issue-ok">
            <AppIcon name="check" :size="13" />
            可提交
          </span>
          <div v-if="row.status === 'INVALID' && isRowEdited(row) && validateRow(row).length === 0" class="original-error">
            原解析：{{ errorCodes(row) }}（已修正）
          </div>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="86" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            size="small"
            :disabled="!canRestore(row)"
            data-testid="restore-row"
            @click="emit('restore', row)"
          >
            <AppIcon name="restore" :size="14" />
            恢复
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<style scoped lang="scss">
.panel-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
}

.result-panel {
  overflow: hidden;
}

.result-panel__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 22px 24px 17px;
}

.result-panel__heading {
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

.result-panel__hint {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding-top: 5px;
  color: var(--color-text-muted);
  font-size: 11px;
  white-space: nowrap;
}

.result-table {
  width: 100%;
}

.row-number {
  display: inline-grid;
  place-items: center;
  min-width: 24px;
  height: 24px;
  padding: 0 6px;
  border-radius: 6px;
  background: #f1f5f9;
  color: var(--color-text-secondary);
  font-size: 11px;
  font-variant-numeric: tabular-nums;
}

.field-counter {
  margin-top: 3px;
  color: var(--color-text-muted);
  font-size: 10px;
  line-height: 1.2;
  text-align: right;

  &.is-over {
    color: var(--color-danger);
    font-weight: 600;
  }
}

.submit-dot {
  display: inline-block;
  width: 5px;
  height: 5px;
  margin-left: 5px;
  border-radius: 50%;
  background: var(--color-success);
  vertical-align: middle;
}

.issue-list {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.issue {
  padding: 2px 0;
  color: var(--color-danger);
  font-size: 11px;
  line-height: 1.4;
}

.original-error {
  margin-top: 3px;
  color: var(--color-text-muted);
  font-size: 10px;
}

.issue-ok {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--color-success);
  font-size: 11px;
}

.raw-detail {
  display: flex;
  flex-direction: column;
  gap: 9px;
  padding: 10px 14px 12px;
  background: #f8fafc;
  border-radius: 8px;
  color: var(--color-text-secondary);
  font-size: 12px;
}

.raw-detail__block {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.raw-detail__label {
  color: var(--color-text-muted);
  font-size: 10px;
  letter-spacing: 0.04em;
}

.raw-detail code {
  color: var(--color-text);
  white-space: pre-wrap;
  word-break: break-all;
}

:deep(.row-invalid) {
  background: #fffafa;
}

:deep(.el-table__expanded-cell) {
  padding: 0 20px 10px;
  background: #fff;
}

:deep(.field-error .el-input__wrapper),
:deep(.field-error .el-textarea__inner) {
  box-shadow: 0 0 0 1px var(--el-color-danger) inset;
}

@media (max-width: 760px) {
  .result-panel__header {
    flex-direction: column;
    gap: 6px;
    padding: 18px 16px 13px;
  }

  .result-panel__hint {
    padding-top: 0;
  }
}
</style>
