<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppIcon from '@/components/common/AppIcon.vue'
import AddressSearchBar from '@/components/address/AddressSearchBar.vue'
import AddressTable from '@/components/address/AddressTable.vue'
import AddressFormDialog from '@/components/address/AddressFormDialog.vue'
import BatchDeleteDialog from '@/components/address/BatchDeleteDialog.vue'
import {
  batchDeleteAddresses,
  createAddress,
  deleteAddress,
  getAddress,
  listAddresses,
  updateAddress
} from '@/api/addresses'
import { ApiError } from '@/api/request'
import type { AddressSavePayload, AddressVO } from '@/types/address'
import { codePointLength } from '@/utils/textLength'

const rows = ref<AddressVO[]>([])
const total = ref(0)
const current = ref(1)
const size = ref(10)
const pages = ref(0)
const keywordInput = ref('')
const appliedKeyword = ref('')
const selectedRows = ref<AddressVO[]>([])
const tableRef = ref<InstanceType<typeof AddressTable> | null>(null)

const loading = ref(false)
const initialLoading = ref(true)
const listError = ref('')
let requestSequence = 0

const formVisible = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const editingAddress = ref<AddressVO | null>(null)
const formSubmitting = ref(false)
const formError = ref('')
const detailLoading = ref(false)

const batchVisible = ref(false)
const batchDeleting = ref(false)
const deleteLoading = ref(false)

const searchActive = computed(() => appliedKeyword.value.length > 0)
const selectedCount = computed(() => selectedRows.value.length)
const selectedNames = computed(() =>
  selectedRows.value.map((row) => row.receiverName).join('、')
)

function errorMessage(error: unknown, fallback: string): string {
  return error instanceof ApiError ? error.message : fallback
}

function clearSelection() {
  selectedRows.value = []
  tableRef.value?.clearSelection()
}

async function loadAddresses() {
  const sequence = ++requestSequence
  loading.value = true
  listError.value = ''
  clearSelection()
  try {
    const data = await listAddresses({
      page: current.value,
      size: size.value,
      keyword: appliedKeyword.value || undefined
    })
    if (sequence !== requestSequence) return
    rows.value = data.records
    total.value = data.total
    current.value = data.current
    size.value = data.size
    pages.value = data.pages
    clearSelection()
  } catch (error) {
    if (sequence !== requestSequence) return
    rows.value = []
    total.value = 0
    pages.value = 0
    listError.value = errorMessage(error, '地址列表加载失败，请稍后重试')
    clearSelection()
  } finally {
    if (sequence === requestSequence) {
      loading.value = false
      initialLoading.value = false
    }
  }
}

async function reloadAfterMutation() {
  clearSelection()
  await loadAddresses()
  if (rows.value.length === 0 && total.value > 0 && current.value > 1) {
    current.value -= 1
    await loadAddresses()
  }
}

function handleSelectionChange(selection: AddressVO[]) {
  selectedRows.value = selection
}

function handleSearch() {
  const keyword = keywordInput.value.trim()
  if (codePointLength(keyword) > 50) {
    ElMessage.warning('关键词最多 50 个 Unicode 码点')
    return
  }
  appliedKeyword.value = keyword
  current.value = 1
  clearSelection()
  loadAddresses()
}

function handleReset() {
  keywordInput.value = ''
  appliedKeyword.value = ''
  current.value = 1
  clearSelection()
  loadAddresses()
}

function handlePageChange(next: number) {
  if (next === current.value || loading.value) return
  current.value = next
  clearSelection()
  loadAddresses()
}

function handleSizeChange(next: number) {
  if (next === size.value || loading.value) return
  size.value = next
  current.value = 1
  clearSelection()
  loadAddresses()
}

function openCreate() {
  formMode.value = 'create'
  editingAddress.value = null
  formError.value = ''
  formVisible.value = true
}

async function openEdit(row: AddressVO) {
  if (detailLoading.value) return
  detailLoading.value = true
  formError.value = ''
  try {
    const latest = await getAddress(row.id)
    formMode.value = 'edit'
    editingAddress.value = latest
    formVisible.value = true
  } catch (error) {
    ElMessage.error(errorMessage(error, '读取地址详情失败'))
    if (error instanceof ApiError && error.code === 40401) {
      await reloadAfterMutation()
    }
  } finally {
    detailLoading.value = false
  }
}

async function handleFormSave(payload: AddressSavePayload) {
  if (formSubmitting.value) return
  formSubmitting.value = true
  formError.value = ''
  try {
    if (formMode.value === 'create') {
      await createAddress(payload)
      ElMessage.success('地址新建成功')
    } else if (editingAddress.value) {
      await updateAddress(editingAddress.value.id, payload)
      ElMessage.success('地址修改成功')
    }
    formVisible.value = false
    await reloadAfterMutation()
  } catch (error) {
    formError.value = errorMessage(error, '保存失败，请稍后重试')
    if (error instanceof ApiError && error.code === 40401) {
      ElMessage.warning('该记录不存在或已被删除')
    }
  } finally {
    formSubmitting.value = false
  }
}

