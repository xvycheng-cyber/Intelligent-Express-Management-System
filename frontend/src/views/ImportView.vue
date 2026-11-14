<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AddressInputPanel from '@/components/import/AddressInputPanel.vue'
import ParseStatistics from '@/components/import/ParseStatistics.vue'
import ParseResultTable from '@/components/import/ParseResultTable.vue'
import SaveConfirmDialog from '@/components/import/SaveConfirmDialog.vue'
import AppIcon from '@/components/common/AppIcon.vue'
import { parseAddresses, saveAddressBatch } from '@/api/addresses'
import { ApiError } from '@/api/request'
import type { ParsedRow, ParseResult } from '@/types/address'
import { canSubmitRow } from '@/utils/addressValidation'
import {
  isRowEdited,
  payloadSignature,
  restoreRow,
  toPayload
} from '@/utils/addressPayload'

const rawText = ref('')
const rows = ref<ParsedRow[]>([])
const parseResult = ref<ParseResult | null>(null)
const isParsing = ref(false)
const isSaving = ref(false)
const parseError = ref<ApiError | null>(null)
const saveError = ref<ApiError | null>(null)
const saveDialogVisible = ref(false)
const savedSignature = ref('')
const savedCount = ref(0)

const hasResult = computed(() => rows.value.length > 0)
const totalCount = computed(() => rows.value.length)
const validCount = computed(() => rows.value.filter((row) => row.status === 'VALID').length)
const invalidCount = computed(() => rows.value.filter((row) => row.status === 'INVALID').length)
const editedCount = computed(() => rows.value.filter((row) => isRowEdited(row)).length)
const submittableCount = computed(() => rows.value.filter((row) => canSubmitRow(row)).length)
const unresolvedCount = computed(() => totalCount.value - submittableCount.value)

const submitRows = computed(() => rows.value.filter((row) => canSubmitRow(row)))
const submitPayloads = computed(() => submitRows.value.map((row) => toPayload(row)))
const currentSignature = computed(() => payloadSignature(submitPayloads.value))
const isCurrentSaved = computed(
  () => savedSignature.value.length > 0 && savedSignature.value === currentSignature.value
)

function makeRows(result: ParseResult): ParsedRow[] {
  return result.items.map((item, index) => ({
    rowKey: `line-${item.lineNo}-${index}`,
    lineNo: item.lineNo,
    rawLine: item.rawLine,
    receiverName: item.receiverName ?? '',
    phone: item.phone ?? '',
    fullAddress: item.fullAddress ?? '',
    original: {
      receiverName: item.receiverName ?? '',
      phone: item.phone ?? '',
      fullAddress: item.fullAddress ?? ''
    },
    status: item.status,
    errors: item.errors ?? []
  }))
}

async function handleParse() {
  if (isParsing.value || !rawText.value.trim()) return
  isParsing.value = true
  parseError.value = null
  saveError.value = null
  savedSignature.value = ''
  savedCount.value = 0
  try {
    const result = await parseAddresses(rawText.value)
    parseResult.value = result
    rows.value = makeRows(result)
    if (rows.value.length === 0) {
      ElMessage.warning('没有识别到有效记录，请检查输入内容')
    } else {
      ElMessage.success(`已识别 ${rows.value.length} 条记录`)
    }
  } catch (error) {
    parseError.value = error instanceof ApiError ? error : new ApiError('识别失败，请稍后重试')
    rows.value = []
    parseResult.value = null
  } finally {
    isParsing.value = false
  }
}

function handleClear() {
  if (isParsing.value || isSaving.value) return
  rawText.value = ''
  rows.value = []
  parseResult.value = null
  parseError.value = null
  saveError.value = null
  savedSignature.value = ''
  savedCount.value = 0
}

function handleRestore(row: ParsedRow) {
  restoreRow(row)
}

