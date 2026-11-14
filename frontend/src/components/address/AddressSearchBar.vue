<script setup lang="ts">
import { computed } from 'vue'
import AppIcon from '@/components/common/AppIcon.vue'
import { codePointLength } from '@/utils/textLength'

const props = defineProps<{
  modelValue: string
  loading: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  search: []
  reset: []
}>()

const trimmedKeyword = computed(() => props.modelValue.trim())
const keywordLength = computed(() => codePointLength(trimmedKeyword.value))
const keywordTooLong = computed(() => keywordLength.value > 50)

function handleSearch() {
  if (keywordTooLong.value || props.loading) return
  emit('search')
}

function handleReset() {
  if (props.loading) return
  emit('reset')
}
</script>

<template>
  <section class="search-bar panel-card" aria-label="地址搜索">
    <div class="search-bar__copy">
      <div class="search-bar__title">
        <span class="search-bar__icon"><AppIcon name="search" :size="17" /></span>
        <span>搜索地址簿</span>
      </div>
      <p>姓名、电话或详细地址，可组合分页使用；特殊字符按字面搜索。</p>
    </div>

    <div class="search-bar__control">
      <el-input
        :model-value="modelValue"
        clearable
        size="large"
        placeholder="输入姓名、电话或地址关键词"
        aria-label="搜索关键词"
        data-testid="address-search-input"
        @update:model-value="emit('update:modelValue', $event)"
        @keyup.enter="handleSearch"
        @clear="handleReset"
      >
        <template #prefix><AppIcon name="search" :size="16" /></template>
      </el-input>
      <div class="search-bar__count" :class="{ 'is-over': keywordTooLong }">
        {{ keywordLength }} / 50 码点
      </div>
      <el-button type="primary" size="large" :loading="loading" :disabled="keywordTooLong" data-testid="address-search-button" @click="handleSearch">
        搜索
      </el-button>
      <el-button size="large" :disabled="loading" data-testid="address-reset-button" @click="handleReset">
        重置
      </el-button>
    </div>

    <el-alert
      v-if="keywordTooLong"
      class="search-bar__alert"
      type="warning"
      :closable="false"
      show-icon
      title="关键词最多 50 个 Unicode 码点"
      description="当前关键词超出前端限制，请缩短后再搜索。后端仍会独立校验。"
    />
  </section>
</template>

<style scoped lang="scss">
.search-bar {
  display: flex;
  flex-direction: column;
  gap: 13px;
}

.search-bar__copy {
  display: flex;
  align-items: baseline;
  gap: 14px;
}

.search-bar__title {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: var(--color-text);
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.search-bar__icon {
  width: 26px;
  height: 26px;
  display: grid;
  place-items: center;
  border-radius: 7px;
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

.search-bar__copy p {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 11px;
}

.search-bar__control {
  display: flex;
  align-items: center;
  gap: 9px;
  max-width: 800px;
  position: relative;
}

.search-bar__control :deep(.el-input) {
  max-width: 470px;
}

.search-bar__count {
  position: absolute;
  right: 196px;
  bottom: -16px;
  color: var(--color-text-muted);
  font-size: 10px;
  white-space: nowrap;

  &.is-over {
    color: var(--color-danger);
    font-weight: 600;
  }
}

.search-bar__alert {
  max-width: 800px;
}

@media (max-width: 800px) {
  .search-bar__copy {
    align-items: flex-start;
    flex-direction: column;
    gap: 4px;
  }

  .search-bar__control {
    flex-wrap: wrap;
  }

  .search-bar__control :deep(.el-input) {
    max-width: none;
    width: 100%;
    flex: 1 0 100%;
  }

  .search-bar__count {
    position: static;
    order: 3;
    width: 100%;
  }
}
</style>
