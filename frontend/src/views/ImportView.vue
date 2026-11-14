<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getHealth } from '@/api/health'

const backendStatus = ref('检查中')
const backendDetail = ref('')

onMounted(async () => {
  try {
    const data = await getHealth()
    backendStatus.value = data.status
    backendDetail.value = '通过 Vite 代理 /api 请求后端健康检查'
  } catch (error) {
    backendStatus.value = 'DOWN'
    backendDetail.value = error instanceof Error ? error.message : '请求失败'
  }
})
</script>

<template>
  <section class="page">
    <header class="page__header">
      <h2 class="page__title">智能录入</h2>
      <p class="page__description">
        MS-01 仅完成工程初始化。地址解析、预览、确认保存将在后续里程碑实现。
      </p>
    </header>

    <el-card shadow="never" class="panel">
      <template #header>
        <div class="panel__header">
          <span>初始化状态</span>
          <el-tag :type="backendStatus === 'UP' ? 'success' : 'danger'" effect="light">
            {{ backendStatus }}
          </el-tag>
        </div>
      </template>

      <el-descriptions :column="1" border>
        <el-descriptions-item label="前端">Vue 3 + TypeScript + Vite + Element Plus</el-descriptions-item>
        <el-descriptions-item label="后端">Spring Boot + Spring MVC + MyBatis-Plus</el-descriptions-item>
        <el-descriptions-item label="健康检查">GET /api/health</el-descriptions-item>
        <el-descriptions-item label="监听地址">127.0.0.1:5173 → 127.0.0.1:8080</el-descriptions-item>
      </el-descriptions>

      <el-alert
        class="panel__hint"
        type="info"
        :closable="false"
        show-icon
        :title="backendDetail || '正在请求后端健康检查'"
      />
    </el-card>

    <el-card shadow="never" class="panel">
      <template #header>本阶段范围</template>
      <ul class="scope-list">
        <li>已建立前后端工程、统一响应、全局异常、数据库脚本与健康检查。</li>
        <li>未实现 AddressParserService、地址增删改查、批量保存/删除、搜索分页。</li>
        <li>未实现用户注册与登录；系统仅供本机课程演示。</li>
      </ul>
    </el-card>
  </section>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page__header {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.page__title {
  margin: 0;
  font-size: 24px;
}

.page__description {
  margin: 0;
  color: var(--color-text-secondary);
}

.panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel__hint {
  margin-top: 16px;
}

.scope-list {
  margin: 0;
  padding-left: 20px;
  color: var(--color-text-secondary);
  line-height: 1.9;
}
</style>
