<template>
  <view class="order-detail-page">
    <view class="loading-container" v-if="isLoading">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <template v-else-if="orderData">
      <view class="order-header">
        <view class="header-content">
          <view class="status-icon-wrapper">
            <text class="status-icon">{{ statusIcon }}</text>
          </view>
          <view class="header-text">
            <view class="order-status-badge" :class="statusClass">
              {{ statusText }}
            </view>
            <text class="order-no">订单号: {{ orderData.orderNo }}</text>
            <text class="order-time">{{ orderData.createdAt }}</text>
          </view>
        </view>
      </view>

      <!-- 待制作倒计时 -->
      <view class="countdown-bar" v-if="orderData.orderStatus === 1 && countdownSeconds > 0">
        <text class="countdown-icon">⏳</text>
        <text class="countdown-text">可取消剩余时间：{{ formatCountdown(countdownSeconds) }}</text>
      </view>
      <view class="countdown-bar expired" v-else-if="orderData.orderStatus === 1 && countdownSeconds <= 0">
        <text class="countdown-icon">⏰</text>
        <text class="countdown-text">取消时间已过，订单将自动开始制作</text>
      </view>

      <view class="order-items">
        <view class="section-title-row">
          <view class="section-title-bar"></view>
          <text class="section-title">商品明细</text>
        </view>
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
        <view class="section-title-row">
          <view class="section-title-bar"></view>
          <text class="section-title">金额明细</text>
        </view>
        <view class="summary-row">
          <text class="summary-label">商品总计</text>
          <text class="summary-value">¥{{ (orderData.totalAmount || orderData.payAmount).toFixed(2) }}</text>
        </view>
        <view class="summary-row" v-if="orderData.discountAmount > 0">
          <text class="summary-label">兑换券抵扣</text>
          <text class="summary-value discount">-¥{{ orderData.discountAmount.toFixed(2) }}</text>
        </view>
        <view class="summary-row summary-pay-row">
          <text class="summary-label">实付金额</text>
          <view class="summary-pay-wrapper">
            <text class="summary-pay-symbol">¥</text>
            <text class="summary-value pay">{{ orderData.payAmount.toFixed(2) }}</text>
          </view>
        </view>
        <view class="summary-row" v-if="orderData.orderStatus === 0">
          <text class="summary-label">就餐方式</text>
          <view class="dining-type-selector">
            <text
              class="dining-option"
              :class="{ active: orderData.diningType === '1' }"
              @tap="handleDiningTypeChange(1)"
            >堂食</text>
            <text
              class="dining-option"
              :class="{ active: orderData.diningType === '2' }"
              @tap="handleDiningTypeChange(2)"
            >打包</text>
          </view>
        </view>
        <view class="summary-row" v-else>
          <text class="summary-label">就餐方式</text>
          <text class="summary-value">{{ orderData.diningType === '1' ? '堂食' : '打包' }}</text>
        </view>
        <view class="summary-row" v-if="orderData.tableNumber">
          <text class="summary-label">桌号</text>
          <text class="summary-value">{{ orderData.tableNumber }}</text>
        </view>
        <!-- 订单备注：待支付状态可编辑 -->
        <view class="summary-row remark-edit-row" v-if="orderData.orderStatus === 0">
          <text class="summary-label">备注</text>
          <view class="remark-edit-wrapper">
            <textarea
              class="remark-textarea"
              v-model="remarkText"
              placeholder="请填写您的特殊要求，如：少辣、不要香菜、先上汤等"
              maxlength="200"
              :auto-height="true"
            />
            <text class="remark-count">{{ remarkText.length }}/200</text>
          </view>
        </view>
        <!-- 非待支付状态只读显示 -->
        <view class="summary-row" v-else-if="orderData.remark">
          <text class="summary-label">备注</text>
          <text class="summary-value remark">{{ orderData.remark }}</text>
        </view>
      </view>

      <!-- 待支付：取消 + 去支付 -->
      <view class="order-actions" v-if="orderData.orderStatus === 0">
        <button class="action-btn cancel-btn" @tap="handleCancelPending">取消订单</button>
        <button class="action-btn pay-btn" @tap="handlePay">去支付</button>
      </view>

      <!-- 待制作：取消订单（倒计时内） -->
      <view class="order-actions" v-else-if="orderData.orderStatus === 1">
        <button
          class="action-btn cancel-btn"
          @tap="handleCancel"
          :disabled="countdownSeconds <= 0"
        >取消订单</button>
      </view>

      <!-- 制作中：联系商家 -->
      <view class="order-actions" v-else-if="orderData.orderStatus === 2">
        <button class="action-btn done-btn" disabled>制作中，请耐心等待</button>
      </view>

      <!-- 已完成：去评价 + 再来一单 -->
      <view class="order-actions" v-else-if="orderData.orderStatus === 3">
        <button class="action-btn review-btn" @tap="goReview" v-if="!reviewData">去评价</button>
        <button class="action-btn reorder-btn" @tap="handleReorder">再来一单</button>
      </view>

      <!-- 已评价摘要 -->
      <view class="review-summary" v-if="reviewData && orderData.orderStatus === 3">
        <view class="review-header">
          <text class="review-title">我的评价</text>
          <view class="review-stars">
            <text v-for="i in 5" :key="i" class="review-star" :class="{ active: i <= reviewData.rating }">★</text>
          </view>
        </view>
        <text class="review-content" v-if="reviewData.content">{{ reviewData.content }}</text>
      </view>

      <!-- 已取消 / 已完成 -->
      <view class="order-actions" v-else>
        <button class="action-btn done-btn" disabled>{{ statusText }}</button>
      </view>
    </template>

    <view class="error-container" v-else>
      <text class="error-icon">📭</text>
      <text class="error-text">订单不存在</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { getOrderDetail, cancelOrder, payOrder, updateOrderDiningType, updateOrderRemark, checkReview } from '@/api/order.js'
