import request from './request'

export function getDashboardStats() {
  return request.get('/admin/dashboard/stats')
}

export function getSalesStats(params) {
  return request.get('/admin/dashboard/sales', { params })
}

export function getTopProducts(params) {
  return request.get('/admin/dashboard/top-products', { params })
}

export function getOrderStatusDistribution() {
  return request.get('/admin/dashboard/order-status')
}
