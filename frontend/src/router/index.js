import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
    },
    {
      path: '/post/:id',
      name: 'post-detail',
      component: () => import('../views/PostDetailView.vue'),
    },
    {
      path: '/space',
      name: 'space',
      component: () => import('../views/ProfileView.vue'),
    },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

export default router
