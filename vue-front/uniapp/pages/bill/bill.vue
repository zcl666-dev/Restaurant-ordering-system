<template>
  <view class="bill-page">
    <view class="loading-container" v-if="isLoading">
      <text class="loading-text">加载中...</text>
    </view>

    <view class="empty-container" v-else-if="orderList.length === 0">
      <text class="empty-icon">📋</text>
      <text class="empty-text">暂无订单</text>
      <text class="empty-sub">去点餐页下单吧</text>
    </view>

    <scroll-view v-else class="order-list" scroll-y>
      <view
        v-for="order in orderList"
        :key="order.id"
        class="order-card"
        @tap="goDetail(order.id)"
      >
        <view class="order-card-header">
          <text class="order-no">订单号: {{ order.orderNo }}</text>
          <text class="order-status" :class="statusClass(order.orderStatus)">
            {{ statusText(order.orderStatus) }}
          </text>
        </view>
        <view class="order-card-body">
          <text class="order-amount">¥{{ order.payAmount.toFixed(2) }}</text>
          <text class="order-time">{{ order.createdAt }}</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getOrderList } from '@/api/order.js'

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

const statusClass = (status) => {
  if (status === 0) return 'status-pending'
  if (status === 5 || status === 6) return 'status-cancelled'
  return 'status-done'
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
  uni.navigateTo({
    url: `/pages/order-detail/order-detail?id=${id}`
  })
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

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-top: 200rpx;
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
  padding-top: 200rpx;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 30rpx;
  color: #999;
  margin-bottom: 12rpx;
}

.empty-sub {
  font-size: 26rpx;
  color: #ccc;
}

.order-list {
  padding: 20rpx;
}

.order-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.04);
}

.order-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.order-no {
  font-size: 24rpx;
  color: #999;
}

.order-status {
  font-size: 24rpx;
  font-weight: bold;
}

.status-pending {
  color: #FF6B6B;
}

.status-cancelled {
  color: #999;
}

.status-done {
  color: #07c160;
}

.order-card-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-amount {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.order-time {
  font-size: 24rpx;
  color: #ccc;
}
</style>