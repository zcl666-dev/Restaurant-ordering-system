<template>
  <view class="bill-page">
    <view class="page-header">
      <text class="page-title">订单列表</text>
    </view>

    <view class="loading-container" v-if="isLoading">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <view class="empty-container" v-else-if="orderList.length === 0">
      <text class="empty-icon">📋</text>
      <text class="empty-text">暂无订单</text>
      <text class="empty-sub">去点餐页下单吧</text>
      <view class="empty-btn" @tap="goToOrder">
        <text class="empty-btn-text">去点餐</text>
      </view>
    </view>

    <scroll-view v-else class="order-list" scroll-y>
      <view
        v-for="order in orderList"
        :key="order.id"
        class="order-card"
        @tap="goDetail(order.id)"
      >
        <!-- 卡片头部：自取标签 + 门店名 + 状态 -->
        <view class="card-header">
          <view class="header-left">
            <view class="dining-badge">
              <text class="dining-badge-text">{{ order.diningType === '2' ? '打包' : '堂食' }}</text>
            </view>
            <text class="store-name">308商业帝国（财院店）</text>
          </view>
          <text class="order-status">{{ statusText(order.orderStatus) }}</text>
        </view>
        <text class="order-time">{{ formatTime(order.createdAt) }}</text>

        <!-- 商品图片行 -->
        <view class="products-row" v-if="order.items && order.items.length > 0">
          <view class="product-thumb" v-for="(item, idx) in order.items.slice(0, 4)" :key="idx">
            <image class="thumb-image" :src="item.productImage" mode="aspectFill" />
            <text class="thumb-name">{{ item.productName }}</text>
          </view>
          <view class="product-price-info">
            <text class="total-price">¥{{ order.payAmount.toFixed(0) }}</text>
            <text class="total-count">共{{ order.itemCount }}件</text>
          </view>
        </view>

        <!-- 座位号 -->
        <view class="pickup-section">
          <text class="pickup-label">座位号：</text>
          <text class="pickup-number">{{ order.tableNumber || '0' }}</text>
        </view>

        <!-- 操作按钮 -->
        <view class="card-actions">
          <view class="action-btn-group">
            <view class="action-btn-wrapper" v-if="order.orderStatus === 4">
              <view class="scratch-badge">
                <text class="scratch-badge-text">参与刮奖</text>
              </view>
              <view class="action-btn" @tap.stop="goDetail(order.id)">
                <text class="action-btn-text">去评价</text>
              </view>
            </view>
            <view class="action-btn" @tap.stop="reorder(order)" v-if="order.orderStatus === 4 || order.orderStatus === 5">
              <text class="action-btn-text">再来一单</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getOrderList, getOrderDetail } from '@/api/order.js'
import { addToCart } from '@/api/cart.js'

const orderList = ref([])
const isLoading = ref(true)

const statusText = (status) => {
  const map = {
    0: '待支付',
    1: '已支付',
    2: '制作中',
    3: '待取餐',
    4: '已完成',
    5: '已取消',
    6: '已退款'
  }
  return map[status] || '未知'
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  const sec = String(d.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}:${sec}`
}

const fetchOrderList = async () => {
  isLoading.value = true
  try {
    const list = await getOrderList()
    orderList.value = list || []
  } catch (err) {
    console.error('获取订单列表失败:', err)
  } finally {
    isLoading.value = false
  }
}

const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/order-detail/order-detail?id=${id}` })
}

const goToOrder = () => {
  uni.switchTab({ url: '/pages/order/order' })
}

const reorder = async (order) => {
  try {
    const detail = await getOrderDetail(order.id)
    const items = detail.items || []
    if (items.length === 0) {
      uni.showToast({ title: '订单无商品', icon: 'none' })
      return
    }
    for (const item of items) {
      await addToCart({
        productId: item.productId,
        quantity: item.quantity,
        optionSnapshot: item.optionSnapshot || null
      })
    }
    uni.showToast({ title: '已加入购物车', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/order/order' })
    }, 500)
  } catch (err) {
    uni.showToast({ title: err.message || '操作失败', icon: 'none' })
  }
}

onMounted(() => {
  fetchOrderList()
})

onShow(() => {
  fetchOrderList()
})
</script>

<style lang="scss" scoped>
.bill-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 页面头部 */
.page-header {
  background-color: #fff;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.page-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #333;
}

/* 加载和空状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 300rpx;
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

.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 240rpx;
  gap: 12rpx;
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 16rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #666;
  font-weight: 500;
}

.empty-sub {
  font-size: 26rpx;
  color: #ccc;
  margin-bottom: 32rpx;
}

.empty-btn {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  padding: 18rpx 64rpx;
  border-radius: 40rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 107, 107, 0.3);

  &:active {
    transform: scale(0.95);
  }
}

.empty-btn-text {
  font-size: 28rpx;
  color: #fff;
  font-weight: 600;
}

/* 订单列表 */
.order-list {
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}

/* 订单卡片 */
.order-card {
  background: #fff;
  border-radius: 20rpx;
  margin-bottom: 24rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.04);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex: 1;
  min-width: 0;
}

.dining-badge {
  border: 2rpx solid #FF6B6B;
  border-radius: 8rpx;
  padding: 4rpx 12rpx;
  flex-shrink: 0;
}

.dining-badge-text {
  font-size: 22rpx;
  color: #FF6B6B;
  font-weight: 500;
}

.store-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-status {
  font-size: 26rpx;
  color: #999;
  flex-shrink: 0;
  margin-left: 16rpx;
}

.order-time {
  font-size: 24rpx;
  color: #ccc;
  margin-top: 8rpx;
}

/* 商品图片行 */
.products-row {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  margin-top: 24rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f5f5f5;
}

.product-thumb {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  width: 140rpx;
  flex-shrink: 0;
}

.thumb-image {
  width: 140rpx;
  height: 140rpx;
  border-radius: 14rpx;
  background-color: #f5f5f5;
}

.thumb-name {
  font-size: 22rpx;
  color: #666;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 140rpx;
}

.product-price-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  margin-left: auto;
  flex-shrink: 0;
}

.total-price {
  font-size: 34rpx;
  font-weight: 700;
  color: #333;
}

.total-count {
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
}

/* 取餐号 */
.pickup-section {
  display: flex;
  align-items: center;
  background: #f8f8f8;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  margin-top: 20rpx;
}

.pickup-label {
  font-size: 28rpx;
  color: #666;
}

.pickup-number {
  font-size: 48rpx;
  font-weight: 700;
  color: #333;
  margin-left: 8rpx;
  letter-spacing: 4rpx;
}

/* 操作按钮 */
.card-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20rpx;
}

.action-btn-group {
  display: flex;
  gap: 16rpx;
}

.action-btn-wrapper {
  position: relative;
}

.scratch-badge {
  position: absolute;
  top: -16rpx;
  right: -8rpx;
  background: #FF6B6B;
  border-radius: 8rpx;
  padding: 2rpx 10rpx;
  z-index: 1;
}

.scratch-badge-text {
  font-size: 18rpx;
  color: #fff;
  font-weight: 500;
  white-space: nowrap;
}

.action-btn {
  border: 2rpx solid #e0e0e0;
  border-radius: 32rpx;
  padding: 14rpx 36rpx;
  background: #fff;

  &:active {
    background: #f8f8f8;
  }
}

.action-btn-text {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
}
</style>
