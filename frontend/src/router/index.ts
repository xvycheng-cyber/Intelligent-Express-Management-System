import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/import'
    },
    {
      path: '/import',
      name: 'import',
      component: () => import('@/views/ImportView.vue'),
      meta: { title: '智能录入' }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue'),
      meta: { title: '页面不存在' }
    }
  ],
  scrollBehavior: () => ({ top: 0 })
})

router.afterEach((to) => {
  const title = (to.meta.title as string | undefined) ?? '快递通'
  document.title = `${title} - 快递通`
})

export default router
