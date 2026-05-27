import { post, get } from './request.js'

export const wxLogin = (data) => {
  return post('/api/wx/login', data)
}

export const getUserInfo = () => {
  return get('/api/user/info')
}

export const saveSubscribe = (data) => {
  return post('/api/subscribe/save', data)
}