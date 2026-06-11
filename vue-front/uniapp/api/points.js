import { get, post } from './request.js'

export const getPointsDetail = () => {
  return get('/api_points_detail.action')
}

export const getPointsMallList = () => {
  return get('/api_points_mall_list.action')
}

export const exchangePoints = (pointsMallId) => {
  return post('/api_points_exchange.action', { pointsMallId })
}

export const getVoucherList = () => {
  return get('/api_points_vouchers.action')
}
