<template>
  <view class="points-page">
    <!-- 积分头部 -->
    <view class="points-header">
      <view class="header-bg-shape"></view>
      <view class="points-display">
        <text class="points-number">{{ pointsBalance }}</text>
        <text class="points-unit">积分</text>
      </view>
      <text class="points-expire">积分永久有效</text>
    </view>

    <!-- 兑换入口 -->
    <view class="exchange-banner">
      <view class="exchange-left">
        <text class="exchange-icon">🎁</text>
        <text class="exchange-text">可用积分兑换代金券和商品</text>
      </view>
      <view class="exchange-btn" @tap="handleExchange">
        <text class="exchange-btn-text">去兑换</text>
      </view>
    </view>

    <!-- 积分记录 -->
    <view class="records-section">
      <text class="records-title">积分记录</text>

      <view v-if="loading" class="loading-container">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>

      <view v-else-if="Object.keys(records).length === 0" class="empty-container">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无积分记录</text>
      </view>

      <template v-else>
        <view class="month-group" v-for="(items, month) in records" :key="month">
          <view class="month-header">
            <view class="month-bar"></view>
            <text class="month-text">{{ month }}</text>
          </view>

          <view class="record-item" v-for="item in items" :key="item.id">
            <view class="record-info">
              <text class="record-type">{{ item.type === 1 ? '积分赠送' : '积分兑换' }}</text>
              <text class="record-desc">{{ item.orderNo || item.remark }}</text>
              <text class="record-time">{{ formatTime(item.createdAt) }}</text>
            </view>
            <text class="record-points" :class="item.type === 1 ? 'points-add' : 'points-sub'">
              {{ item.type === 1 ? '+' : '-' }} {{ item.pointsChange }}
            </text>
          </view>
        </view>
      </template>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPointsDetail } from '@/api/points.js'

const pointsBalance = ref(0)
const records = ref({})
const loading = ref(true)

const fetchPointsDetail = async () => {
  loading.value = true
  try {
    const data = await getPointsDetail()
    pointsBalance.value = data.pointsBalance || 0
    records.value = data.records || {}
  } catch (err) {
    console.error('获取积分详情失败:', err)
    uni.showToast({ title: err.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}/${m}/${day} ${h}:${min}`
}

const handleExchange = () => {
  uni.showToast({ title: '兑换功能开发中', icon: 'none' })
}

onMounted(() => {
  fetchPointsDetail()
})
</script>

<style lang="scss" scoped>
.points-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 积分头部 */
.points-header {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  padding: 60rpx 40rpx 80rpx;
  position: relative;
  overflow: hidden;
}

.header-bg-shape {
  position: absolute;
  top: -80rpx;
  right: -60rpx;
  width: 300rpx;
  height: 300rpx;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 50%;
}

.points-display {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
}

.points-number {
  font-size: 96rpx;
  font-weight: bold;
  color: #fff;
  line-height: 1;
}

.points-unit {
  font-size: 32rpx;
  color: rgba(255, 255, 255, 0.85);
}

.points-expire {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 12rpx;
}

/* 兑换入口 */
.exchange-banner {
  background: #fff;
  margin: -40rpx 24rpx 0;
  border-radius: 20rpx;
  padding: 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 10;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.exchange-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
}

.exchange-icon {
  font-size: 40rpx;
}

.exchange-text {
  font-size: 28rpx;
  color: #333;
}

.exchange-btn {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border-radius: 32rpx;
  padding: 14rpx 36rpx;
  flex-shrink: 0;
}

.exchange-btn-text {
  font-size: 28rpx;
  color: #fff;
  font-weight: 500;
}

/* 积分记录 */
.records-section {
  padding: 40rpx 24rpx;
}

.records-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 30rpx;
}

.loading-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0;
  gap: 16rpx;
}

.loading-spinner {
  width: 50rpx;
  height: 50rpx;
  border: 4rpx solid #f0f0f0;
  border-top: 4rpx solid #FF6B6B;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 28rpx;
  color: #999;
}

.empty-icon {
  font-size: 64rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

/* 月份分组 */
.month-group {
  margin-bottom: 20rpx;
}

.month-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx 0;
}

.month-bar {
  width: 6rpx;
  height: 32rpx;
  background: linear-gradient(180deg, #FF6B6B, #FF8E53);
  border-radius: 3rpx;
}

.month-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

/* 记录项 */
.record-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx 30rpx;
  margin-bottom: 16rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.03);
}

.record-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  flex: 1;
  min-width: 0;
}

.record-type {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.record-desc {
  font-size: 26rpx;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-time {
  font-size: 24rpx;
  color: #999;
}

.record-points {
  font-size: 36rpx;
  font-weight: bold;
  flex-shrink: 0;
  margin-left: 20rpx;
}

.points-add {
  color: #FF6B6B;
}

.points-sub {
  color: #FF6B6B;
}
</style>
