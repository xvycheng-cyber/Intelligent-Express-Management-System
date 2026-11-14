<script setup lang="ts">
withDefaults(
  defineProps<{
    code: string
    language?: string
  }>(),
  { language: 'css' }
)
</script>

<template>
  <figure class="wb-code">
    <figcaption class="wb-code__caption">{{ language }}</figcaption>
    <pre><code>{{ code }}</code></pre>
  </figure>
</template>
