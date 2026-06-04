import { createRouter, createWebHashHistory } from 'vue-router'
import { authState, clearAdminSession, isAdminUser, refreshCurrentAdmin } from '../stores/auth'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      redirect: '/dashboard',
    },
    {
      path: '/login',
      name: 'admin-login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/dashboard',
      name: 'admin-dashboard',
      component: () => import('../views/DashboardView.vue'),
      meta: {
        requiresAdmin: true,
      },
    },
  ],
})

router.beforeEach(async (to) => {
  if (to.meta.requiresAdmin) {
    if (!authState.token) {
      return { name: 'admin-login' }
    }

    if (!authState.user) {
      await refreshCurrentAdmin()
    }

    if (!isAdminUser()) {
      clearAdminSession()
      return { name: 'admin-login' }
    }
  }

  if (to.name === 'admin-login' && authState.token) {
    if (!authState.user) {
      await refreshCurrentAdmin()
    }

    if (isAdminUser()) {
      return { name: 'admin-dashboard' }
    }
  }

  return true
})

export default router
