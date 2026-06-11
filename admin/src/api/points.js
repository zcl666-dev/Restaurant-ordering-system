import request from './request'

export function getPointLogList(params) {
  return request.get('/api_admin_point_logs.action', { params })
}

export function getPointsMallList(params) {
  return request.get('/api_admin_points_mall.action', { params })
}

export function createPointsMallItem(data) {
  return request.post('/api_admin_points_mall_create.action', data)
}

export function updatePointsMallItem(data) {
  return request.post('/api_admin_points_mall_update.action', data)
}

export function deletePointsMallItem(id) {
  return request.post('/api_admin_points_mall_delete.action', { id })
}
