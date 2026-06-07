import request from './request'

export function getCategoryList() {
  return request.get('/api_admin_categories.action')
}

export function createCategory(data) {
  return request.post('/api_admin_category_create.action', data)
}

export function updateCategory(id, data) {
  return request.post('/api_admin_category_update.action', { ...data, id })
}

export function deleteCategory(id) {
  return request.post('/api_admin_category_delete.action', { id })
}
