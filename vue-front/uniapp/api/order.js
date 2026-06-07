import { get, post, put } from './request.js'

export const createOrder = (data) => {
  return post('/api_order_create.action', data)
}

export const getOrderDetail = (id) => {
  return get(`/api_order_detail.action?id=${id}`)
}

export const cancelOrder = (id) => {
  return put(`/api_order_cancel.action?id=${id}`)
}

export const payOrder = (id) => {
  return put(`/api_order_pay.action?id=${id}`)
}

export const getOrderList = () => {
  return get('/api_order_list.action')
}

export const completeOrder = (id) => {
  return put(`/api_order_complete.action?id=${id}`)
}

export const updateOrderDiningType = (id, diningType) => {
  return put(`/api_order_update_dining_type.action?id=${id}`, { diningType })
}