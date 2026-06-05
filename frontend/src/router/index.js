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
      path: '/records',
      name: 'records',
      component: () => import('../views/RecordsView.vue'),
    },
    {
      path: '/barrage-wall',
      name: 'barrage-wall',
      component: () => import('../views/BarrageWallView.vue'),
    },
    {
      path: '/album',
      name: 'album',
      component: () => import('../views/AlbumView.vue'),
    },
    {
      path: '/about',
      name: 'about-me',
      component: () => import('../views/ProfileView.vue'),
    },
    {
      path: '/space',
      name: 'space',
      component: () => import('../views/ProfileView.vue'),
    },
    {
      path: '/studio',
      name: 'studio',
      component: () => import('../views/StudioView.vue'),
    },
    {
      path: '/community',
      name: 'community',
      component: () => import('../views/CommunityView.vue'),
    },
    {
      path: '/garden',
      name: 'garden',
      component: () => import('../views/GardenView.vue'),
    },
    {
      path: '/community/studio',
      name: 'community-studio',
      component: () => import('../views/CommunityStudioView.vue'),
    },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

export default router
