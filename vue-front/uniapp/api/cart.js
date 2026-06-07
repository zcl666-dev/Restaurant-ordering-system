import { get, post, put } from './request.js'

export const getCurrentCart = () => {
  return get('/api_cart_current.action')
}

export const addToCart = (data) => {
  return post('/api_cart_add.action', data)
}

export const updateCartItem = (itemId, data) => {
  return put(`/api_cart_item_update.action?itemId=${itemId}`, data)
}
