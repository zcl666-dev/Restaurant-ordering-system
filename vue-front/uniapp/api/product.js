import { get } from './request.js'

export const getProductDisplay = () => {
  return get('/api/product/display')
}

export const getProductDetail = (id) => {
  return get(`/api/product/${id}`)
}