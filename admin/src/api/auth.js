import request from './request'

export function login(data) {
  return request.post('/api_admin_login.action', data)
}

export function getAdminInfo() {
  return request.get('/api_admin_info.action')
}
