import request from './request'

export function getProductList(params) {
  return request.get('/api_admin_products.action', { params })
}

export function getProductDetail(id) {
  return request.get('/api_admin_product_detail.action', { params: { id } })
}

export function createProduct(data) {
  return request.post('/api_admin_product_create.action', data)
}

export function updateProduct(id, data) {
  return request.post('/api_admin_product_update.action', { ...data, id })
}

export function deleteProduct(id) {
  return request.post('/api_admin_product_delete.action', null, { params: { id } })
}

export function toggleProductStatus(id, status) {
  return request.post('/api_admin_product_status.action', null, { params: { id, status } })
}
