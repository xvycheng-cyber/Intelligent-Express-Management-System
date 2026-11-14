<template>
  <section class="not-found">
    <h2>页面不存在</h2>
    <p>请返回 <RouterLink to="/import">批量地址录入</RouterLink>。</p>
  </section>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<style scoped>
.not-found {
  display: grid;
  place-items: center;
  gap: 8px;
  padding: 64px 0;
  color: var(--color-text-secondary);
}
</style>