import { addToCart } from '@/api/cart.js'

const orderData = ref(null)
const reviewData = ref(null)
const remarkText = ref('')
const isLoading = ref(true)
const countdownSeconds = ref(0)
let orderId = null
let countdownTimer = null

const statusText = computed(() => {
  if (!orderData.value) return ''
  const map = {
    0: '待支付',
    1: '待制作',
    2: '制作中',
    3: '已完成',
    4: '已取消'
  }
  return map[orderData.value.orderStatus] || '未知'
})

const statusIcon = computed(() => {
  if (!orderData.value) return ''
  const map = {
    0: '💳',
    1: '⏳',
    2: '👨‍🍳',
    3: '🎉',
    4: '❌'
  }
  return map[orderData.value.orderStatus] || '📋'
})

const statusClass = computed(() => {
  if (!orderData.value) return ''
  const status = orderData.value.orderStatus
  if (status === 0) return 'status-pending'
  if (status === 1) return 'status-waiting'
  if (status === 2) return 'status-processing'
  if (status === 3) return 'status-done'
  return 'status-cancelled'
})

function formatCountdown(seconds) {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

function startCountdown() {
  stopCountdown()
  if (!orderData.value || orderData.value.orderStatus !== 1 || !orderData.value.cancelDeadline) {
    countdownSeconds.value = 0
    return
  }

  const deadline = new Date(orderData.value.cancelDeadline).getTime()
  const now = Date.now()
  let remaining = Math.floor((deadline - now) / 1000)
  if (remaining < 0) remaining = 0
  countdownSeconds.value = remaining

  if (remaining > 0) {
    countdownTimer = setInterval(() => {
      remaining--
      countdownSeconds.value = remaining
      if (remaining <= 0) {
        stopCountdown()
        // 倒计时结束，刷新订单状态
        setTimeout(() => fetchOrderDetail(), 1000)
      }
    }, 1000)
  }
}

function stopCountdown() {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

const fetchOrderDetail = async () => {
  if (!orderId) return
  isLoading.value = true
  try {
    const data = await getOrderDetail(orderId)
    orderData.value = data
    remarkText.value = data.remark || ''
    startCountdown()
    // 已完成状态检查是否已评价
    if (data.orderStatus === 3) {
      try {
        const reviewRes = await checkReview(orderId)
        if (reviewRes && reviewRes.id) {
          reviewData.value = reviewRes
        }
      } catch (e) {}
    }
  } catch (err) {
    console.error('获取订单详情失败:', err)
    uni.showToast({ title: err.message || '加载失败', icon: 'none' })
  } finally {
    isLoading.value = false
  }
}

// 取消待支付订单
const handleCancelPending = async () => {
  uni.showModal({
    title: '提示',
    content: '确定要取消订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await cancelOrder(orderId)
          uni.showToast({ title: '订单已取消', icon: 'success' })
          setTimeout(() => {
            uni.navigateBack()
          }, 1000)
        } catch (err) {
          uni.showToast({ title: err.message || '取消失败', icon: 'none' })
        }
      }
    }
  })
}

