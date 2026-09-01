import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { guestOnly: true, title: '登录' },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/auth/RegisterView.vue'),
      meta: { guestOnly: true, title: '注册' },
    },
    {
      path: '/',
      component: () => import('@/layout/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: '/home' },
        {
          path: 'home',
          name: 'home',
          component: () => import('@/views/home/HomeView.vue'),
          meta: { title: '首页' },
        },
        {
          path: 'plans',
          name: 'plans',
          component: () => import('@/views/plans/PlanListView.vue'),
          meta: { title: '学习计划' },
        },
        {
          path: 'plans/new',
          name: 'plan-new',
          component: () => import('@/views/plans/PlanEditView.vue'),
          meta: { title: '新建计划' },
        },
        {
          path: 'plans/:id/edit',
          name: 'plan-edit',
          component: () => import('@/views/plans/PlanEditView.vue'),
          meta: { title: '编辑计划' },
        },
        {
          path: 'checkins',
          name: 'checkins',
          component: () => import('@/views/checkins/CheckinView.vue'),
          meta: { title: '每日打卡' },
        },
        {
          path: 'statistics',
          name: 'statistics',
          component: () => import('@/views/statistics/StatisticsView.vue'),
          meta: { title: '学习统计' },
        },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/home' },
  ],
})

// 全局路由守卫：需要登录的页面跳登录；已登录访问登录/注册跳首页；
// token 存在但用户信息缺失时先拉取。
router.beforeEach(async (to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guestOnly && auth.token) {
    return { name: 'home' }
  }
  if (auth.token && !auth.user) {
    try {
      await auth.fetchMe()
    } catch {
      return { name: 'login' }
    }
  }
  return true
})

router.afterEach((to) => {
  const title = to.meta.title as string | undefined
  document.title = title ? `${title} · StudyFlow` : 'StudyFlow'
})

export default router
