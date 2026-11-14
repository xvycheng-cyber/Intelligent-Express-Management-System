<script setup lang="ts">
defineProps<{
  number: string
  title: string
  target: string
}>()
</script>

<template>
  <article class="wb-demo-section" :data-chapter="number" :data-testid="`demo-section-${number}`">
    <header class="wb-demo-section__head">
      <span class="wb-demo-section__number">{{ number }}</span>
      <div>
        <h3>{{ title }}</h3>
        <p>{{ target }}</p>
      </div>
    </header>
    <div class="wb-demo-section__body">
      <div class="wb-demo-section__slot">
        <slot name="demo">
          <p class="wb-empty-inline">演示将在后续 MS-06.3 起实现。</p>
        </slot>
      </div>
      <div class="wb-demo-section__grid">
        <div class="wb-demo-section__block">
          <h4>观察结论</h4>
          <slot name="conclusion">
            <p class="wb-empty-inline">待对应阶段补充。</p>
          </slot>
        </div>
        <div class="wb-demo-section__block">
          <h4>关键代码</h4>
          <slot name="code">
            <p class="wb-empty-inline">待对应阶段补充。</p>
          </slot>
        </div>
      </div>
    </div>
  </article>
</template>
