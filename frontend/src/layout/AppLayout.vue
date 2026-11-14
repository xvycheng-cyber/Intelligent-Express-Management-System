<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <div class="app-header__brand">
        <span class="app-header__mark">快</span>
        <div>
          <h1 class="app-header__title">快递通</h1>
          <p class="app-header__subtitle">智能快递地址簿录入与管理系统</p>
        </div>
      </div>
      <nav class="app-nav" aria-label="主导航">
        <RouterLink class="app-nav__link" to="/import">智能录入</RouterLink>
        <span class="app-nav__hint">MS-01 工程初始化</span>
      </nav>
    </header>

    <main class="app-main">
      <slot />
    </main>

    <footer class="app-footer">
      <span>Intelligent-Express-Management-System</span>
      <span>仅供本机课程演示 · 127.0.0.1</span>
    </footer>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 16px 32px;
  background: #ffffff;
  border-bottom: 1px solid var(--color-border);
}

.app-header__brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-header__mark {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  color: #ffffff;
  font-weight: 700;
  background: var(--color-primary);
}

.app-header__title {
  margin: 0;
  font-size: 18px;
  line-height: 1.4;
}

.app-header__subtitle {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 12px;
}

.app-nav {
  display: flex;
  align-items: center;
  gap: 16px;
}

.app-nav__link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 600;
}

.app-nav__link:hover {
  text-decoration: underline;
}

.app-nav__hint {
  color: var(--color-text-secondary);
  font-size: 12px;
}

.app-main {
  flex: 1;
  width: min(1120px, calc(100% - 48px));
  margin: 0 auto;
  padding: 32px 0;
}

.app-footer {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 32px;
  border-top: 1px solid var(--color-border);
  color: var(--color-text-secondary);
  font-size: 12px;
}
</style>
