import { get } from './request.js'

export const getProductDisplay = () => {
  return get('/api_product_display.action')
}

export const getProductDetail = (id) => {
  return get(`/api_product_detail.action?id=${id}`)
}

export const getRecommendProducts = () => {
  return get('/api_product_recommend.action')
}