function maskPhone(phone: string): string {
  if (phone.length <= 7) return phone
  const head = phone.slice(0, 3)
  const tail = phone.slice(-4)
  return `${head}${'*'.repeat(Math.max(1, phone.length - 7))}${tail}`
}

function addressSummary(address: string): string {
  return address.length > 48 ? `${address.slice(0, 48)}…` : address
}

function confirmSingleDelete(row: AddressVO) {
  return ElMessageBox.confirm(
    h('div', { class: 'delete-confirm' }, [
      h('p', `确认删除「${row.receiverName}」的地址记录？`),
      h('p', `联系电话：${maskPhone(row.phone)}`),
      h('p', `地址摘要：${addressSummary(row.fullAddress)}`),
      h('p', { class: 'delete-confirm__hint' }, '删除为逻辑删除，删除后列表中将不再显示。')
    ]),
    '删除地址',
    {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger'
    }
  )
}

async function handleDelete(row: AddressVO) {
  if (deleteLoading.value) return
  try {
    await confirmSingleDelete(row)
  } catch {
    return
  }
  deleteLoading.value = true
  try {
    await deleteAddress(row.id)
    ElMessage.success('地址删除成功')
    await reloadAfterMutation()
  } catch (error) {
    ElMessage.error(errorMessage(error, '删除失败，请稍后重试'))
    if (error instanceof ApiError && error.code === 40401) {
      await reloadAfterMutation()
    }
  } finally {
    deleteLoading.value = false
  }
}

function openBatchDelete() {
  if (selectedCount.value === 0) {
    ElMessage.warning('请先选择当前页的记录')
    return
  }
  batchVisible.value = true
}

async function handleBatchDelete() {
  if (batchDeleting.value) return
  const ids = selectedRows.value.map((row) => row.id)
  if (ids.length === 0) {
    batchVisible.value = false
    return
  }
  batchDeleting.value = true
  try {
    const result = await batchDeleteAddresses(ids)
    batchVisible.value = false
    ElMessage.success(`实际删除 ${result.deletedCount} 条地址`)
    await reloadAfterMutation()
  } catch (error) {
    ElMessage.error(errorMessage(error, '批量删除失败，请稍后重试'))
  } finally {
    batchDeleting.value = false
  }
}

onMounted(loadAddresses)
</script>

<template>
  <section class="address-page" v-loading="initialLoading">
    <header class="page-hero">
      <div class="page-hero__copy">
        <div class="eyebrow"><span></span>MS-05 · 地址簿管理</div>
        <h1>收件地址簿</h1>
        <p>管理已确认的收件地址，支持姓名、电话、地址搜索、分页维护和批量整理。</p>
      </div>
      <el-button type="primary" size="large" data-testid="create-address" @click="openCreate">
        <AppIcon name="plus" :size="17" />
        新建地址
      </el-button>
    </header>

    <div class="metrics">
      <article class="metric-card">
        <span class="metric-card__label">地址总量</span>
        <strong class="metric-card__value" data-testid="address-total">{{ total }}</strong>
        <span class="metric-card__hint">来自后端分页 total</span>
      </article>
      <article class="metric-card">
        <span class="metric-card__label">当前页数量</span>
        <strong class="metric-card__value">{{ rows.length }}</strong>
        <span class="metric-card__hint">第 {{ current }} / {{ pages || 1 }} 页</span>
      </article>
      <article class="metric-card metric-card--selected">
        <span class="metric-card__label">当前选中</span>
        <strong class="metric-card__value" data-testid="selected-count">{{ selectedCount }}</strong>
        <span class="metric-card__hint">仅当前页有效</span>
      </article>
      <div class="metric-card metric-card--actions">
        <span>批量整理当前页</span>
        <el-button
          type="danger"
          plain
          :disabled="selectedCount === 0 || loading"
          data-testid="batch-delete-open"
          @click="openBatchDelete"
        >
          <AppIcon name="trash" :size="15" />
          批量删除
        </el-button>
      </div>
    </div>

    <AddressSearchBar
      v-model="keywordInput"
      :loading="loading"
      @search="handleSearch"
      @reset="handleReset"
    />

    <el-alert
      v-if="listError"
      type="error"
      :closable="false"
      show-icon
      title="地址加载失败"
      :description="listError"
      class="list-alert"
      data-testid="address-list-error"
    />

    <section class="table-section">
      <div class="table-toolbar">
        <div class="table-toolbar__left">
          <span class="table-toolbar__title">地址记录</span>
          <span v-if="searchActive" class="search-chip">
            搜索：{{ appliedKeyword }}
            <button type="button" aria-label="清除搜索" @click="handleReset">×</button>
          </span>
          <span v-if="selectedCount" class="selection-text">
            已选 {{ selectedCount }} 项
            <button type="button" @click="clearSelection">清空选择</button>
          </span>
        </div>
        <div class="table-toolbar__right">
          <el-button text :disabled="loading" data-testid="address-refresh" @click="reloadAfterMutation">
            <AppIcon name="refresh" :size="15" />
            刷新
          </el-button>
        </div>
      </div>

      <AddressTable
        ref="tableRef"
        :rows="rows"
        :loading="loading"
        :current="current"
        :size="size"
        :search-active="searchActive"
        @selection-change="handleSelectionChange"
        @edit="openEdit"
        @delete="handleDelete"
      />
    </section>

    <div class="pagination-bar">
      <span class="pagination-bar__total">共 {{ total }} 条 · 每页 {{ size }} 条</span>
      <el-pagination
        :current-page="current"
        :page-size="size"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="sizes, prev, pager, next, jumper"
        background
        :disabled="loading"
        data-testid="address-pagination"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <AddressFormDialog
      v-model="formVisible"
      :mode="formMode"
      :address="editingAddress"
      :submitting="formSubmitting"
      :server-error="formError"
      @save="handleFormSave"
    />

    <BatchDeleteDialog
      v-model="batchVisible"
      :rows="selectedRows"
      :loading="batchDeleting"
      @confirm="handleBatchDelete"
    />

    <span class="sr-only" aria-live="polite">
      {{ selectedCount ? `已选择 ${selectedCount} 条：${selectedNames}` : '' }}
    </span>
  </section>
