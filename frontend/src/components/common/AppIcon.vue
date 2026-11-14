<script setup lang="ts">
withDefaults(
  defineProps<{
    name: 'edit' | 'list' | 'spark' | 'save' | 'restore' | 'info' | 'check' | 'inbox' | 'chevron' | 'plus' | 'search' | 'refresh' | 'trash' | 'book' | 'route'
    size?: number
  }>(),
  { size: 18 }
)
</script>

<template>
  <svg
    class="app-icon"
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.8"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <template v-if="name === 'edit'">
      <path d="M4 20h4l10-10a2.8 2.8 0 0 0-4-4L4 16v4Z" />
      <path d="m13.5 6.5 4 4" />
    </template>
    <template v-else-if="name === 'list'">
      <path d="M8 6h12M8 12h12M8 18h12" />
      <path d="M4 6h.01M4 12h.01M4 18h.01" />
    </template>
    <template v-else-if="name === 'spark'">
      <path d="m12 3 1.4 4.6L18 9l-4.6 1.4L12 15l-1.4-4.6L6 9l4.6-1.4L12 3Z" />
      <path d="m19 15 .8 2.2L22 18l-2.2.8L19 21l-.8-2.2L16 18l2.2-.8L19 15Z" />
    </template>
    <template v-else-if="name === 'save'">
      <path d="M5 4h11l3 3v13H5V4Z" />
      <path d="M8 4v5h7V4M8 15h8" />
    </template>
    <template v-else-if="name === 'restore'">
      <path d="M4 10a8 8 0 1 1 2.3 5.7" />
      <path d="M4 4v6h6" />
    </template>
    <template v-else-if="name === 'info'">
      <circle cx="12" cy="12" r="9" />
      <path d="M12 10v6M12 7h.01" />
    </template>
    <template v-else-if="name === 'check'">
      <path d="m5 12 4 4L19 6" />
    </template>
    <template v-else-if="name === 'inbox'">
      <path d="M4 5h16v14H4V5Z" />
      <path d="M4 13h4l2 3h4l2-3h4" />
    </template>
    <template v-else-if="name === 'chevron'">
      <path d="m9 6 6 6-6 6" />
    </template>
    <template v-else-if="name === 'plus'">
      <path d="M12 5v14M5 12h14" />
    </template>
    <template v-else-if="name === 'search'">
      <circle cx="10.5" cy="10.5" r="6.5" />
      <path d="m16 16 4 4" />
    </template>
    <template v-else-if="name === 'refresh'">
      <path d="M20 11a8 8 0 1 0-2.3 6" />
      <path d="M20 4v7h-7" />
    </template>
    <template v-else-if="name === 'trash'">
      <path d="M4 7h16M9 7V4h6v3M7 7l1 13h8l1-13M10 11v5M14 11v5" />
    </template>
    <template v-else-if="name === 'book'">
      <path d="M4 5.5A2.5 2.5 0 0 1 6.5 3H20v16H6.5A2.5 2.5 0 0 0 4 21.5V5.5Z" />
      <path d="M4 18.5A2.5 2.5 0 0 1 6.5 16H20M8 7h8M8 11h6" />
    </template>
    <template v-else-if="name === 'route'">
      <circle cx="6" cy="18" r="2.5" />
      <circle cx="18" cy="6" r="2.5" />
      <path d="M8.5 18H14a4 4 0 0 0 4-4V8.5" />
    </template>
  </svg>
</template>

<style scoped>
.app-icon {
  display: inline-block;
  flex: 0 0 auto;
  vertical-align: -0.18em;
}
</style>
