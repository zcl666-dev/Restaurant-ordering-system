import request from './request'

export function getProductList(params) {
  return request.get('/admin/products', { params })
}

export function getProductDetail(id) {
  return request.get(`/admin/products/${id}`)
}

export function createProduct(data) {
  return request.post('/admin/products', data)
}

export function updateProduct(id, data) {
  return request.put(`/admin/products/${id}`, data)
}

export function deleteProduct(id) {
  return request.delete(`/admin/products/${id}`)
}

export function toggleProductStatus(id, status) {
  return request.put(`/admin/products/${id}/status`, null, { params: { status } })
}
