import { get, post, put } from './request.js'

export const createOrder = () => {
  return post('/api/order/create')
}

export const getOrderDetail = (id) => {
  return get(`/api/order/${id}`)
}

export const cancelOrder = (id) => {
  return put(`/api/order/${id}/cancel`)
}

export const payOrder = (id) => {
  return put(`/api/order/${id}/pay`)
}

export const getOrderList = () => {
  return get('/api/order/list')
}

export const completeOrder = (id) => {
  return put(`/api/order/${id}/complete`)
}