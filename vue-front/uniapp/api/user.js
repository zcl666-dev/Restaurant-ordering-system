import { post, get } from './request.js'

export const wxLogin = (data) => {
  return post('/api_wx_login.action', data)
}

export const getUserInfo = () => {
  return get('/api_user_info.action')
}

export const saveSubscribe = (data) => {
  return post('/api_subscribe_save.action', data)
}