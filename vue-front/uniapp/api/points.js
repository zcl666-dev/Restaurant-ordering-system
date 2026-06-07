import { get } from './request.js'

export const getPointsDetail = () => {
  return get('/api_points_detail.action')
}
