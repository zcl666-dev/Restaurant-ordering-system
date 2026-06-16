import request from './request'

export function getReviewList(params) {
  return request.get('/api_admin_review_list.action', { params })
}
