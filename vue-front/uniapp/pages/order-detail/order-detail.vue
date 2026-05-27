<template>
  <view class="order-detail-page">
    <view class="loading-container" v-if="isLoading">
      <text class="loading-text">加载中...</text>
    </view>

    <template v-else-if="orderData">
      <view class="order-header">
        <view class="order-status-badge" :class="statusClass">
          {{ statusText }}
        </view>
        <text class="order-no">订单号: {{ orderData.orderNo }}</text>
        <text class="order-time">{{ orderData.createdAt }}</text>
      </view>

      <view class="order-items">
        <view class="section-title">商品明细</view>
        <view
          v-for="(item, idx) in orderData.items"
          :key="idx"
          class="order-item"
        >
          <image
            class="order-item-image"
            :src="item.productImage"
            mode="aspectFill"
          />
          <view class="order-item-info">
            <text class="order-item-name">{{ item.productName }}</text>
            <view class="order-item-specs" v-if="item.options && item.options.length > 0">
              <text class="order-item-spec" v-for="(opt, oidx) in item.options" :key="oidx">
                {{ opt.groupName }}: {{ opt.valueName }}
              </text>
            </view>
            <view class="order-item-bottom">
              <text class="order-item-price">¥{{ item.unitPrice.toFixed(2) }}</text>
              <text class="order-item-quantity">x{{ item.quantity }}</text>
              <text class="order-item-subtotal">¥{{ item.subtotalAmount.toFixed(2) }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="order-summary">
        <view class="section-title">金额明细</view>
        <view class="summary-row">
          <text class="summary-label">实付金额</text>
          <text class="summary-value pay">¥{{ orderData.payAmount.toFixed(2) }}</text>
        </view>
        <view class="summary-row" v-if="orderData.remark">
          <text class="summary-label">备注</text>
          <text class="summary-value remark">{{ orderData.remark }}</text>
        </view>
      </view>

      <view class="order-actions" v-if="orderData.orderStatus === 0">
        <button class="action-btn cancel-btn" @tap="handleCancel">取消订单</button>
        <button class="action-btn pay-btn" @tap="handlePay">去支付</button>
      </view>

      <view class="order-actions" v-else>
        <button class="action-btn done-btn" disabled>{{ statusText }}</button>
      </view>
    </template>

    <view class="error-container" v-else>
      <text class="error-text">订单不存在</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getOrderDetail, cancelOrder, payOrder } from '@/api/order.js'

const orderData = ref(null)
const isLoading = ref(true)
let orderId = null

const statusText = computed(() => {
  if (!orderData.value) return ''
  const map = {
    0: '待支付',
    1: '已支付',
    2: '制作中',
    3: '待取餐',
    4: '已完成',
    5: '已取消',
    6: '已退款'
  }
  return map[orderData.value.orderStatus] || '未知'
})

const statusClass = computed(() => {
  if (!orderData.value) return ''
  const status = orderData.value.orderStatus
  if (status === 0) return 'status-pending'
  if (status === 5 || status === 6) return 'status-cancelled'
  return 'status-done'
})

const fetchOrderDetail = async () => {
  if (!orderId) return
  isLoading.value = true
  try {
    const data = await getOrderDetail(orderId)
    orderData.value = data
  } catch (err) {
    console.error('获取订单详情失败:', err)
    uni.showToast({ title: err.message || '加载失败', icon: 'none' })
  } finally {
    isLoading.value = false
  }
}

const handleCancel = async () => {
  uni.showModal({
    title: '提示',
    content: '确定要取消订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await cancelOrder(orderId)
          uni.showToast({ title: '订单已取消', icon: 'success' })
          setTimeout(() => {
            uni.switchTab({ url: '/pages/bill/bill' })
          }, 1000)
        } catch (err) {
          uni.showToast({ title: err.message || '取消失败', icon: 'none' })
        }
      }
    }
  })
}

const handlePay = async () => {
  uni.showModal({
    title: '确认支付',
    content: `需支付 ¥${orderData.value.payAmount.toFixed(2)}`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await payOrder(orderId)
          uni.showToast({ title: '支付成功', icon: 'success' })
          setTimeout(() => {
            uni.switchTab({ url: '/pages/bill/bill' })
          }, 1000)
        } catch (err) {
          uni.showToast({ title: err.message || '支付失败', icon: 'none' })
        }
      }
    }
  })
}

onLoad((options) => {
  orderId = options.id
  fetchOrderDetail()
})
</script>

<style lang="scss" scoped>
.order-detail-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 40rpx;
}

.loading-container,
.error-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;
}

.loading-text {
  font-size: 28rpx;
  color: #999;
}

.error-text {
  font-size: 28rpx;
  color: #FF6B6B;
}

.order-header {
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  padding: 40rpx 30rpx;
  color: #fff;
}

.order-status-badge {
  display: inline-block;
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 16rpx;
}

.order-no {
  display: block;
  font-size: 24rpx;
  opacity: 0.9;
  margin-bottom: 8rpx;
}

.order-time {
  display: block;
  font-size: 22rpx;
  opacity: 0.7;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  padding: 24rpx 30rpx 16rpx;
}

.order-items {
  background-color: #fff;
  margin-top: 20rpx;
}

.order-item {
  display: flex;
  padding: 20rpx 30rpx;
  border-bottom: 2rpx solid #f5f5f5;
}

.order-item:last-child {
  border-bottom: none;
}

.order-item-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 12rpx;
  background-color: #f5f5f5;
  flex-shrink: 0;
}

.order-item-info {
  flex: 1;
  margin-left: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.order-item-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.order-item-specs {
  display: flex;
  gap: 8rpx;
  margin-top: 6rpx;
}

.order-item-spec {
  font-size: 22rpx;
  color: #999;
  background-color: #f5f5f5;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
}

.order-item-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8rpx;
}

.order-item-price {
  font-size: 28rpx;
  color: #FF6B6B;
  font-weight: bold;
}

.order-item-quantity {
  font-size: 26rpx;
  color: #666;
}

.order-item-subtotal {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.order-summary {
  background-color: #fff;
  margin-top: 20rpx;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 30rpx;
  border-bottom: 2rpx solid #f5f5f5;
}

.summary-row:last-child {
  border-bottom: none;
}

.summary-label {
  font-size: 26rpx;
  color: #666;
}

.summary-value {
  font-size: 26rpx;
  color: #333;
}

.summary-value.pay {
  font-size: 32rpx;
  font-weight: bold;
  color: #FF6B6B;
}

.summary-value.remark {
  color: #999;
  font-size: 24rpx;
  max-width: 60%;
  text-align: right;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 24rpx;
  padding: 40rpx 30rpx;
}

.action-btn {
  min-width: 180rpx;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: bold;
  border-radius: 40rpx;
  border: none;
  padding: 0 40rpx;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #666;
}

.pay-btn {
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  color: #fff;
}

.done-btn {
  background-color: #e0e0e0;
  color: #999;
}
</style>