<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import AppIcon from '@/components/common/AppIcon.vue'
import { getHealth } from '@/api/health'

const route = useRoute()
const activeMenu = computed(() => {
  if (route.path.startsWith('/import')) return 'import'
  if (route.path.startsWith('/addresses')) return 'addresses'
  return ''
})
const pageTitle = computed(() => (route.meta.title as string | undefined) ?? '批量地址录入')
const pageStage = computed(() =>
  route.path.startsWith('/addresses') ? 'MS-05 · 地址簿管理' : 'MS-04 · 批量地址录入'
)
const backendState = ref<'checking' | 'up' | 'down'>('checking')

onMounted(async () => {
  try {
    const health = await getHealth()
    backendState.value = health.status === 'UP' ? 'up' : 'down'
  } catch {
    backendState.value = 'down'
  }
})
</script>

<template>
  <div class="app-shell">
    <a class="skip-link" href="#main-content">跳到主要内容</a>

    <aside class="sidebar" aria-label="产品导航">
      <div class="brand">
        <span class="brand__mark" aria-hidden="true">快</span>
        <div class="brand__copy">
          <strong>快递通</strong>
          <span>Express Address</span>
        </div>
      </div>

      <div class="sidebar__section-label">工作台</div>
      <nav class="side-nav" aria-label="主导航">
        <RouterLink
          class="side-nav__item"
          :class="{ 'is-active': activeMenu === 'import' }"
          to="/import"
        >
          <AppIcon name="edit" :size="18" />
          <span>批量地址录入</span>
        </RouterLink>
        <RouterLink
          class="side-nav__item"
          :class="{ 'is-active': activeMenu === 'addresses' }"
          to="/addresses"
        >
          <AppIcon name="list" :size="18" />
          <span>地址簿管理</span>
        </RouterLink>
      </nav>

      <div class="sidebar__footer">
        <div class="sidebar__footer-line">
          <span class="status-dot" aria-hidden="true"></span>
          <span>本机演示环境</span>
        </div>
        <span class="sidebar__hint">127.0.0.1 · 课程设计</span>
      </div>
    </aside>

    <div class="workspace">
      <header class="topbar">
        <div class="topbar__crumb">
          <span>工作台</span>
          <AppIcon name="chevron" :size="14" />
          <strong>{{ pageTitle }}</strong>
        </div>
        <div class="topbar__right">
          <span class="topbar__pill" :class="`is-${backendState}`">
            <span class="status-dot" aria-hidden="true"></span>
            {{ backendState === 'up' ? '服务已连接' : backendState === 'down' ? '服务未连接' : '连接中' }}
          </span>
          <span class="topbar__user">本机演示</span>
        </div>
      </header>

      <main id="main-content" class="app-main">
        <slot />
      </main>

      <footer class="app-footer">
        <span>Intelligent-Express-Management-System</span>
        <span>{{ pageStage }}</span>
      </footer>
    </div>
  </div>
</template>

<style scoped lang="scss">
.app-shell {
  min-height: 100vh;
  display: flex;
  background: var(--color-bg);
}

.skip-link {
  position: fixed;
  top: -48px;
  left: 16px;
  z-index: 100;
  padding: 10px 14px;
  border-radius: 8px;
  background: var(--color-primary);
  color: #fff;
  text-decoration: none;
  transition: top 0.2s ease;

  &:focus {
    top: 16px;
  }
}

