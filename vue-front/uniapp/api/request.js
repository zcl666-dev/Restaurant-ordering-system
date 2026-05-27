import config from '@/utils/config.js'

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || ''

    const header = {
      'Content-Type': 'application/json',
      ...options.header
    }

    if (token) {
      header['Authorization'] = 'Bearer ' + token
    }

    uni.request({
      url: config.baseURL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      timeout: options.timeout || config.timeout,
      success: (res) => {
        if (res.statusCode === 200) {
          const body = res.data

          if (body.code === 200) {
            resolve(body.data !== undefined ? body.data : body)
          } else if (body.code === 401) {
            uni.removeStorageSync('token')
            uni.removeStorageSync('userInfo')
            uni.reLaunch({ url: '/pages/login/login' })
            reject(new Error('登录已过期，请重新登录'))
          } else {
            const msg = body.message || '请求失败'
            uni.showToast({ title: msg, icon: 'none', duration: 2000 })
            reject(new Error(msg))
          }
        } else if (res.statusCode === 401) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.reLaunch({ url: '/pages/login/login' })
          reject(new Error('登录已过期，请重新登录'))
        } else {
          const msg = (res.data && res.data.message) || '服务器响应异常'
          uni.showToast({ title: msg, icon: 'none', duration: 2000 })
          reject(new Error(msg))
        }
      },
      fail: (err) => {
        const isTimeout = err.errMsg && err.errMsg.includes('timeout')
        const msg = isTimeout ? '网络请求超时，请检查网络' : '网络连接失败，请重试'
        uni.showToast({ title: msg, icon: 'none', duration: 2000 })
        reject(new Error(msg))
      }
    })
  })
}

const get = (url, data = {}, options = {}) => {
  return request({ url, method: 'GET', data, ...options })
}

const post = (url, data = {}, options = {}) => {
  return request({ url, method: 'POST', data, ...options })
}

const put = (url, data = {}, options = {}) => {
  return request({ url, method: 'PUT', data, ...options })
}

export { get, post, put }
export default request