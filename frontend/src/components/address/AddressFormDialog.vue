<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import AppIcon from '@/components/common/AppIcon.vue'
import type { AddressSavePayload, AddressVO } from '@/types/address'
import { validateAddress, validateName, validatePhone } from '@/utils/addressValidation'
import { codePointLength } from '@/utils/textLength'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  address: AddressVO | null
  submitting: boolean
  serverError: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  save: [payload: AddressSavePayload]
}>()

const formRef = ref<{ clearValidate?: () => void } | null>(null)
const form = reactive({
  receiverName: '',
  phone: '',
  fullAddress: ''
})
const errors = reactive<Record<'receiverName' | 'phone' | 'fullAddress', string>>({
  receiverName: '',
  phone: '',
  fullAddress: ''
})

const title = computed(() => (props.mode === 'create' ? '新建地址' : '编辑地址'))
const subtitle = computed(() =>
  props.mode === 'create'
    ? '手动补充一条收件地址，保存后立即出现在地址簿。'
    : '修改后会直接更新当前地址记录。'
)
const nameCount = computed(() => codePointLength(form.receiverName.trim()))
const addressCount = computed(() => form.fullAddress.trim().length)

function resetForm() {
  form.receiverName = props.address?.receiverName ?? ''
  form.phone = props.address?.phone ?? ''
  form.fullAddress = props.address?.fullAddress ?? ''
  errors.receiverName = ''
  errors.phone = ''
  errors.fullAddress = ''
  formRef.value?.clearValidate?.()
}

watch(
  () => [props.modelValue, props.address] as const,
  ([visible]) => {
    if (visible) resetForm()
  },
  { immediate: true }
)

function validate(): boolean {
  errors.receiverName = validateName(form.receiverName) ?? ''
  errors.phone = validatePhone(form.phone) ?? ''
  errors.fullAddress = validateAddress(form.fullAddress) ?? ''
  return !errors.receiverName && !errors.phone && !errors.fullAddress
}

function handleSave() {
  if (props.submitting) return
  if (!validate()) return
  emit('save', {
    receiverName: form.receiverName.trim(),
    phone: form.phone.trim(),
    fullAddress: form.fullAddress.trim()
  })
}

function close() {
  if (!props.submitting) emit('update:modelValue', false)
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    width="min(580px, calc(100vw - 28px))"
    :close-on-click-modal="!submitting"
    :close-on-press-escape="!submitting"
    :show-close="!submitting"
    align-center
    @update:model-value="emit('update:modelValue', $event)"
  >
    <template #header>
      <div class="form-heading">
        <span class="form-heading__icon"><AppIcon :name="mode === 'create' ? 'plus' : 'edit'" :size="19" /></span>
        <div>
          <h3>{{ title }}</h3>
          <p>{{ subtitle }}</p>
        </div>
      </div>
    </template>

    <el-alert
      v-if="serverError"
      type="error"
      :closable="false"
      show-icon
      :title="serverError"
      class="form-error"
      data-testid="address-form-error"
    />

    <el-form ref="formRef" label-position="top" class="address-form" @submit.prevent>
      <el-form-item label="联系人姓名" required :error="errors.receiverName">
        <el-input
          v-model="form.receiverName"
          size="large"
          placeholder="例如：菜小鸟"
          aria-label="联系人姓名"
          data-testid="address-form-name"
          @input="errors.receiverName = ''"
        />
        <div class="input-counter" :class="{ 'is-over': nameCount > 50 }">{{ nameCount }} / 50 码点</div>
      </el-form-item>

      <el-form-item label="联系电话" required :error="errors.phone">
        <el-input
          v-model="form.phone"
          size="large"
          placeholder="手机、座机、+86 或脱敏号码"
          aria-label="联系电话"
          data-testid="address-form-phone"
          @input="errors.phone = ''"
        />
      </el-form-item>

      <el-form-item label="详细地址" required :error="errors.fullAddress">
        <el-input
          v-model="form.fullAddress"
          type="textarea"
          :rows="4"
          resize="vertical"
          placeholder="省 / 市 / 区 / 小区 / 楼栋 / 门牌"
          aria-label="详细地址"
          data-testid="address-form-address"
          @input="errors.fullAddress = ''"
        />
        <div class="input-counter" :class="{ 'is-over': addressCount > 255 }">{{ addressCount }} / 255</div>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="form-footer">
        <el-button :disabled="submitting" @click="close">取消</el-button>
        <el-button type="primary" :loading="submitting" data-testid="address-form-submit" @click="handleSave">
          <AppIcon v-if="!submitting" name="save" :size="16" />
          {{ mode === 'create' ? '保存地址' : '保存修改' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
.form-heading {
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

.form-error {
  margin-bottom: 13px;
}

.address-form {
  margin-top: 4px;

  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-form-item__label) {
    color: var(--color-text-secondary);
    font-weight: 600;
  }
}

.input-counter {
  width: 100%;
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

.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