</template>

<style scoped lang="scss">
.address-page {
  display: flex;
  flex-direction: column;
  gap: 17px;
}

.page-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 22px;
  padding: 7px 2px 3px;
}

.page-hero__copy {
  max-width: 680px;

  h1 {
    margin: 6px 0 7px;
    color: var(--color-text);
    font-size: clamp(25px, 2.2vw, 32px);
    line-height: 1.22;
    letter-spacing: -0.035em;
  }

  p {
    margin: 0;
    color: var(--color-text-secondary);
    font-size: 13px;
    line-height: 1.7;
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

.metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr)) minmax(180px, 0.9fr);
  gap: 11px;
}

.metric-card {
  position: relative;
  min-height: 86px;
  padding: 13px 15px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-base);
  background: var(--color-surface);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.03);

  &::after {
    content: '';
    position: absolute;
    right: -20px;
    bottom: -28px;
    width: 78px;
    height: 78px;
    border-radius: 50%;
    background: var(--color-primary);
    opacity: 0.04;
  }
}

.metric-card__label {
  display: block;
  color: var(--color-text-muted);
  font-size: 10px;
}

.metric-card__value {
  display: block;
  margin-top: 1px;
  color: var(--color-text);
  font-size: 24px;
  line-height: 1.2;
  letter-spacing: -0.03em;
  font-variant-numeric: tabular-nums;
}

.metric-card__hint {
  display: block;
  margin-top: 2px;
  color: var(--color-text-muted);
  font-size: 10px;
}

.metric-card--selected {
  border-color: #bae6fd;
  background: linear-gradient(150deg, #f0f9ff, #fff);

  .metric-card__value {
    color: var(--color-primary-strong);
  }
}

.metric-card--actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;

  span {
    color: var(--color-text-secondary);
    font-size: 11px;
  }
}

.list-alert {
  margin: 0;
}

.table-section {
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 13px 16px;
  border-bottom: 1px solid var(--color-border);
}

.table-toolbar__left,
.table-toolbar__right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.table-toolbar__title {
  color: var(--color-text);
  font-size: 13px;
  font-weight: 700;
}

.search-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  max-width: 300px;
  padding: 2px 7px;
  overflow: hidden;
  border-radius: 5px;
  background: var(--color-primary-soft);
  color: var(--color-primary-strong);
  font-size: 10px;
  text-overflow: ellipsis;
  white-space: nowrap;

  button {
    border: 0;
    background: transparent;
    color: inherit;
    cursor: pointer;
    font-size: 13px;
    line-height: 1;
  }
}

.selection-text {
  color: var(--color-primary-strong);
  font-size: 11px;

  button {
    margin-left: 5px;
    border: 0;
    background: transparent;
    color: var(--color-primary);
    cursor: pointer;
    font-size: 11px;
    text-decoration: underline;
  }
}

.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 2px 2px 0;
}

.pagination-bar__total {
  color: var(--color-text-muted);
  font-size: 11px;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

:deep(.delete-confirm p) {
  margin: 0 0 7px;
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.55;
}

:deep(.delete-confirm__hint) {
  margin-top: 10px !important;
  padding-top: 8px;
  border-top: 1px dashed var(--color-border);
  color: var(--color-text-muted) !important;
  font-size: 11px !important;
}

@media (max-width: 1050px) {
  .metrics {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .metric-card--actions {
    grid-column: 1 / -1;
    min-height: auto;
  }
}

@media (max-width: 760px) {
  .page-hero {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
  }

  .page-hero :deep(.el-button) {
    width: 100%;
  }

  .metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .metric-card--actions {
    grid-column: 1 / -1;
  }

  .pagination-bar {
    align-items: flex-start;
    flex-direction: column;
    width: 100%;
  }

  .pagination-bar :deep(.el-pagination) {
    width: 100%;
    flex-wrap: wrap;
    justify-content: flex-start;
    row-gap: 7px;
  }

  .pagination-bar :deep(.el-pagination__jump) {
    margin-left: 0;
  }

  .table-toolbar {
    align-items: flex-start;
    flex-direction: column;
    gap: 7px;
  }
}
</style>
