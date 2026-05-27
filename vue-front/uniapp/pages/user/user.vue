<template>
  <view class="user-page">
    <view class="header-bg">
      <image class="bg-image" src="/static/PersonalCenter/PersonalCenter.jpg" mode="aspectFill" />
      <view class="user-info-row">
        <view class="avatar-wrapper">
          <image class="avatar" :src="userInfo.avatarUrl || '/static/Personal Center/default-avatar.png'" mode="aspectFill" />
        </view>
        <text class="nickname">{{ userInfo.nickName || '微信用户' }}</text>
      </view>
    </view>

    <view class="assets-card">
      <view class="asset-item" @tap="goVoucher">
        <image class="asset-icon" src="/static/PersonalCenter/lucky_bag.png" mode="aspectFit" />
        <text class="asset-label">兑换卷</text>
        <text class="asset-value">0张</text>
      </view>
      <view class="asset-divider"></view>
      <view class="asset-item asset-balance">
        <image class="asset-icon" src="/static/PersonalCenter/balance.png" mode="aspectFit" />
        <text class="asset-label">余额</text>
        <text class="asset-value">{{ userInfo.balance != null ? userInfo.balance.toFixed(2) : '0.00' }}元</text>
      </view>
      <view class="asset-divider"></view>
      <view class="asset-item" @tap="goPoints">
        <image class="asset-icon" src="/static/PersonalCenter/points.png" mode="aspectFit" />
        <text class="asset-label">积分</text>
        <text class="asset-value">{{ userInfo.pointsBalance != null ? userInfo.pointsBalance : 0 }}</text>
      </view>
    </view>

    <view class="func-card">
      <view class="func-grid">
        <view class="func-item" @tap="goOrder">
          <image class="func-icon" src="/static/PersonalCenter/order.png" mode="aspectFit" />
          <text class="func-text">我的订单</text>
        </view>
        <view class="func-item" @tap="goProfile">
          <image class="func-icon" src="/static/PersonalCenter/Personal_Information.png" mode="aspectFit" />
          <text class="func-text">个人信息</text>
        </view>
        <view class="func-item" @tap="goVoucherExchange">
          <image class="func-icon" src="/static/PersonalCenter/voucher.png" mode="aspectFit" />
          <text class="func-text">兑换卷兑换</text>
        </view>
        <button class="func-item contact-btn" open-type="contact">
          <image class="func-icon" src="/static/PersonalCenter/customer_service.png" mode="aspectFit" />
          <text class="func-text">联系客服</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getUserInfo } from '@/api/user.js'

const userInfo = reactive({
  nickName: '',
  avatarUrl: '',
  balance: 0,
  pointsBalance: 0
})

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

const goOrder = () => {
  uni.switchTab({ url: '/pages/bill/bill' })
}

const goProfile = () => {
  uni.showToast({ title: '个人信息开发中', icon: 'none' })
}

const goVoucher = () => {
  uni.showToast({ title: '兑换卷开发中', icon: 'none' })
}

const goPoints = () => {
  uni.showToast({ title: '积分中心开发中', icon: 'none' })
}

const goVoucherExchange = () => {
  uni.showToast({ title: '兑换卷兑换开发中', icon: 'none' })
}

onShow(() => {
  fetchUserInfo()
})
</script>

<style lang="scss" scoped>
.user-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.header-bg {
  position: relative;
  height: 380rpx;
}

.bg-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.user-info-row {
  position: relative;
  display: flex;
  align-items: center;
  padding: 80rpx 40rpx 30rpx;
  z-index: 1;
}

.avatar-wrapper {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background-color: #fff;
  overflow: hidden;
  flex-shrink: 0;
  border: 4rpx solid rgba(255, 255, 255, 0.6);
}

.avatar {
  width: 100%;
  height: 100%;
}

.nickname {
  margin-left: 24rpx;
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
  text-shadow: 0 1rpx 4rpx rgba(255, 255, 255, 0.8);
}

.assets-card {
  margin: -60rpx 24rpx 20rpx;
  background-color: #ffffff;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 36rpx 16rpx 28rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 10;
}

.asset-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.asset-balance {
  cursor: default;
}

.asset-icon {
  width: 72rpx;
  height: 72rpx;
}

.asset-label {
  font-size: 24rpx;
  color: #666666;
}

.asset-value {
  font-size: 26rpx;
  color: #333333;
  font-weight: 500;
}

.asset-divider {
  width: 1rpx;
  height: 70rpx;
  background-color: #eeeeee;
}

.func-card {
  margin: 0 24rpx;
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 32rpx 16rpx 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.func-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx 0;
}

.func-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14rpx;
}

.func-icon {
  width: 64rpx;
  height: 64rpx;
}

.func-text {
  font-size: 24rpx;
  color: #333333;
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