function openSaveDialog() {
  if (isSaving.value) return
  if (rows.value.length === 0) {
    ElMessage.warning('请先识别地址')
    return
  }
  if (submittableCount.value === 0) {
    ElMessage.warning('没有可提交的记录，请先修正异常行')
    return
  }
  if (isCurrentSaved.value) {
    ElMessage.info('当前内容已经保存，无需重复提交')
    return
  }
  saveError.value = null
  saveDialogVisible.value = true
}

async function handleConfirmSave() {
  if (isSaving.value) return
  const payloads = submitPayloads.value
  if (payloads.length === 0) {
    ElMessage.warning('没有可提交的记录')
    return
  }
  if (savedSignature.value === payloadSignature(payloads)) {
    saveDialogVisible.value = false
    ElMessage.info('当前内容已经保存，无需重复提交')
    return
  }

  isSaving.value = true
  saveError.value = null
  const signature = payloadSignature(payloads)
  try {
    const result = await saveAddressBatch(payloads)
    savedSignature.value = signature
    savedCount.value = result.savedCount
    saveDialogVisible.value = false
    ElMessage.success(`已保存 ${result.savedCount} 条地址`)
  } catch (error) {
    saveError.value = error instanceof ApiError ? error : new ApiError('保存失败，请稍后重试')
    saveDialogVisible.value = false
    ElMessage.error('保存失败，请根据页面提示修正后重试')
  } finally {
    isSaving.value = false
  }
}
</script>