.sidebar {
  width: 236px;
  flex: 0 0 236px;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 24px 16px 18px;
  background: var(--color-sidebar);
  color: #dbe7f5;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 8px;
  margin-bottom: 36px;

  &__mark {
    width: 40px;
    height: 40px;
    border-radius: 12px;
    display: grid;
    place-items: center;
    color: #fff;
    font-weight: 800;
    background: linear-gradient(145deg, #0ea5e9, #0369a1);
    box-shadow: 0 8px 18px rgba(2, 132, 199, 0.26);
  }

  &__copy {
    display: flex;
    flex-direction: column;
    line-height: 1.35;

    strong {
      color: #fff;
      font-size: 17px;
      letter-spacing: 0.02em;
    }

    span {
      color: #8fa8c2;
      font-size: 11px;
      letter-spacing: 0.04em;
    }
  }
}

.sidebar__section-label {
  padding: 0 12px;
  margin-bottom: 10px;
  color: #7893ad;
  font-size: 11px;
  letter-spacing: 0.12em;
}

.side-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;

  &__item {
    display: flex;
    align-items: center;
    gap: 11px;
    min-height: 42px;
    padding: 0 12px;
    border-radius: 9px;
    color: #b9cbe0;
    text-decoration: none;
    font-size: 13px;
    transition: background 0.18s ease, color 0.18s ease;

    &:hover:not(.is-disabled) {
      background: rgba(148, 190, 233, 0.1);
      color: #fff;
    }

    &.is-active {
      background: linear-gradient(90deg, rgba(14, 165, 233, 0.22), rgba(14, 165, 233, 0.08));
      color: #fff;
      box-shadow: inset 3px 0 0 #38bdf8;
    }

    &.is-disabled {
      cursor: not-allowed;
      color: #7893ad;
    }
  }

  &__soon {
    margin-left: auto;
    padding: 2px 6px;
    border-radius: 4px;
    background: rgba(148, 190, 233, 0.1);
    font-size: 10px;
    white-space: nowrap;
  }
}

.sidebar__footer {
  margin-top: auto;
  padding: 16px 10px 0;
  border-top: 1px solid rgba(148, 190, 233, 0.14);
  font-size: 11px;
  color: #7893ad;
}

.sidebar__footer-line {
  display: flex;
  align-items: center;
  gap: 7px;
  color: #b9cbe0;
}

.sidebar__hint {
  display: block;
  margin-top: 5px;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #34d399;
  box-shadow: 0 0 0 3px rgba(52, 211, 153, 0.14);
}

.workspace {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.topbar {
  min-height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 32px;
  background: rgba(255, 255, 255, 0.9);
  border-bottom: 1px solid var(--color-border);
  backdrop-filter: blur(10px);

  &__crumb {
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--color-text-muted);
    font-size: 12px;

    strong {
      color: var(--color-text);
      font-weight: 600;
    }
  }

  &__right {
    display: flex;
    align-items: center;
    gap: 14px;
  }

  &__pill {
    display: inline-flex;
    align-items: center;
    gap: 7px;
    padding: 5px 10px;
    border: 1px solid #d7f0e4;
    border-radius: 999px;
    background: #f2fbf7;
    color: #167a54;
    font-size: 11px;

    &.is-down {
      border-color: #fecaca;
      background: #fef2f2;
      color: #b91c1c;

      .status-dot {
        background: #ef4444;
        box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.12);
      }
    }

    &.is-checking {
      border-color: #fde68a;
      background: #fffbeb;
      color: #b45309;

      .status-dot {
        background: #f59e0b;
        box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.12);
      }
    }
  }

  &__user {
    color: var(--color-text-secondary);
    font-size: 12px;
  }
}

.app-main {
  flex: 1;
  width: min(1440px, 100%);
  margin: 0 auto;
  padding: 28px 32px 36px;
}

.app-footer {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 32px;
  border-top: 1px solid var(--color-border);
  color: var(--color-text-muted);
  font-size: 11px;
}

@media (max-width: 980px) {
  .app-shell {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    flex: none;
    min-height: auto;
    padding: 14px 20px;
  }

  .brand {
    margin-bottom: 14px;
  }

  .sidebar__section-label,
  .sidebar__footer {
    display: none;
  }

  .side-nav {
    flex-direction: row;
    overflow-x: auto;
  }

  .side-nav__item {
    white-space: nowrap;
  }

  .topbar {
    padding: 0 20px;
  }

  .app-main {
    padding: 22px 20px 28px;
  }

  .app-footer {
    padding: 12px 20px;
  }
}

@media (max-width: 640px) {
  .topbar__pill,
  .topbar__user {
    display: none;
  }

  .topbar {
    min-height: 54px;
  }

  .app-footer {
    flex-direction: column;
    gap: 3px;
  }
}
</style>
