import request from './request'

/**
 * 获取所有启用的规格组列表
 */
export const getOptionGroups = () => {
  return request.get('/api_admin_option_groups.action')
}