<template>
  <section class="import-page">
    <header class="page-hero">
      <div class="page-hero__copy">
        <div class="eyebrow"><span></span>MS-04 · 批量录入</div>
        <h1>批量录入收件地址</h1>
        <p>
          支持一次性录入多个联系人信息，自动识别姓名、联系电话和收件地址，确认无误后批量保存。
        </p>
      </div>
      <div class="page-hero__aside">
        <div class="hero-stat">
          <strong>200</strong>
          <span>单次最多记录</span>
        </div>
        <div class="hero-stat">
          <strong>3</strong>
          <span>字段自动识别</span>
        </div>
        <div class="hero-stat">
          <strong>0</strong>
          <span>解析时写入数据库</span>
        </div>
      </div>
    </header>

    <div class="workflow" aria-label="录入流程">
      <div class="workflow__step is-current">
        <span>01</span>
        <div><strong>粘贴原文</strong><small>每行一条联系人</small></div>
      </div>
      <div class="workflow__line" aria-hidden="true"></div>
      <div class="workflow__step">
        <span>02</span>
        <div><strong>智能识别</strong><small>姓名 / 电话 / 地址</small></div>
      </div>
      <div class="workflow__line" aria-hidden="true"></div>
      <div class="workflow__step">
        <span>03</span>
        <div><strong>人工核对</strong><small>修正异常行</small></div>
      </div>
      <div class="workflow__line" aria-hidden="true"></div>
      <div class="workflow__step">
        <span>04</span>
        <div><strong>确认保存</strong><small>整批事务写入</small></div>
      </div>
    </div>

    <AddressInputPanel
      v-model="rawText"
      :loading="isParsing"
      @parse="handleParse"
      @clear="handleClear"
    />

    <el-alert
      v-if="parseError"
      type="error"
      :closable="false"
      show-icon
      title="识别失败"
      class="page-alert"
      data-testid="parse-error"
    >
      <template #default>
        <p class="alert-message">{{ parseError.message }}</p>
        <ul v-if="parseError.violations.length" class="alert-list">
          <li v-for="violation in parseError.violations" :key="`${violation.field}-${violation.message}`">
            {{ violation.field }}：{{ violation.message }}
          </li>
        </ul>
      </template>
    </el-alert>

    <div v-if="parseResult" class="parse-meta">
      共 {{ parseResult.totalLines }} 个物理行 · 忽略 {{ parseResult.skippedLines }} 个空行 · 有效行
      {{ parseResult.effectiveLines }} 条
    </div>

    <template v-if="hasResult">
      <ParseStatistics
        :total="totalCount"
        :valid="validCount"
        :invalid="invalidCount"
        :edited="editedCount"
        :submittable="submittableCount"
      />

      <div v-if="saveError" class="page-alert">
        <el-alert
          type="error"
          :closable="false"
          show-icon
          title="保存失败，数据已保留"
          data-testid="save-error"
        >
          <template #default>
            <p class="alert-message">{{ saveError.message }}</p>
            <ul v-if="saveError.violations.length" class="alert-list">
              <li v-for="violation in saveError.violations" :key="`${violation.field}-${violation.message}`">
                {{ violation.field }}：{{ violation.message }}
              </li>
            </ul>
            <p class="alert-hint">请修正后再次保存；本次没有清空你的输入和编辑结果。</p>
          </template>
        </el-alert>
      </div>

      <div v-if="savedCount > 0" class="save-success" data-testid="save-success">
        <AppIcon name="check" :size="16" />
        已保存 <strong>{{ savedCount }}</strong> 条地址。当前内容再次保存前请先修改，避免重复提交。
      </div>

      <ParseResultTable :rows="rows" @restore="handleRestore" />

      <div class="save-bar">
        <div class="save-bar__summary">
          <span class="save-bar__dot" aria-hidden="true"></span>
          <span>可提交 <strong>{{ submittableCount }}</strong> 条</span>
          <span v-if="unresolvedCount" class="save-bar__warning">
            · {{ unresolvedCount }} 条仍未修正
          </span>
        </div>
        <div class="save-bar__actions">
          <el-button text :disabled="isSaving" @click="handleClear">重新录入</el-button>
          <el-button
            type="primary"
            size="large"
            :loading="isSaving"
            :disabled="submittableCount === 0 || isSaving || isCurrentSaved"
            data-testid="open-save-dialog"
            @click="openSaveDialog"
          >
            <AppIcon v-if="!isSaving" name="save" :size="17" />
            {{ isCurrentSaved ? '当前内容已保存' : `保存地址（${submittableCount}）` }}
          </el-button>
        </div>
      </div>
    </template>

    <div v-else class="empty-state" data-testid="empty-state">
      <div class="empty-state__art" aria-hidden="true">
        <AppIcon name="inbox" :size="34" />
      </div>
      <h3>识别结果将在这里展示</h3>
      <p>粘贴文本并点击“识别地址”，逐行核对姓名、电话和地址后再保存。</p>
      <div class="empty-state__notes">
        <span>不写入数据库</span>
        <span>可逐行修正</span>
        <span>无效行不会提交</span>
      </div>
    </div>

    <SaveConfirmDialog
      v-model="saveDialogVisible"
      :rows="rows"
      :loading="isSaving"
      @confirm="handleConfirmSave"
    />
  </section>
</template>

<style scoped lang="scss">
.import-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding-bottom: 84px;
}

.page-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 28px;
  padding: 8px 2px 5px;
}

.page-hero__copy {
  max-width: 700px;

  h1 {
    margin: 6px 0 7px;
    color: var(--color-text);
    font-size: clamp(25px, 2.2vw, 32px);
    line-height: 1.22;
    letter-spacing: -0.035em;
  }

  p {
    max-width: 660px;
    margin: 0;
    color: var(--color-text-secondary);
    font-size: 13px;
    line-height: 1.75;
  }
}

.eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: var(--color-primary);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;

  span {
    width: 16px;
    height: 2px;
    border-radius: 2px;
    background: var(--color-primary);
  }
}

.page-hero__aside {
  display: flex;
  gap: 10px;
  flex: 0 0 auto;
}

.hero-stat {
  min-width: 86px;
  padding: 10px 12px;
  border: 1px solid var(--color-border);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.65);

  strong {
    display: block;
    color: var(--color-primary-strong);
    font-size: 20px;
    line-height: 1.2;
    font-variant-numeric: tabular-nums;
  }

  span {
    display: block;
    margin-top: 2px;
    color: var(--color-text-muted);
    font-size: 10px;
    white-space: nowrap;
  }
}

