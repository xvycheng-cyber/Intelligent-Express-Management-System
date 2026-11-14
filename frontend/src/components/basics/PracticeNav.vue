<script setup lang="ts">
import { RouterLink, useRoute } from 'vue-router'
import PracticeStatus from '@/components/basics/PracticeStatus.vue'
import type { PracticeGroup } from '@/types/practice'

defineProps<{
  groups: PracticeGroup[]
}>()

const route = useRoute()

function isActive(target: string | undefined): boolean {
  if (!target) return false
  if (target.includes('#')) {
    return target === `${route.path}${route.hash}`
  }
  return target === route.path
}
</script>

<template>
  <nav class="wb-practice-nav" aria-label="实践章节导航" data-testid="practice-nav">
    <div class="wb-practice-nav__heading">
      <span class="wb-kicker">课程实践</span>
      <strong>2.1 — 2.19</strong>
    </div>

    <section v-for="group in groups" :id="`wb-group-${group.id}`" :key="group.id" class="wb-practice-nav__group">
      <div class="wb-practice-nav__group-label">
        <span>{{ group.label }}</span>
        <small>{{ group.range }}</small>
      </div>
      <ul class="wb-practice-nav__list">
        <li v-for="chapter in group.chapters" :key="chapter.number">
          <RouterLink
            v-if="chapter.to"
            class="wb-practice-nav__item"
            :class="{ 'is-active': isActive(chapter.to) }"
            :to="chapter.to"
            :data-testid="`practice-link-${chapter.number}`"
          >
            <span class="wb-practice-nav__number">{{ chapter.number }}</span>
            <span class="wb-practice-nav__title">{{ chapter.title }}</span>
            <PracticeStatus :status="chapter.status" />
          </RouterLink>
          <div v-else class="wb-practice-nav__item is-disabled">
            <span class="wb-practice-nav__number">{{ chapter.number }}</span>
            <span class="wb-practice-nav__title">{{ chapter.title }}</span>
            <PracticeStatus :status="chapter.status" />
          </div>
        </li>
      </ul>
    </section>

    <div class="wb-practice-nav__foot">
      <span>Servlet 映射演示</span>
      <PracticeStatus status="planned" />
    </div>
  </nav>
</template>
