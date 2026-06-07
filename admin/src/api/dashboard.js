import request from './request'

export function getDashboardStats() {
  return request.get('/api_admin_dashboard_stats.action')
}

export function getSalesStats(params) {
  return request.get('/api_admin_dashboard_sales.action', { params })
}

export function getTopProducts(params) {
  return request.get('/api_admin_dashboard_top_products.action', { params })
}

export function getOrderStatusDistribution() {
  return request.get('/api_admin_dashboard_order_status.action')
}
