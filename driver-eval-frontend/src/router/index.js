import { createRouter, createWebHistory } from 'vue-router'
import { getToken, getUserInfo } from '../utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/passenger',
    component: () => import('../layout/PassengerLayout.vue'),
    meta: { requiresAuth: true, role: 'PASSENGER' },
    children: [
      { path: 'home', name: 'PassengerHome', component: () => import('../views/passenger/Home.vue') },
      { path: 'order/create', name: 'CreateOrder', component: () => import('../views/passenger/CreateOrder.vue') },
      { path: 'order/list', name: 'PassengerOrderList', component: () => import('../views/passenger/OrderList.vue') },
      { path: 'order/completed', name: 'PassengerCompletedOrders', component: () => import('../views/passenger/CompletedOrders.vue') },
      { path: 'order/cancelled', name: 'PassengerCancelledOrders', component: () => import('../views/passenger/CancelledOrders.vue') },
      { path: 'eval', name: 'PassengerEval', component: () => import('../views/passenger/EvalManage.vue') },
      { path: 'complaint', name: 'PassengerComplaint', component: () => import('../views/passenger/ComplaintManage.vue') }
    ]
  },
  {
    path: '/driver',
    component: () => import('../layout/DriverLayout.vue'),
    meta: { requiresAuth: true, role: 'DRIVER' },
    children: [
      { path: 'profile', name: 'DriverProfile', component: () => import('../views/driver/Profile.vue') },
      { path: 'vehicle', name: 'DriverVehicle', component: () => import('../views/driver/Vehicle.vue') },
      { path: 'order/dispatch', name: 'DriverDispatch', component: () => import('../views/driver/DispatchManage.vue') },
      { path: 'order/completed', name: 'DriverCompletedOrders', component: () => import('../views/driver/CompletedOrders.vue') },
      { path: 'order/cancelled', name: 'DriverCancelledOrders', component: () => import('../views/driver/CancelledOrders.vue') },
      { path: 'eval', name: 'DriverEval', component: () => import('../views/driver/EvalManage.vue') },
      { path: 'tag-stats', name: 'DriverTagStats', component: () => import('../views/driver/TagStats.vue') },
      { path: 'star-chart', name: 'DriverStarChart', component: () => import('../views/driver/StarChart.vue') },
      { path: 'complaint', name: 'DriverComplaint', component: () => import('../views/driver/ComplaintManage.vue') },
      { path: 'appeal', name: 'DriverAppeal', component: () => import('../views/driver/AppealManage.vue') }
    ]
  },
  {
    path: '/admin',
    component: () => import('../layout/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/Users.vue') },
      { path: 'vehicle/list', name: 'AdminVehicleList', component: () => import('../views/admin/VehicleList.vue') },
      { path: 'vehicle/types', name: 'AdminVehicleTypes', component: () => import('../views/admin/VehicleTypes.vue') },
      { path: 'order/dispatch', name: 'AdminDispatchOrders', component: () => import('../views/admin/DispatchOrders.vue') },
      { path: 'order/completed', name: 'AdminCompletedOrders', component: () => import('../views/admin/CompletedOrders.vue') },
      { path: 'order/cancelled', name: 'AdminCancelledOrders', component: () => import('../views/admin/CancelledOrders.vue') },
      { path: 'score', name: 'AdminScore', component: () => import('../views/admin/ScoreManage.vue') },
      { path: 'complaint', name: 'AdminComplaint', component: () => import('../views/admin/ComplaintManage.vue') },
      { path: 'punish', name: 'AdminPunish', component: () => import('../views/admin/PunishManage.vue') },
      { path: 'eval', name: 'AdminEval', component: () => import('../views/admin/EvalManage.vue') },
      { path: 'tag-stats', name: 'AdminTagStats', component: () => import('../views/admin/TagStats.vue') },
      { path: 'appeal', name: 'AdminAppeal', component: () => import('../views/admin/AppealManage.vue') },
      { path: 'tags', name: 'AdminTags', component: () => import('../views/admin/TagManage.vue') },
      { path: 'sensitive-words', name: 'AdminSensitiveWords', component: () => import('../views/admin/SensitiveWords.vue') },
      { path: 'announcement', name: 'AdminAnnouncement', component: () => import('../views/admin/AnnouncementManage.vue') }
    ]
  },
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const roleHomeMap = {
  PASSENGER: '/passenger/home',
  DRIVER: '/driver/profile',
  ADMIN: '/admin/dashboard'
}

router.beforeEach((to, from, next) => {
  const token = getToken()
  const userInfo = getUserInfo()

  if (to.path === '/login' || to.path === '/register') {
    if (token && userInfo) {
      next(roleHomeMap[userInfo.role] || '/login')
    } else {
      next()
    }
    return
  }

  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  if (to.meta.role && userInfo && to.meta.role !== userInfo.role) {
    next(roleHomeMap[userInfo.role] || '/login')
    return
  }

  next()
})

export default router
