<script setup lang="ts">
import { computed } from 'vue'
import AppIcon from '@/components/common/AppIcon.vue'
import type { AddressVO } from '@/types/address'

const props = defineProps<{
  modelValue: boolean
  rows: AddressVO[]
  loading: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: []
}>()

const count = computed(() => props.rows.length)

function addressSummary(address: string): string {
  return address.length > 30 ? `${address.slice(0, 30)}…` : address
}

function close() {
  if (!props.loading) emit('update:modelValue', false)
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    width="min(620px, calc(100vw - 28px))"
    :close-on-click-modal="!loading"
    :close-on-press-escape="!loading"
    :show-close="!loading"
    align-center
    @update:model-value="emit('update:modelValue', $event)"
  >
    <template #header>
      <div class="batch-heading">
        <span class="batch-heading__icon"><AppIcon name="trash" :size="20" /></span>
        <div>
          <h3>确认批量删除</h3>
          <p>仅删除当前页已选中的地址，删除后可在地址簿中重新录入。</p>
        </div>
      </div>
    </template>

    <div class="batch-warning">
      将删除当前页选中的 <strong>{{ count }}</strong> 条记录，请确认收件人信息。
    </div>

    <div class="batch-list" data-testid="batch-delete-list">
      <div v-for="row in rows" :key="row.id" class="batch-item">
        <div class="batch-item__main">
          <strong>{{ row.receiverName }}</strong>
          <span class="batch-item__phone">{{ row.phone }}</span>
        </div>
        <div class="batch-item__address">{{ addressSummary(row.fullAddress) }}</div>
      </div>
      <el-empty v-if="rows.length === 0" description="没有选中记录" :image-size="60" />
    </div>

    <div class="batch-notice">
      <AppIcon name="info" :size="14" />
      只提交本次勾选的 {{ count }} 个 ID，不使用跨页选择。
    </div>

    <template #footer>
      <div class="batch-footer">
        <el-button :disabled="loading" @click="close">取消</el-button>
        <el-button
          type="danger"
          :loading="loading"
          :disabled="count === 0 || loading"
          data-testid="batch-delete-confirm"
          @click="emit('confirm')"
        >
          <AppIcon v-if="!loading" name="trash" :size="15" />
          确认删除（{{ count }}）
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
.batch-heading {
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
    background: var(--color-danger-soft);
    color: var(--color-danger);
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

.batch-warning {
  padding: 10px 12px;
  border-radius: 8px;
  background: #fff7ed;
  color: #9a3412;
  font-size: 12px;

  strong {
    font-size: 16px;
  }
}

.batch-list {
  display: flex;
  flex-direction: column;
  max-height: 230px;
  margin-top: 10px;
  overflow-y: auto;
  border: 1px solid var(--color-border);
  border-radius: 9px;
}

.batch-item {
  padding: 9px 12px;
  border-bottom: 1px solid var(--color-border);

  &:last-child {
    border-bottom: 0;
  }
}

.batch-item__main {
  display: flex;
  align-items: baseline;
  gap: 9px;

  strong {
    color: var(--color-text);
    font-size: 12px;
  }
}

.batch-item__phone {
  color: var(--color-text-muted);
  font-size: 11px;
  font-variant-numeric: tabular-nums;
}

.batch-item__address {
  margin-top: 2px;
  overflow: hidden;
  color: var(--color-text-secondary);
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.batch-notice {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 10px;
  color: var(--color-text-muted);
  font-size: 11px;
}

.batch-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
