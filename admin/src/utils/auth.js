const TOKEN_KEY = 'admin_token'
const INFO_KEY = 'admin_info'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(INFO_KEY)
}

export function getAdminInfo() {
  const info = localStorage.getItem(INFO_KEY)
  return info ? JSON.parse(info) : null
}

export function setAdminInfo(info) {
  localStorage.setItem(INFO_KEY, JSON.stringify(info))
}
