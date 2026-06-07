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
