<template>
  <view class="subscribe-overlay" :class="{ show: visible }">
    <view class="subscribe-mask" @tap="handleCancel"></view>
    <view class="subscribe-popup">
      <!-- 顶部标题栏 -->
      <view class="subscribe-header">
        <view class="app-icon">
          <text class="icon-text">🍠</text>
        </view>
        <text class="app-name">308商业帝国 申请</text>
      </view>

      <!-- 副标题 -->
      <view class="subscribe-subtitle">
        <text class="subtitle-text">发送一次以下消息</text>
      </view>

      <!-- 订阅选项列表 -->
      <view class="subscribe-list">
        <view class="subscribe-item" v-for="item in subscribeItems" :key="item.templateId">
          <text class="item-name">{{ item.name }}</text>
          <view class="switch" :class="{ active: item.enabled }" @tap="toggleSwitch(item)">
            <view class="switch-thumb"></view>
          </view>
        </view>
      </view>

      <!-- 总是保持选择 -->
      <view class="keep-choice" @tap="toggleKeepChoice">
        <view class="checkbox" :class="{ checked: keepChoice }">
          <text v-if="keepChoice" class="check-mark">✓</text>
        </view>
        <text class="keep-text">总是保持以上选择</text>
      </view>

      <!-- 按钮组 -->
      <view class="button-group">
        <view class="btn btn-cancel" @tap="handleCancel">
          <text class="btn-text">取消</text>
        </view>
        <view class="btn btn-confirm" @tap="handleConfirm">
          <text class="btn-text">确定</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { saveSubscribe } from '@/api/user.js'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'confirm', 'cancel'])

const STORAGE_KEY = 'subscribe_keep_choice'

const subscribeItems = ref([
  { templateId: 'owRJNezMKIuvTypD-IS_CEcgYbe3rfPz2WdWQyR889c', name: '下单成功通知', enabled: false },
  { templateId: 'J2kTKOFZHXW8TuRUYR-QfUPotm0uwS6ft0ikephjEjE', name: '订单取消通知', enabled: false },
  { templateId: 'iSuL7Y8g3WyG-4VM0tbFrEwvqB95LDqp71k4vx1OTvQ', name: '用餐提醒', enabled: false }
])

const keepChoice = ref(false)

// 调用微信订阅消息API获取权限
const requestSubscribeMessage = (tmplIds) => {
  return new Promise((resolve, reject) => {
    wx.requestSubscribeMessage({
      tmplIds: tmplIds,
      success: resolve,
      fail: reject
    })
  })
}

// 检查是否已经保存了"总是保持"的选择
const hasKeepChoice = () => {
  try {
    return !!uni.getStorageSync(STORAGE_KEY)
  } catch {
    return false
  }
}

// 获取保存的订阅选择
const getKeepChoiceSettings = () => {
  try {
    const saved = uni.getStorageSync(STORAGE_KEY)
    return saved ? JSON.parse(saved) : null
  } catch {
    return null
  }
}

// 静默调用微信订阅API（不弹自定义弹窗，但仍会弹微信原生授权）
const silentRequestSubscribe = async () => {
  const settings = getKeepChoiceSettings()
  if (!settings) return

  const tmplIds = settings.selectedTemplates || []
  if (tmplIds.length === 0) return

  try {
    await requestSubscribeMessage(tmplIds)
  } catch (err) {
    console.log('静默订阅调用失败:', err)
  }
}

const toggleSwitch = (item) => {
  item.enabled = !item.enabled
}

const toggleKeepChoice = () => {
  keepChoice.value = !keepChoice.value
}

const handleCancel = () => {
  emit('update:visible', false)
  emit('cancel')
}

const handleConfirm = async () => {
  const selectedTemplates = subscribeItems.value
    .filter(item => item.enabled)
    .map(item => item.templateId)

  // 将用户选择保存到后端
  for (const item of subscribeItems.value) {
    try {
      await saveSubscribe({
        templateId: item.templateId,
        status: item.enabled ? 1 : 0
      })
    } catch (err) {
      console.error('保存订阅状态失败:', item.templateId, err)
    }
  }

  // 调用微信订阅消息API获取权限（会弹出微信原生授权弹窗）
  if (selectedTemplates.length > 0) {
    try {
      await requestSubscribeMessage(selectedTemplates)
    } catch (err) {
      console.log('用户拒绝了订阅授权或调用失败:', err)
    }
  }

  // 如果勾选了"总是保持以上选择"，保存到本地缓存，后续不再弹窗
  if (keepChoice.value) {
    uni.setStorageSync(STORAGE_KEY, JSON.stringify({
      selectedTemplates,
      subscribeSettings: subscribeItems.value.map(item => ({
        templateId: item.templateId,
        name: item.name,
        enabled: item.enabled
      }))
    }))
  }

  emit('update:visible', false)
  emit('confirm')
}

defineExpose({
  hasKeepChoice,
  getKeepChoiceSettings,
  silentRequestSubscribe
})
</script>

<style lang="scss" scoped>
.subscribe-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  visibility: hidden;
  opacity: 0;
  transition: all 0.3s ease;

  &.show {
    visibility: visible;
    opacity: 1;
  }
}

.subscribe-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
}

.subscribe-popup {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #f5f5f5;
  border-radius: 35rpx 35rpx 0 0;
  padding: 0 30rpx;
  padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);

  .show & {
    transform: translateY(0);
  }
}

.subscribe-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 30rpx 0 20rpx;
}

.app-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #128812, #1aad19);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-text {
  font-size: 32rpx;
}

.app-name {
  font-size: 30rpx;
  color: #333;
  font-weight: 500;
}

.subscribe-subtitle {
  padding: 20rpx 0 30rpx;
}

.subtitle-text {
  font-size: 40rpx;
  color: #333;
  font-weight: bold;
}

.subscribe-list {
  padding-bottom: 20rpx;
}

.subscribe-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #e5e5e5;

  &:last-child {
    border-bottom: none;
  }
}

.item-name {
  font-size: 36rpx;
  color: #333;
  font-weight: 500;
  flex: 1;
}

.switch {
  width: 80rpx;
  height: 48rpx;
  border-radius: 24rpx;
  background-color: #c4c2c2;
  position: relative;
  transition: background-color 0.3s ease;
  flex-shrink: 0;

  &.active {
    background-color: #128812;
  }
}

.switch-thumb {
  position: absolute;
  top: 4rpx;
  left: 4rpx;
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background-color: #fff;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15);
  transition: transform 0.3s ease;

  .switch.active & {
    transform: translateX(32rpx);
  }
}

.keep-choice {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx 0 40rpx;
}

.checkbox {
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  border: 2rpx solid #c4c2c2;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.checked {
    background-color: #128812;
    border-color: #128812;
  }
}

.check-mark {
  font-size: 20rpx;
  color: #fff;
  font-weight: bold;
}

.keep-text {
  font-size: 24rpx;
  color: #988c8c;
}

.button-group {
  display: flex;
  justify-content: center;
  gap: 30rpx;
  padding-top: 10rpx;
}

.btn {
  width: 180rpx;
  height: 84rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.95);
  }
}

.btn-cancel {
  background-color: #e5e5e5;
}

.btn-confirm {
  background-color: #128812;
}

.btn-text {
  font-size: 32rpx;
  color: #333;
  font-weight: 500;

  .btn-confirm & {
    color: #fff;
  }
}
</style>
