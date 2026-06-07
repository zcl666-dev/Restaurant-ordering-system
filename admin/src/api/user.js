import request from './request'

export function getUserList(params) {
  return request.get('/api_admin_users.action', { params })
}

export function getUserDetail(id) {
  return request.get('/api_admin_user_detail.action', { params: { id } })
}

export function updateUser(id, data) {
  return request.post('/api_admin_user_update.action', { ...data, id })
}

export function deleteUser(id) {
  return request.post('/api_admin_user_delete.action', { id })
}
