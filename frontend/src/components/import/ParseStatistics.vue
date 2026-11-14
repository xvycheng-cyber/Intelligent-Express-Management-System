<script setup lang="ts">
import AppIcon from '@/components/common/AppIcon.vue'

defineProps<{
  total: number
  valid: number
  invalid: number
  edited: number
  submittable: number
}>()
</script>

<template>
  <section class="stats" aria-label="解析统计">
    <article class="stat-card stat-card--total">
      <div class="stat-card__icon"><AppIcon name="inbox" :size="18" /></div>
      <div>
        <span class="stat-card__label">总记录</span>
        <strong class="stat-card__value">{{ total }}</strong>
      </div>
      <span class="stat-card__hint">本次识别行数</span>
    </article>

    <article class="stat-card stat-card--valid">
      <div class="stat-card__icon"><AppIcon name="check" :size="18" /></div>
      <div>
        <span class="stat-card__label">有效记录</span>
        <strong class="stat-card__value">{{ valid }}</strong>
      </div>
      <span class="stat-card__hint">解析状态 VALID</span>
    </article>

    <article class="stat-card stat-card--invalid">
      <div class="stat-card__icon"><AppIcon name="info" :size="18" /></div>
      <div>
        <span class="stat-card__label">无效记录</span>
        <strong class="stat-card__value">{{ invalid }}</strong>
      </div>
      <span class="stat-card__hint">需要人工修正</span>
    </article>

    <article class="stat-card stat-card--edited">
      <div class="stat-card__icon"><AppIcon name="edit" :size="18" /></div>
      <div>
        <span class="stat-card__label">人工修改</span>
        <strong class="stat-card__value">{{ edited }}</strong>
      </div>
      <span class="stat-card__hint">当前值与原始值不同</span>
    </article>

    <article class="stat-card stat-card--submit">
      <div class="stat-card__icon"><AppIcon name="save" :size="18" /></div>
      <div>
        <span class="stat-card__label">可提交</span>
        <strong class="stat-card__value">{{ submittable }}</strong>
      </div>
      <span class="stat-card__hint">通过前端完整校验</span>
    </article>
  </section>
</template>

<style scoped lang="scss">
.stats {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.stat-card {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 11px;
  min-height: 94px;
  padding: 16px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-base);
  background: var(--color-surface);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.03);

  &::after {
    content: '';
    position: absolute;
    right: -18px;
    bottom: -24px;
    width: 74px;
    height: 74px;
    border-radius: 50%;
    background: currentColor;
    opacity: 0.05;
  }
}

.stat-card__icon {
  width: 32px;
  height: 32px;
  flex: 0 0 32px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: var(--color-surface-soft);
  color: var(--color-text-secondary);
}

.stat-card__label {
  display: block;
  color: var(--color-text-muted);
  font-size: 11px;
}

.stat-card__value {
  display: block;
  margin-top: 1px;
  color: var(--color-text);
  font-size: 24px;
  line-height: 1.2;
  letter-spacing: -0.03em;
}

.stat-card__hint {
  position: absolute;
  left: 16px;
  bottom: 12px;
  color: var(--color-text-muted);
  font-size: 10px;
}

.stat-card--valid {
  .stat-card__icon {
    background: var(--color-success-soft);
    color: var(--color-success);
  }

  .stat-card__value {
    color: var(--color-success);
  }
}

.stat-card--invalid {
  .stat-card__icon {
    background: var(--color-danger-soft);
    color: var(--color-danger);
  }

  .stat-card__value {
    color: var(--color-danger);
  }
}

.stat-card--edited {
  .stat-card__icon {
    background: #eef2ff;
    color: #4338ca;
  }
}

.stat-card--submit {
  border-color: #bae6fd;
  background: linear-gradient(150deg, #f0f9ff, #fff);

  .stat-card__icon {
    background: var(--color-primary-soft);
    color: var(--color-primary);
  }

  .stat-card__value {
    color: var(--color-primary-strong);
  }
}

@media (max-width: 1180px) {
  .stats {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 680px) {
  .stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .stat-card {
    min-height: 88px;
  }
}
</style>
