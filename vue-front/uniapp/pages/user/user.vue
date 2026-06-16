<template>
  <view class="user-page">
    <view class="header-bg">
      <image class="bg-image" src="https://zcl-library-management-system.oss-cn-beijing.aliyuncs.com/%E5%AE%A3%E4%BC%A0%E5%9B%BE/5.jpg" mode="aspectFill" />
      <view class="header-overlay"></view>
      <view class="user-info-row">
        <view class="avatar-wrapper">
          <image v-if="userInfo.avatarUrl" class="avatar" :src="userInfo.avatarUrl" mode="aspectFill" />
          <view v-else class="avatar-fallback">
            <text class="avatar-fallback-text">{{ (userInfo.nickName || '用')[0] }}</text>
          </view>
        </view>
        <view class="user-text-info">
          <text class="nickname">{{ userInfo.nickName || '微信用户' }}</text>
          <text class="user-id-label">欢迎光临</text>
        </view>
      </view>
    </view>

    <view class="assets-card">
      <view class="asset-item" @tap="goVoucher">
        <view class="asset-icon-circle asset-icon-voucher">
          <text class="asset-emoji">🎁</text>
        </view>
        <text class="asset-label">兑换券</text>
        <text class="asset-value">{{ voucherCount }}张</text>
      </view>
      <view class="asset-divider"></view>
      <view class="asset-item asset-balance">
        <view class="asset-icon-circle asset-icon-balance">
          <text class="asset-emoji">💰</text>
        </view>
        <text class="asset-label">余额</text>
        <text class="asset-value">{{ userInfo.balance != null ? userInfo.balance.toFixed(2) : '0.00' }}元</text>
      </view>
      <view class="asset-divider"></view>
      <view class="asset-item" @tap="goPoints">
        <view class="asset-icon-circle asset-icon-points">
          <text class="asset-emoji">⭐</text>
        </view>
        <text class="asset-label">积分</text>
        <text class="asset-value">{{ userInfo.pointsBalance != null ? userInfo.pointsBalance : 0 }}</text>
      </view>
    </view>

    <view class="func-card">
      <view class="func-grid">
        <view class="func-item" @tap="goOrder">
          <view class="func-icon-wrapper func-icon-order">
            <text class="func-emoji">📋</text>
          </view>
          <text class="func-text">我的订单</text>
        </view>
        <view class="func-item" @tap="goProfile">
          <view class="func-icon-wrapper func-icon-profile">
            <text class="func-emoji">👤</text>
          </view>
          <text class="func-text">个人信息</text>
        </view>
        <view class="func-item" @tap="goVoucherExchange">
          <view class="func-icon-wrapper func-icon-voucher">
            <text class="func-emoji">🎫</text>
          </view>
          <text class="func-text">兑换券兑换</text>
        </view>
        <button class="func-item contact-btn" open-type="contact">
          <view class="func-icon-wrapper func-icon-service">
            <text class="func-emoji">💬</text>
          </view>
          <text class="func-text">联系客服</text>
        </button>
      </view>
    </view>

    <!-- 自定义 TabBar -->
    <TabBar />
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { getUserInfo } from '@/api/user.js'
import { getVoucherList } from '@/api/points.js'
import TabBar from '@/components/TabBar/TabBar.vue'

const userInfo = reactive({
  nickName: '',
  avatarUrl: '',
  balance: 0,
  pointsBalance: 0
})

const voucherCount = ref(0)

const fetchUserInfo = async () => {
  try {
    const data = await getUserInfo()
    if (data) {
      Object.assign(userInfo, {
        nickName: data.nickName || '微信用户',
        avatarUrl: data.avatarUrl || '',
        balance: data.balance || 0,
        pointsBalance: data.pointsBalance || 0
      })
    }
  } catch (err) {
    console.error('获取用户信息失败:', err)
  }
}

const fetchVoucherCount = async () => {
  try {
    const data = await getVoucherList()
    voucherCount.value = data.unusedCount || 0
  } catch (err) {
    console.error('获取兑换券数量失败:', err)
  }
}

const goOrder = () => {
  uni.switchTab({ url: '/pages/bill/bill' })
}

const goProfile = () => {
  uni.navigateTo({ url: '/pages/profile/profile' })
}

const goVoucher = () => {
  uni.navigateTo({ url: '/pages/voucher/voucher' })
}

const goPoints = () => {
  uni.navigateTo({ url: '/pages/points/points' })
}

const goVoucherExchange = () => {
  uni.navigateTo({ url: '/pages/points-exchange/points-exchange' })
}

onShow(() => {
  fetchUserInfo()
  fetchVoucherCount()
})

onPullDownRefresh(async () => {
  await Promise.all([fetchUserInfo(), fetchVoucherCount()])
  uni.stopPullDownRefresh()
})
</script>

<style lang="scss" scoped>
.user-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: calc(110rpx + env(safe-area-inset-bottom));
}

.header-bg {
  position: relative;
  height: 400rpx;
}

.bg-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.header-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.1) 0%, rgba(0, 0, 0, 0.3) 100%);
}

.user-info-row {
  position: relative;
  display: flex;
  align-items: center;
  padding: 100rpx 40rpx 30rpx;
  z-index: 1;
}

.avatar-wrapper {
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  border: 4rpx solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.2);
}

.avatar {
  width: 100%;
  height: 100%;
}

.avatar-fallback {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-fallback-text {
  font-size: 48rpx;
  font-weight: 700;
  color: #fff;
}

.user-text-info {
  margin-left: 28rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.nickname {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.3);
}

.user-id-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* 资产卡片 */
.assets-card {
  margin: -70rpx 24rpx 24rpx;
  background: linear-gradient(135deg, #ffffff 0%, #fafafa 100%);
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 40rpx 16rpx 32rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 10;
}

.asset-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  transition: transform 0.2s ease;

  &:active {
    transform: scale(0.95);
  }
}

.asset-balance {
  cursor: default;
}

.asset-icon-circle {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.asset-icon-voucher {
  background: linear-gradient(135deg, #fff5f5, #ffe8e8);
}

.asset-icon-balance {
  background: linear-gradient(135deg, #fff8e1, #ffecb3);
}

.asset-icon-points {
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
}

.asset-emoji {
  font-size: 36rpx;
}

.asset-label {
  font-size: 24rpx;
  color: #999;
}

.asset-value {
  font-size: 28rpx;
  color: #333;
  font-weight: 600;
}

.asset-divider {
  width: 1rpx;
  height: 80rpx;
  background: linear-gradient(180deg, transparent, #eee, transparent);
}

/* 功能卡片 */
.func-card {
  margin: 0 24rpx;
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 36rpx 16rpx 28rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.func-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24rpx 0;
}

.func-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14rpx;
  transition: transform 0.2s ease;

  &:active {
    transform: scale(0.92);
  }
}

.func-icon-wrapper {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.func-icon-order {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
}

.func-icon-profile {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.func-icon-voucher {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.func-icon-service {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.func-emoji {
  font-size: 40rpx;
}

.func-text {
  font-size: 24rpx;
  color: #333;
  font-weight: 500;
}

.contact-btn {
  margin: 0;
  padding: 0;
  border: none;
  background-color: transparent;
  line-height: normal;

  &::after {
    display: none;
  }
}
</style>
