<script setup lang="ts">
import { computed } from 'vue'
import type { PracticeStatus } from '@/types/practice'

const props = defineProps<{
  status: PracticeStatus
}>()

const labels: Record<PracticeStatus, string> = {
  'not-started': '未开始',
  skeleton: '骨架已建立',
  planned: '待实现',
  ready: '已实现'
}

const label = computed(() => labels[props.status])
</script>

<template>
  <span class="wb-status" :class="`wb-status--${status}`">{{ label }}</span>
</template>
