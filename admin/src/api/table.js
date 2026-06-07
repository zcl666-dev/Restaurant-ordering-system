import request from './request'

/**
 * 获取桌台列表
 */
export function getTableList(params) {
  return request.get('/api_admin_tables.action', { params })
}

/**
 * 获取桌台详情
 */
export function getTableDetail(id) {
  return request.get('/api_admin_table_detail.action', { params: { id } })
}

/**
 * 新增桌台
 */
export function addTable(data) {
  return request.post('/api_admin_table_add.action', data)
}

/**
 * 更新桌台
 */
export function updateTable(id, data) {
  return request.post('/api_admin_table_update.action', data, { params: { id } })
}

/**
 * 删除桌台
 */
export function deleteTable(id) {
  return request.post('/api_admin_table_delete.action', null, { params: { id } })
}

/**
 * 生成二维码
 */
export function generateQrCode(id) {
  return request.post('/api_admin_table_generate_qr.action', { id })
}

/**
 * 批量生成二维码
 */
export function batchGenerateQrCode() {
  return request.post('/api_admin_table_generate_all_qr.action')
}

/**
 * 下载二维码
 */
export function downloadQrCode(id) {
  return request.get('/api_admin_table_download_qr.action', {
    params: { id },
    responseType: 'blob'
  })
}
