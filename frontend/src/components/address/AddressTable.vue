<script setup lang="ts">
import { ref } from 'vue'
import AppIcon from '@/components/common/AppIcon.vue'
import type { AddressVO } from '@/types/address'

const props = defineProps<{
  rows: AddressVO[]
  loading: boolean
  current: number
  size: number
  searchActive: boolean
}>()

const emit = defineEmits<{
  'selection-change': [rows: AddressVO[]]
  edit: [row: AddressVO]
  delete: [row: AddressVO]
}>()

const tableRef = ref<{ clearSelection: () => void } | null>(null)

function handleSelectionChange(rows: AddressVO[]) {
  emit('selection-change', rows)
}

function rowIndex(index: number): number {
  return (props.current - 1) * props.size + index + 1
}

function clearSelection() {
  tableRef.value?.clearSelection()
}

defineExpose({ clearSelection })
</script>

<template>
  <section class="address-table-card panel-card">
    <el-table
      ref="tableRef"
      v-loading="loading"
      :data="rows"
      row-key="id"
      class="address-table"
      element-loading-text="正在加载地址..."
      data-testid="address-table"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="48" align="center" />

      <el-table-column label="#" width="62" align="center">
        <template #default="{ $index }">
          <span class="row-index">{{ rowIndex($index) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="联系人" min-width="128">
        <template #default="{ row }">
          <div class="contact-cell">
            <span class="contact-cell__avatar" aria-hidden="true">{{ row.receiverName.slice(0, 1) }}</span>
            <span class="contact-cell__name">{{ row.receiverName }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="联系电话" width="158">
        <template #default="{ row }">
          <span class="phone-cell">{{ row.phone }}</span>
        </template>
      </el-table-column>

      <el-table-column label="详细地址" min-width="320">
        <template #default="{ row }">
          <el-tooltip :content="row.fullAddress" placement="top-start" :show-after="350">
            <span class="address-cell">{{ row.fullAddress }}</span>
          </el-tooltip>
        </template>
      </el-table-column>

      <el-table-column label="创建时间" width="158">
        <template #default="{ row }">
          <span class="time-cell">{{ row.createTime }}</span>
        </template>
      </el-table-column>

      <el-table-column label="更新时间" width="158">
        <template #default="{ row }">
          <span class="time-cell">{{ row.updateTime }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="126" fixed="right" align="center">
        <template #default="{ row }">
          <div class="row-actions">
            <el-button link type="primary" size="small" data-testid="edit-address" @click="emit('edit', row)">
              <AppIcon name="edit" :size="14" />
              编辑
            </el-button>
            <el-button link type="danger" size="small" data-testid="delete-address" @click="emit('delete', row)">
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>

      <template #empty>
        <el-empty
          :description="searchActive ? '没有找到匹配的地址，试试其他关键词' : '地址簿还是空的，先新建一条地址吧'"
          :image-size="76"
        />
      </template>
    </el-table>
  </section>
</template>

<style scoped lang="scss">
.address-table-card {
  overflow: hidden;
}

.address-table {
  width: 100%;
}

.row-index {
  color: var(--color-text-muted);
  font-size: 11px;
  font-variant-numeric: tabular-nums;
}

.contact-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.contact-cell__avatar {
  width: 26px;
  height: 26px;
  flex: 0 0 26px;
  display: grid;
  place-items: center;
  border-radius: 7px;
  background: var(--color-primary-soft);
  color: var(--color-primary-strong);
  font-size: 11px;
  font-weight: 700;
}

.contact-cell__name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--color-text);
  font-weight: 600;
}

.phone-cell {
  color: var(--color-text-secondary);
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.02em;
}

.address-cell {
  display: -webkit-box;
  overflow: hidden;
  color: var(--color-text-secondary);
  line-height: 1.55;
  overflow-wrap: anywhere;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  cursor: default;
}

.time-cell {
  color: var(--color-text-muted);
  font-size: 11px;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

.row-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
  white-space: nowrap;
}

:deep(.el-table__empty-block) {
  min-height: 240px;
}

:deep(.el-table__body-wrapper) {
  overflow-x: auto;
}

@media (max-width: 760px) {
  :deep(.el-table__body-wrapper) {
    overflow-x: auto;
  }
}
</style>
