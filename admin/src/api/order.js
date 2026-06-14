import request from './request'

export function getOrderList(params) {
  return request.get('/api_admin_orders.action', { params })
}

export function getOrderDetail(id) {
  return request.get('/api_admin_order_detail.action', { params: { id } })
}

export function updateOrderStatus(id, status) {
  return request.post('/api_admin_order_status.action', { orderStatus: status }, { params: { id } })
}

export function startProduction(id) {
  return request.post('/api_admin_order_start_production.action', null, { params: { id } })
}

export function rejectOrder(id) {
  return request.post('/api_admin_order_reject.action', null, { params: { id } })
}

export function completeProduction(id) {
  return request.post('/api_admin_order_complete_production.action', null, { params: { id } })
}

// 代客点餐
export function getStaffProducts() {
  return request.get('/api_admin_staff_products.action')
}

export function getStaffProductDetail(id) {
  return request.get('/api_admin_staff_product_detail.action', { params: { id } })
}

export function staffCreateOrder(data) {
  return request.post('/api_admin_staff_create_order.action', data)
}
