import { getToken } from '../utils/auth'

/**
 * 上传图片到后端（后端代理上传到 OSS）
 * @param {File} file - 文件对象
 * @returns {Promise<string>} 图片 URL
 */
export const uploadImage = (file) => {
  return new Promise((resolve, reject) => {
    const formData = new FormData()
    formData.append('file', file)

    const xhr = new XMLHttpRequest()
    xhr.open('POST', '/api/upload', true)

    const token = getToken()
    if (token) {
      xhr.setRequestHeader('Authorization', 'Bearer ' + token)
    }

    xhr.timeout = 30000

    xhr.onload = () => {
      try {
        const res = JSON.parse(xhr.responseText)
        if (res.code === 200) {
          resolve(res.data)
        } else {
          reject(new Error(res.message || '上传失败'))
        }
      } catch (e) {
        reject(new Error('解析响应失败'))
      }
    }

    xhr.onerror = () => {
      reject(new Error('网络错误，上传失败'))
    }

    xhr.ontimeout = () => {
      reject(new Error('上传超时，请检查网络'))
    }

    xhr.send(formData)
  })
}
