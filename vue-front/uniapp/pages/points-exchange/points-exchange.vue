<template>
  <view class="exchange-page">
    <!-- 余额卡片 -->
    <view class="balance-card">
      <view class="balance-row">
        <text class="balance-label">积分余额：</text>
        <text class="balance-value">{{ pointsBalance }}</text>
      </view>
      <view class="record-link" @tap="goToRecords">
        <text class="record-link-text">查看兑换记录</text>
        <text class="record-link-arrow">></text>
      </view>
    </view>

    <!-- 筛选栏 -->
    <view class="filter-bar">
      <text class="filter-title">积分兑换</text>
      <view class="filter-option" @tap="filterExchangeable = !filterExchangeable">
        <view class="radio-circle" :class="{ active: filterExchangeable }"></view>
        <text class="filter-text">能兑换的</text>
      </view>
    </view>

    <!-- 商品列表 -->
    <view v-if="loading" class="loading-container">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <view v-else-if="displayItems.length === 0" class="empty-container">
      <text class="empty-icon">🎁</text>
      <text class="empty-text">暂无可兑换商品</text>
    </view>

    <view v-else class="goods-grid">
      <view class="goods-item" v-for="item in displayItems" :key="item.id" @tap="handleExchange(item)">
        <view class="goods-img-wrapper">
          <image
            v-if="item.productImage"
            :src="item.productImage"
            mode="aspectFill"
            class="goods-img"
          />
          <view v-else class="goods-img-placeholder">🍽</view>
        </view>
        <view class="goods-info">
          <text class="goods-name">{{ item.productName }}兑换券</text>
          <view class="goods-points-row">
            <text class="goods-points">{{ item.pointsRequired }}</text>
            <text class="goods-points-unit">积分</text>
          </view>
          <text v-if="item.remainCount > 0 && item.remainCount <= 5" class="goods-remain">
            仅剩{{ item.remainCount }}张
          </text>
          <text v-else-if="item.remainCount === 0" class="goods-sold-out">已兑完</text>
        </view>
      </view>
    </view>

    <view v-if="!loading && displayItems.length > 0" class="no-more">
      <text class="no-more-text">没有更多信息了~~</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getPointsMallList, exchangePoints } from '@/api/points.js'

const pointsBalance = ref(0)
const items = ref([])
const loading = ref(true)
const filterExchangeable = ref(false)

const displayItems = computed(() => {
  if (!filterExchangeable.value) return items.value
  return items.value.filter(item => {
    if (item.remainCount === -1) return true // 不限量
    return item.remainCount > 0
  })
})

const fetchData = async () => {
  loading.value = true
  try {
    const data = await getPointsMallList()
    pointsBalance.value = data.pointsBalance || 0
    items.value = data.items || []
  } catch (err) {
    console.error('获取积分商城失败:', err)
    uni.showToast({ title: err.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const goToRecords = () => {
  uni.showToast({ title: '兑换记录开发中', icon: 'none' })
}

const handleExchange = (item) => {
  if (item.remainCount === 0) {
    uni.showToast({ title: '该商品已兑完', icon: 'none' })
    return
  }
  if (item.pointsRequired > pointsBalance.value) {
    uni.showToast({ title: '积分不足', icon: 'none' })
    return
  }
  uni.showModal({
    title: '确认兑换',
    content: `确定使用 ${item.pointsRequired} 积分兑换「${item.productName}兑换券」？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          const voucherCode = await exchangePoints(item.id)
          uni.showModal({
            title: '兑换成功',
            content: `兑换券码：${voucherCode}`,
            showCancel: false
          })
          // 刷新列表和余额
          fetchData()
        } catch (err) {
          uni.showToast({ title: err.message || '兑换失败', icon: 'none' })
        }
      }
    }
  })
}

onMounted(() => {
  fetchData()
})

onPullDownRefresh(async () => {
  await fetchData()
  uni.stopPullDownRefresh()
})
</script>

<style lang="scss" scoped>
.exchange-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: env(safe-area-inset-bottom);
}

/* 余额卡片 */
.balance-card {
  background: #fff;
  margin: 24rpx;
  border-radius: 20rpx;
  padding: 36rpx 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.04);
}

.balance-row {
  display: flex;
  align-items: baseline;
}

.balance-label {
  font-size: 30rpx;
  color: #333;
}

.balance-value {
  font-size: 44rpx;
  font-weight: bold;
  color: #FF6B6B;
}

.record-link {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.record-link-text {
  font-size: 26rpx;
  color: #999;
}

.record-link-arrow {
  font-size: 26rpx;
  color: #ccc;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx 16rpx;
}

.filter-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.filter-option {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.radio-circle {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  border: 3rpx solid #ddd;
  transition: all 0.2s;
}

.radio-circle.active {
  border-color: #FF6B6B;
  background: #FF6B6B;
  box-shadow: inset 0 0 0 4rpx #fff;
}

.filter-text {
  font-size: 28rpx;
  color: #666;
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

/* 商品网格 */
.goods-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 0 20rpx;
  gap: 0;
}

.goods-item {
  width: 50%;
  padding: 12rpx;
  box-sizing: border-box;
}

.goods-img-wrapper {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 20rpx;
  overflow: hidden;
  background: #f8f8f8;
}

.goods-img {
  width: 100%;
  height: 100%;
}

.goods-img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 80rpx;
  background: #f0f0f0;
}

.goods-info {
  padding: 16rpx 8rpx 12rpx;
}

.goods-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-points-row {
  display: flex;
  align-items: baseline;
  gap: 6rpx;
  margin-top: 8rpx;
}

.goods-points {
  font-size: 38rpx;
  font-weight: bold;
  color: #FF6B6B;
}

.goods-points-unit {
  font-size: 24rpx;
  color: #999;
}

.goods-remain {
  font-size: 22rpx;
  color: #FF8E53;
  margin-top: 4rpx;
}

.goods-sold-out {
  font-size: 22rpx;
  color: #ccc;
  margin-top: 4rpx;
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
