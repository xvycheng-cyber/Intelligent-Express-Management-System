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
      meta: { title: '批量地址录入' }
    },
    {
      path: '/addresses',
      name: 'addresses',
      component: () => import('@/views/AddressListView.vue'),
      meta: { title: '地址簿管理' }
    },
    {
      path: '/web-basics',
      name: 'web-basics',
      component: () => import('@/views/WebBasicsView.vue'),
      meta: { title: 'Web 基础实践' }
    },
    {
      path: '/web-basics/wireframe',
      name: 'web-basics-wireframe',
      component: () => import('@/views/WireframeView.vue'),
      meta: { title: '快递通系统原创线框设计' }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue'),
      meta: { title: '页面不存在' }
    }
  ],
  scrollBehavior(to, _from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.hash) {
      return { el: to.hash, top: 24, behavior: 'smooth' }
    }
    return { top: 0 }
  }
})

router.afterEach((to) => {
  const title = (to.meta.title as string | undefined) ?? '快递通'
  document.title = `${title} - 快递通`
})

export default router
