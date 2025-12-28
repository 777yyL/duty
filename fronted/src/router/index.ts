import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

const router = createRouter({
  history: createWebHistory('/api/'),
  routes: [
    {
      path: '/',
      component: Layout,
      redirect: '/schedule',
      children: [
        {
          path: 'schedule',
          name: 'Schedule',
          component: () => import('@/views/schedule/index.vue'),
          meta: { title: '值班表管理' }
        },
        {
          path: 'record',
          name: 'Record',
          component: () => import('@/views/record/index.vue'),
          meta: { title: '值班记录查询' }
        },
        {
          path: 'shift',
          name: 'Shift',
          component: () => import('@/views/shift/index.vue'),
          meta: { title: '班次配置' }
        },
        {
          path: 'template',
          name: 'Template',
          component: () => import('@/views/template/index.vue'),
          meta: { title: '记录模板配置' }
        }
      ]
    }
  ]
})

export default router
