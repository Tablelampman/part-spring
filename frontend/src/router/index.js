import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue')
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/Cart.vue'),
        meta: { role: 'CONSUMER' }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/Orders.vue')
      },
      {
        path: 'my-products',
        name: 'MyProducts',
        component: () => import('@/views/MyProducts.vue'),
        meta: { role: 'FARMER' }
      },
      {
        path: 'review-products',
        name: 'ReviewProducts',
        component: () => import('@/views/ReviewProducts.vue'),
        meta: { role: 'ADMIN' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token

  if (!token && to.path !== '/login' && to.path !== '/register') {
    next('/login')
  } else if (to.meta.role && userStore.userInfo.role !== to.meta.role) {
    next('/')
  } else {
    next()
  }
})

export default router
