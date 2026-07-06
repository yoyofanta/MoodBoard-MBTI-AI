import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import LoginPage from './pages/LoginPage.vue'
import OnboardingPage from './pages/OnboardingPage.vue'
import MainLayout from './pages/MainLayout.vue'
import DiaryEditPage from './pages/DiaryEditPage.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/app/diary'
  },
  {
    path: '/login',
    component: LoginPage
  },
  {
    path: '/onboarding',
    component: OnboardingPage
  },
  {
    path: '/app',
    component: MainLayout,
    children: [
      {
        path: '',
        redirect: '/app/diary'
      },
      {
        path: 'diary',
        name: 'Diary',
        component: () => import('./pages/DiaryPage.vue')
      },
      {
        path: 'diary/edit',
        name: 'DiaryEdit',
        component: DiaryEditPage
      },
      {
        path: 'tree-hole',
        name: 'TreeHole',
        component: () => import('./pages/TreeHolePage.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('./pages/ProfilePage.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')

  if (!token && to.path !== '/login') {
    return '/login'
  }

  if (token && to.path === '/login') {
    return '/app/diary'
  }
})

export default router