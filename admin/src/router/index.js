import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/LoginView.vue'),
    meta: { noAuth: true }
  },
  {
    path: '/',
    component: () => import('../views/layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/DashboardView.vue'),
        meta: { title: '数据概览' }
      },
      {
        path: 'users',
        name: 'UserList',
        component: () => import('../views/user/UserListView.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'categories',
        name: 'CategoryList',
        component: () => import('../views/category/CategoryListView.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'products',
        name: 'ProductList',
        component: () => import('../views/product/ProductListView.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'products/create',
        name: 'ProductCreate',
        component: () => import('../views/product/ProductFormView.vue'),
        meta: { title: '新增商品' }
      },
      {
        path: 'products/:id/edit',
        name: 'ProductEdit',
        component: () => import('../views/product/ProductFormView.vue'),
        meta: { title: '编辑商品' }
      },
      {
        path: 'orders',
        name: 'OrderList',
        component: () => import('../views/order/OrderListView.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'staff-order',
        name: 'StaffOrder',
        component: () => import('../views/staff-order/StaffOrderView.vue'),
        meta: { title: '代客点餐' }
      },
      {
        path: 'orders/:id',
        name: 'OrderDetail',
        component: () => import('../views/order/OrderDetailView.vue'),
        meta: { title: '订单详情' }
      },
      {
        path: 'stats',
        name: 'SalesStats',
        component: () => import('../views/stats/SalesStatsView.vue'),
        meta: { title: '销售统计' }
      },
      {
        path: 'dining-tables',
        name: 'DiningTable',
        component: () => import('../views/dining-table/DiningTableView.vue'),
        meta: { title: '桌台管理' }
      },
      {
        path: 'point-logs',
        name: 'PointLogs',
        component: () => import('../views/points/PointsLogView.vue'),
        meta: { title: '积分流水' }
      },
      {
        path: 'points-mall',
        name: 'PointsMall',
        component: () => import('../views/points/PointsMallView.vue'),
        meta: { title: '积分商城' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.noAuth) {
    next()
    return
  }
  const token = getToken()
  if (token) {
    next()
  } else {
    next('/login')
  }
})

export default router
