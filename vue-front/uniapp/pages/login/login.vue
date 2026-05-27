<template>
  <view class="login-page">
    <view class="login-container" v-if="!isChecking">
      <image class="logo" src="/static/logo.png" mode="aspectFill" />

      <button
        class="login-btn"
        :class="{ disabled: isLoading }"
        open-type="chooseAvatar"
        @chooseavatar="onChooseAvatar"
      >
        <text class="btn-text">{{ isLoading ? '授权中...' : '微信授权登录' }}</text>
      </button>

      <view class="agreement">
        <text class="agreement-text">登录即表示同意</text>
        <text class="agreement-link" @click="openAgreement('user')">《用户协议》</text>
        <text class="agreement-text">和</text>
        <text class="agreement-link" @click="openAgreement('privacy')">《隐私政策》</text>
      </view>
    </view>

    <!-- 授权弹窗 -->
    <view class="modal-mask" v-if="showAuthModal" @click.stop>
      <view class="modal-content" @click.stop>
        <text class="modal-title">微信授权登录</text>

        <view class="modal-avatar-row">
          <image class="modal-avatar" :src="tempAvatarUrl" mode="aspectFill" />
        </view>

        <view class="modal-input-row">
          <input
            class="modal-nickname-input"
            type="nickname"
            placeholder="点击获取微信昵称"
            v-model="capturedNickName"
            focus
          />
        </view>

        <text class="modal-hint">点击输入框，选择微信推荐的昵称</text>

        <view class="modal-btn-row">
          <view class="modal-btn cancel" @click="onCancel">
            <text class="modal-btn-text cancel-text">取消</text>
          </view>
          <view class="modal-btn confirm" @click="onConfirm">
            <text class="modal-btn-text confirm-text">确认</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { wxLogin as loginRequest } from '@/api/user.js'

const isLoading = ref(false)
const isChecking = ref(true)
const showAuthModal = ref(false)
const tempAvatarUrl = ref('')
const tempCode = ref('')
const capturedNickName = ref('')

const navigateToHome = () => {
  uni.reLaunch({
    url: '/pages/index/index',
    success: () => {
      console.log('reLaunch跳转成功')
    },
    fail: (err) => {
      console.log('reLaunch跳转失败:', JSON.stringify(err))
      uni.switchTab({
        url: '/pages/index/index',
        success: () => {
          console.log('switchTab跳转成功')
        },
        fail: (err2) => {
          console.log('switchTab跳转失败:', JSON.stringify(err2))
          uni.redirectTo({
            url: '/pages/index/index',
            fail: (err3) => {
              console.log('redirectTo也失败:', JSON.stringify(err3))
            }
          })
        }
      })
    }
  })
}

const checkTokenAndRedirect = () => {
  const token = uni.getStorageSync('token')
  if (isTokenValid(token)) {
    navigateToHome()
    setTimeout(() => {
      isChecking.value = false
    }, 500)
  } else {
    clearInvalidToken()
    isChecking.value = false
  }
}

const base64Decode = (str) => {
  try {
    const arrayBuffer = uni.base64ToArrayBuffer(str)
    const bytes = new Uint8Array(arrayBuffer)
    let result = ''
    for (let i = 0; i < bytes.length; i++) {
      result += '%' + ('0' + bytes[i].toString(16)).slice(-2)
    }
    return decodeURIComponent(result)
  } catch (e) {
    return null
  }
}

const decodeToken = (token) => {
  if (!token || typeof token !== 'string') return null
  const parts = token.split('.')
  if (parts.length !== 3) return null
  try {
    let payload = parts[1]
    payload = payload.replace(/-/g, '+').replace(/_/g, '/')
    while (payload.length % 4 !== 0) {
      payload += '='
    }
    const decoded = base64Decode(payload)
    if (!decoded) return null
    return JSON.parse(decoded)
  } catch (e) {
    return null
  }
}

const isTokenValid = (token) => {
  const payload = decodeToken(token)
  if (!payload) return false
  if (!payload.exp) return true
  const now = Math.floor(Date.now() / 1000)
  return payload.exp > now
}

const getOpenidFromToken = (token) => {
  const payload = decodeToken(token)

  return payload?.openId || ''
}

const clearInvalidToken = () => {
  try {
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
  } catch (e) {}
}

const onChooseAvatar = async (e) => {
  if (isLoading.value) return

  const avatarUrl = e.detail.avatarUrl
  if (!avatarUrl) {
    uni.showToast({ title: '未获取到头像', icon: 'none' })
    return
  }

  tempAvatarUrl.value = avatarUrl

  try {
    const loginRes = await wxLogin()
    if (!loginRes.code) {
      throw new Error('获取登录凭证失败')
    }
    tempCode.value = loginRes.code
  } catch (err) {
    uni.showToast({ title: err.message || '获取code失败', icon: 'none' })
    return
  }

  capturedNickName.value = ''
  showAuthModal.value = true
}