// 取消待制作订单（倒计时内，自动退款）
const handleCancel = async () => {
  if (countdownSeconds.value <= 0) {
    uni.showToast({ title: '取消时间已过', icon: 'none' })
    return
  }
  uni.showModal({
    title: '取消订单',
    content: '确定取消订单吗？支付金额将原路退回。',
    success: async (res) => {
      if (res.confirm) {
        try {
          await cancelOrder(orderId)
          uni.showToast({ title: '订单已取消，退款已到账', icon: 'success' })
          stopCountdown()
          setTimeout(() => {
            uni.navigateBack()
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
          // 先保存备注
          if (remarkText.value.trim() !== (orderData.value.remark || '')) {
            await updateOrderRemark(orderId, remarkText.value.trim() || null)
          }
          await payOrder(orderId)
          uni.showToast({ title: '支付成功', icon: 'success' })
          fetchOrderDetail()
        } catch (err) {
          uni.showToast({ title: err.message || '支付失败', icon: 'none' })
        }
      }
    }
  })
}

const handleDiningTypeChange = async (type) => {
  if (orderData.value.diningType === String(type)) return
  try {
    await updateOrderDiningType(orderId, type)
    orderData.value.diningType = String(type)
    uni.showToast({ title: '已切换为' + (type === 1 ? '堂食' : '打包'), icon: 'none' })
  } catch (err) {
    uni.showToast({ title: err.message || '切换失败', icon: 'none' })
  }
}

const handleReorder = async () => {
  try {
    const items = orderData.value?.items || []
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

const goReview = () => {
  const orderNo = orderData.value?.orderNo || ''
  uni.navigateTo({
    url: `/pages/review/review?orderId=${orderId}&orderNo=${orderNo}`
  })
}

onLoad((options) => {
  orderId = options.id
  fetchOrderDetail()
})

// 从评价页返回时刷新评价状态
onShow(() => {
  if (orderId && orderData.value?.orderStatus === 3) {
    checkReview(orderId).then(res => {
      if (res && res.id) {
        reviewData.value = res
      }
    }).catch(() => {})
  }
})

onPullDownRefresh(async () => {
  await fetchOrderDetail()
  uni.stopPullDownRefresh()
})

onUnmounted(() => {
  stopCountdown()
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
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;
  gap: 16rpx;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
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

.error-icon {
  font-size: 80rpx;
}

.error-text {
  font-size: 28rpx;
  color: #FF6B6B;
}

/* 订单头部 */
.order-header {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  padding: 48rpx 32rpx;
  border-radius: 0 0 32rpx 32rpx;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.status-icon-wrapper {
  width: 80rpx;
  height: 80rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.status-icon {
  font-size: 40rpx;
}

.header-text {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.order-status-badge {
  display: inline-block;
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
}

.order-no {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.85);
}

.order-time {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.65);
}

/* 倒计时栏 */
.countdown-bar {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin: 20rpx 24rpx 0;
  padding: 20rpx 28rpx;
  background: #fff8e6;
  border-radius: 16rpx;
  border: 2rpx solid #ffe0a3;
}

.countdown-bar.expired {
  background: #fff0f0;
  border-color: #ffc8c8;
}

.countdown-icon {
  font-size: 32rpx;
}

.countdown-text {
  font-size: 26rpx;
  color: #e6a23c;
  font-weight: 500;
}

.countdown-bar.expired .countdown-text {
  color: #f56c6c;
}

/* 商品明细 */
.section-title-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 24rpx 28rpx 16rpx;
}

.section-title-bar {
  width: 6rpx;
  height: 28rpx;
  background: linear-gradient(180deg, #FF6B6B, #FF8E53);
  border-radius: 3rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.order-items {
  background-color: #fff;
  margin: 20rpx 24rpx 0;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.order-item {
  display: flex;
  padding: 20rpx 28rpx;
  border-bottom: 2rpx solid #f8f8f8;
}

.order-item:last-child {
  border-bottom: none;
}

.order-item-image {
  width: 130rpx;
  height: 130rpx;
  border-radius: 16rpx;
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
  flex-wrap: wrap;
}

.order-item-spec {
  font-size: 22rpx;
  color: #999;
  background-color: #f5f5f5;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
}

.order-item-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8rpx;
}

.order-item-price {
  font-size: 26rpx;
  color: #999;
}

.order-item-quantity {
  font-size: 26rpx;
  color: #666;
  font-weight: 500;
}

.order-item-subtotal {
  font-size: 30rpx;
  color: #333;
  font-weight: 600;
}

/* 金额明细 */
.order-summary {
  background-color: #fff;
  margin: 20rpx 24rpx 0;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 28rpx;
  border-bottom: 2rpx solid #f8f8f8;
}

.summary-row:last-child {
  border-bottom: none;
}

.summary-label {
  font-size: 28rpx;
  color: #666;
}

.summary-pay-wrapper {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}

.summary-pay-symbol {
  font-size: 24rpx;
  font-weight: 600;
  color: #FF6B6B;
}

.summary-value {
  font-size: 28rpx;
  color: #333;
}

.summary-value.pay {
  font-size: 36rpx;
  font-weight: bold;
  color: #FF6B6B;
}

.summary-value.discount {
  color: #67c23a;
  font-weight: 600;
}

.summary-pay-row {
  background-color: #fffaf7;
}

.dining-type-selector {
  display: flex;
  background-color: #f5f5f5;
  border-radius: 32rpx;
  padding: 4rpx;
}

.dining-option {
  font-size: 26rpx;
  color: #999;
  padding: 12rpx 32rpx;
  border-radius: 28rpx;
  transition: all 0.2s ease;

  &.active {
    background: linear-gradient(135deg, #FF6B6B, #FF8E53);
    color: #fff;
    font-weight: 600;
  }
}

.summary-value.remark {
  color: #999;
  font-size: 26rpx;
  max-width: 60%;
  text-align: right;
}

.remark-edit-row {
  flex-direction: column;
  align-items: flex-start;
}

.remark-edit-wrapper {
  width: 100%;
  margin-top: 16rpx;
}

.remark-textarea {
  width: 100%;
  min-height: 120rpx;
  background-color: #f8f8f8;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 26rpx;
  color: #333;
  box-sizing: border-box;
  line-height: 1.5;
}

.remark-count {
  display: block;
  text-align: right;
  font-size: 24rpx;
  color: #ccc;
  margin-top: 8rpx;
}

/* 操作按钮 */
.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 24rpx;
  padding: 40rpx 28rpx;
  padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
}

.action-btn {
  min-width: 200rpx;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: 44rpx;
  border: none;
  padding: 0 40rpx;
  transition: all 0.2s ease;
}

.action-btn:active {
  transform: scale(0.95);
}

.action-btn[disabled] {
  opacity: 0.6;
}

.cancel-btn {
  background-color: #fff;
  color: #666;
  border: 2rpx solid #e0e0e0;
}

.pay-btn {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  color: #fff;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 107, 0.3);
}

.done-btn {
  background-color: #f0f0f0;
  color: #999;
  min-width: 400rpx;
}

.reorder-btn {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  color: #fff;
  min-width: 400rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 107, 0.3);
}

.review-btn {
  background: linear-gradient(135deg, #ffd700 0%, #ff9500 100%);
  color: #fff;
  min-width: 280rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 149, 0, 0.3);
}

.review-summary {
  margin: 24rpx 0;
  background: #fff9f0;
  border-radius: 16rpx;
  padding: 28rpx 32rpx;
  border: 2rpx solid #ffe0b2;
}

.review-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.review-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.review-stars {
  display: flex;
  gap: 8rpx;
}

.review-star {
  font-size: 32rpx;
  color: #ddd;
}

.review-star.active {
  color: #ff9500;
}

.review-content {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}
</style>
