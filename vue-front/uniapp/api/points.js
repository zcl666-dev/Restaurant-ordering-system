import { get } from './request.js'

export const getPointsDetail = () => {
  return get('/api/points/detail')
}
