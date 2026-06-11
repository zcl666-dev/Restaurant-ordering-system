<template>
  <view class="voucher-page">
    <!-- 统计卡片 -->
    <view class="stats-card">
      <view class="stats-item" :class="{ active: currentTab === 0 }" @tap="currentTab = 0">
        <text class="stats-num">{{ unusedCount }}</text>
        <text class="stats-label">可使用</text>
      </view>
      <view class="stats-divider"></view>
      <view class="stats-item" :class="{ active: currentTab === 1 }" @tap="currentTab = 1">
        <text class="stats-num">{{ usedCount }}</text>
        <text class="stats-label">已使用</text>
      </view>
      <view class="stats-divider"></view>
      <view class="stats-item" :class="{ active: currentTab === 2 }" @tap="currentTab = 2">
        <text class="stats-num">{{ expiredCount }}</text>
        <text class="stats-label">已过期</text>
      </view>
    </view>

    <!-- 加载态 -->
    <view v-if="loading" class="loading-container">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 空状态 -->
    <view v-else-if="currentList.length === 0" class="empty-container">
      <text class="empty-icon">🎫</text>
      <text class="empty-text">{{ emptyText }}</text>
    </view>

    <!-- 券列表 -->
    <view v-else class="voucher-list">
      <view
        class="voucher-card"
        :class="{ 'voucher-used': currentTab === 1, 'voucher-expired': currentTab === 2 }"
        v-for="item in currentList"
        :key="item.id"
      >
        <view class="voucher-left">
          <view class="voucher-img-wrapper">
            <image
              v-if="item.productImage"
              :src="item.productImage"
              mode="aspectFill"
              class="voucher-img"
            />
            <view v-else class="voucher-img-placeholder">🍽</view>
          </view>
        </view>
        <view class="voucher-middle">
          <text class="voucher-name">{{ item.productName }}兑换券</text>
          <text class="voucher-points">消耗 {{ item.requiredPoints }} 积分</text>
          <text class="voucher-time">{{ formatTime(item) }}</text>
        </view>
        <view class="voucher-right">
          <view v-if="currentTab === 0" class="voucher-status-badge unused">
            <text class="badge-text">可使用</text>
          </view>
          <view v-else-if="currentTab === 1" class="voucher-status-badge used">
            <text class="badge-text">已使用</text>
          </view>
          <view v-else class="voucher-status-badge expired">
            <text class="badge-text">已过期</text>
          </view>
        </view>
        <!-- 左侧锯齿装饰 -->
        <view class="notch-top"></view>
        <view class="notch-bottom"></view>
      </view>
    </view>

    <view v-if="!loading && currentList.length > 0" class="no-more">
      <text class="no-more-text">没有更多信息了~~</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getVoucherList } from '@/api/points.js'

const loading = ref(true)
const currentTab = ref(0)
const unusedCount = ref(0)
const usedCount = ref(0)
const expiredCount = ref(0)
const unusedList = ref([])
const usedList = ref([])
const expiredList = ref([])

const currentList = computed(() => {
  if (currentTab.value === 0) return unusedList.value
  if (currentTab.value === 1) return usedList.value
  return expiredList.value
})

const emptyText = computed(() => {
  if (currentTab.value === 0) return '暂无可用兑换券'
  if (currentTab.value === 1) return '暂无已使用兑换券'
  return '暂无已过期兑换券'
})

const formatTime = (item) => {
  if (currentTab.value === 1 && item.usedAt) {
    return '使用时间: ' + item.usedAt.replace('T', ' ').substring(0, 16)
  }
  if (item.expireTime) {
    return '有效期至: ' + item.expireTime.replace('T', ' ').substring(0, 16)
  }
  return ''
}

const fetchData = async () => {
  loading.value = true
  try {
    const data = await getVoucherList()
    unusedCount.value = data.unusedCount || 0
    usedCount.value = data.usedCount || 0
    expiredCount.value = data.expiredCount || 0
    unusedList.value = data.unusedList || []
    usedList.value = data.usedList || []
    expiredList.value = data.expiredList || []
  } catch (err) {
    console.error('获取兑换券列表失败:', err)
    uni.showToast({ title: err.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onShow(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.voucher-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: env(safe-area-inset-bottom);
}

/* 统计卡片 */
.stats-card {
  margin: 24rpx;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border-radius: 24rpx;
  padding: 40rpx 0;
  display: flex;
  align-items: center;
  justify-content: space-around;
  box-shadow: 0 8rpx 32rpx rgba(255, 107, 107, 0.3);
}

.stats-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  position: relative;
  padding: 8rpx 0;
}

.stats-num {
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
}

.stats-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.stats-divider {
  width: 1rpx;
  height: 60rpx;
  background: rgba(255, 255, 255, 0.3);
}

/* 加载态 */
.loading-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 0;
  gap: 20rpx;
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
  font-size: 80rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

/* 券列表 */
.voucher-list {
  padding: 0 24rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.voucher-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx 24rpx;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.04);
}

.voucher-card.voucher-used {
  opacity: 0.7;
}

.voucher-card.voucher-expired {
  opacity: 0.5;
}

/* 锯齿装饰 */
.notch-top,
.notch-bottom {
  position: absolute;
  right: 170rpx;
  width: 24rpx;
  height: 12rpx;
  background: #f5f5f5;
  border-radius: 0 0 12rpx 12rpx;
}

.notch-top {
  top: 0;
  border-radius: 0 0 12rpx 12rpx;
}

.notch-bottom {
  bottom: 0;
  border-radius: 12rpx 12rpx 0 0;
}

.voucher-left {
  flex-shrink: 0;
  margin-right: 20rpx;
}

.voucher-img-wrapper {
  width: 120rpx;
  height: 120rpx;
  border-radius: 16rpx;
  overflow: hidden;
  background: #f8f8f8;
}

.voucher-img {
  width: 100%;
  height: 100%;
}

.voucher-img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56rpx;
  background: #f0f0f0;
}

.voucher-middle {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  min-width: 0;
}

.voucher-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.voucher-points {
  font-size: 26rpx;
  color: #FF6B6B;
}

.voucher-time {
  font-size: 22rpx;
  color: #999;
}

.voucher-right {
  flex-shrink: 0;
  margin-left: 16rpx;
}

.voucher-status-badge {
  padding: 8rpx 20rpx;
  border-radius: 24rpx;
}

.voucher-status-badge.unused {
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
}

.voucher-status-badge.used {
  background: #e0e0e0;
}

.voucher-status-badge.expired {
  background: #e0e0e0;
}

.badge-text {
  font-size: 22rpx;
  color: #fff;
  font-weight: 500;
}

/* 底部提示 */
.no-more {
  text-align: center;
  padding: 40rpx 0 60rpx;
}

.no-more-text {
  font-size: 26rpx;
  color: #ccc;
}
</style>
