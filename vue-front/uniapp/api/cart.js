import { get, post, put } from './request.js'

export const getCurrentCart = () => {
  return get('/api/cart/current')
}

export const addToCart = (data) => {
  return post('/api/cart/add', data)
}

export const updateCartItem = (itemId, data) => {
  return put(`/api/cart/item/${itemId}`, data)
}