.workflow {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 16px;
  border: 1px solid var(--color-border);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.6);
}

.workflow__step {
  display: flex;
  align-items: center;
  gap: 8px;

  > span {
    width: 23px;
    height: 23px;
    display: grid;
    place-items: center;
    border: 1px solid var(--color-border);
    border-radius: 6px;
    color: var(--color-text-muted);
    font-size: 9px;
    font-weight: 700;
  }

  div {
    display: flex;
    flex-direction: column;
    line-height: 1.3;
  }

  strong {
    color: var(--color-text-secondary);
    font-size: 11px;
    font-weight: 600;
  }

  small {
    color: var(--color-text-muted);
    font-size: 9px;
  }

  &.is-current {
    > span {
      border-color: var(--color-primary);
      background: var(--color-primary);
      color: #fff;
    }

    strong {
      color: var(--color-primary-strong);
    }
  }
}

.workflow__line {
  flex: 1;
  height: 1px;
  min-width: 12px;
  background: var(--color-border);
}

.page-alert {
  margin: 0;
}

.alert-message {
  margin: 0;
  font-size: 12px;
}

.alert-list {
  margin: 6px 0 0;
  padding-left: 18px;
  font-size: 12px;
  line-height: 1.7;
}

.alert-hint {
  margin: 5px 0 0;
  color: var(--color-text-secondary);
  font-size: 11px;
}

.parse-meta {
  color: var(--color-text-muted);
  font-size: 11px;
}

.save-success {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 10px 14px;
  border: 1px solid #bbf7d0;
  border-radius: 9px;
  background: var(--color-success-soft);
  color: var(--color-success);
  font-size: 12px;

  strong {
    font-size: 14px;
  }
}

.save-bar {
  position: sticky;
  bottom: 12px;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 16px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: var(--shadow-float);
  backdrop-filter: blur(12px);
}

.save-bar__summary {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--color-text-secondary);
  font-size: 12px;

  strong {
    color: var(--color-primary-strong);
    font-size: 15px;
  }
}

.save-bar__dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--color-success);
}

.save-bar__warning {
  color: var(--color-warning);
}

.save-bar__actions {
  display: flex;
  align-items: center;
  gap: 7px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 210px;
  padding: 30px 20px;
  border: 1px dashed var(--color-border-strong);
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.55);
  text-align: center;

  h3 {
    margin: 10px 0 4px;
    color: var(--color-text-secondary);
    font-size: 14px;
  }

  p {
    max-width: 430px;
    margin: 0;
    color: var(--color-text-muted);
    font-size: 12px;
    line-height: 1.7;
  }
}

.empty-state__art {
  width: 62px;
  height: 62px;
  display: grid;
  place-items: center;
  border-radius: 18px;
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

.empty-state__notes {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 6px;
  margin-top: 14px;

  span {
    padding: 3px 8px;
    border: 1px solid var(--color-border);
    border-radius: 999px;
    color: var(--color-text-muted);
    font-size: 10px;
  }
}

@media (max-width: 1100px) {
  .page-hero {
    align-items: flex-start;
    flex-direction: column;
    gap: 14px;
  }

  .page-hero__aside {
    width: 100%;
  }

  .hero-stat {
    flex: 1;
  }
}

@media (max-width: 760px) {
  .workflow {
    overflow-x: auto;
  }

  .workflow__step div {
    display: none;
  }

  .workflow__line {
    min-width: 24px;
  }

  .page-hero__aside {
    gap: 7px;
  }

  .hero-stat {
    padding: 8px 10px;
  }

  .hero-stat strong {
    font-size: 17px;
  }

  .save-bar {
    position: static;
    align-items: flex-start;
    flex-direction: column;
  }

  .save-bar__actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