const onConfirm = async () => {
  await nextTick()

  const nickname = capturedNickName.value.trim()

  if (!nickname) {
    uni.showToast({
      title: '请点击输入框获取微信昵称',
      icon: 'none'
    })
    return
  }

  showAuthModal.value = false
  isLoading.value = true

  uni.showLoading({
    title: '登录中',
    mask: true
  })

  try {
    const resData = await sendLoginToServer(
      tempCode.value,
      nickname,
      tempAvatarUrl.value
    )

    const token = resData.token

    if (!isTokenValid(token)) {
      throw new Error('服务器返回的Token无效')
    }

    uni.setStorageSync('token', token)

    uni.setStorageSync('userInfo', {
      user_id: resData.user_id || resData.userId || '',
      openid: resData.openid || getOpenidFromToken(token) || '',
      nickname: resData.nickname || resData.nickName || nickname,
      avatar_url: resData.avatar_url || resData.avatarUrl || tempAvatarUrl.value
    })

    uni.hideLoading()

    uni.showToast({
      title: '登录成功',
      icon: 'success',
      duration: 1500
    })

    setTimeout(() => {
      navigateToHome()
    }, 500)

  } catch (err) {
    uni.hideLoading()

    const msg = err.message || '登录失败，请重试'

    uni.showToast({
      title: msg,
      icon: 'none',
      duration: 2000
    })

  } finally {
    isLoading.value = false
    tempAvatarUrl.value = ''
    tempCode.value = ''
    capturedNickName.value = ''
  }
}

const onCancel = () => {
  showAuthModal.value = false
  tempAvatarUrl.value = ''
  tempCode.value = ''
  capturedNickName.value = ''
}

const wxLogin = () => {
  return new Promise((resolve, reject) => {
    uni.login({
      provider: 'weixin',
      success: (res) => {
        if (res.code) {
          resolve(res)
        } else {
          reject(new Error('微信登录返回code为空'))
        }
      },
      fail: () => {
        reject(new Error('微信登录接口调用失败'))
      }
    })
  })
}

const sendLoginToServer = (code, nickName, avatarUrl) => {
  return loginRequest({ code, nickName, avatarUrl })
}

const openAgreement = (type) => {
  const titles = { user: '用户协议', privacy: '隐私政策' }
  uni.showToast({ title: titles[type], icon: 'none' })
}

onMounted(() => {
  checkTokenAndRedirect()
})
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-page::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: rotate 20s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  padding: 0 80rpx;
  position: relative;
  z-index: 1;
}

.logo {
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  margin-bottom: 60rpx;
  border: 6rpx solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.3);
  transition: transform 0.3s ease;
}

.logo:active {
  transform: scale(0.95);
}

.login-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(90deg, #07c160 0%, #06ad56 100%);
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 40rpx;
  transition: all 0.3s ease;
  padding: 0;
  border: none;
  line-height: 96rpx;
  box-shadow: 0 12rpx 40rpx rgba(7, 193, 96, 0.4);
  position: relative;
  overflow: hidden;
}

.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s ease;
}

.login-btn:hover::before {
  left: 100%;
}

.login-btn::after {
  border: none;
}

.login-btn:active {
  opacity: 0.9;
  transform: translateY(2rpx);
  box-shadow: 0 8rpx 30rpx rgba(7, 193, 96, 0.3);
}

.login-btn.disabled {
  opacity: 0.6;
  pointer-events: none;
  box-shadow: none;
}

.btn-text {
  font-size: 34rpx;
  font-weight: 600;
  color: #ffffff;
  letter-spacing: 6rpx;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
}

.agreement {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  margin-top: 20rpx;
}

.agreement-text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.7);
}

.agreement-link {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 500;
  text-decoration: underline;
  text-underline-offset: 4rpx;
}

.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
  backdrop-filter: blur(10px);
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  width: 640rpx;
  background-color: #ffffff;
  border-radius: 32rpx;
  padding: 56rpx 48rpx 48rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 40rpx 100rpx rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
  position: relative;
  overflow: hidden;
}

.modal-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 8rpx;
  background: linear-gradient(90deg, #667eea, #764ba2);
}

@keyframes slideUp {
  from { 
    opacity: 0; 
    transform: translateY(60rpx); 
  }
  to { 
    opacity: 1; 
    transform: translateY(0); 
  }
}

.modal-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #333333;
  margin-bottom: 48rpx;
  letter-spacing: 2rpx;
}

.modal-avatar-row {
  margin-bottom: 40rpx;
}

.modal-avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  border: 6rpx solid #f0f0f0;
  box-shadow: 0 12rpx 40rpx rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.modal-avatar:active {
  transform: scale(0.95);
}

.modal-input-row {
  width: 100%;
  height: 96rpx;
  border: 3rpx solid #e8e8e8;
  border-radius: 16rpx;
  padding: 0 32rpx;
  margin-bottom: 16rpx;
  transition: border-color 0.3s ease;
  background-color: #fafafa;
}

.modal-input-row:focus-within {
  border-color: #667eea;
  background-color: #ffffff;
  box-shadow: 0 0 0 4rpx rgba(102, 126, 234, 0.1);
}

.modal-nickname-input {
  width: 100%;
  height: 96rpx;
  font-size: 32rpx;
  color: #333333;
}

.modal-hint {
  font-size: 24rpx;
  color: #999999;
  margin-bottom: 48rpx;
  align-self: flex-start;
  padding-left: 8rpx;
}

.modal-btn-row {
  display: flex;
  width: 100%;
  gap: 32rpx;
}

.modal-btn {
  flex: 1;
  height: 96rpx;
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  font-weight: 600;
}

.modal-btn.cancel {
  background-color: #f5f5f5;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.modal-btn.cancel:active {
  background-color: #e8e8e8;
  transform: translateY(2rpx);
}

.modal-btn.confirm {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 12rpx 40rpx rgba(102, 126, 234, 0.4);
}

.modal-btn.confirm:active {
  opacity: 0.9;
  transform: translateY(2rpx);
  box-shadow: 0 8rpx 30rpx rgba(102, 126, 234, 0.3);
}

.modal-btn-text {
  font-size: 32rpx;
  font-weight: 600;
  letter-spacing: 2rpx;
}

.cancel-text {
  color: #666666;
}

.confirm-text {
  color: #ffffff;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
}
</style>
