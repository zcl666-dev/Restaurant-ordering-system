<template>
  <view class="review-page">
    <!-- 订单信息 -->
    <view class="order-info-card">
      <text class="order-label">订单号</text>
      <text class="order-no">{{ orderNo }}</text>
    </view>

    <!-- 星级评分 -->
    <view class="rating-section">
      <text class="section-title">服务评分</text>
      <view class="star-row">
        <view
          v-for="i in 5"
          :key="i"
          class="star-item"
          @tap="setRating(i)"
        >
          <text class="star" :class="{ active: i <= rating }">{{ i <= rating ? '★' : '☆' }}</text>
        </view>
      </view>
      <text class="rating-text">{{ ratingText }}</text>
    </view>

    <!-- 文字评价 -->
    <view class="content-section">
      <text class="section-title">评价内容（选填）</text>
      <textarea
        class="review-textarea"
        v-model="content"
        placeholder="分享您的用餐体验，帮助我们改进服务..."
        maxlength="500"
        :auto-height="false"
      />
      <text class="char-count">{{ content.length }}/500</text>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-section">
      <button
        class="submit-btn"
        :disabled="!rating || submitting"
        @tap="handleSubmit"
      >
        {{ submitting ? '提交中...' : '提交评价' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createReview } from '@/api/order.js'

const orderId = ref(null)
const orderNo = ref('')
const rating = ref(0)
const content = ref('')
const submitting = ref(false)

const ratingText = computed(() => {
  const texts = ['', '非常差', '较差', '一般', '满意', '非常满意']
  return texts[rating.value] || '请评分'
})

function setRating(val) {
  rating.value = val
}

onLoad((options) => {
  orderId.value = options.orderId
  orderNo.value = options.orderNo || ''
})

async function handleSubmit() {
  if (!rating.value) {
    uni.showToast({ title: '请选择评分', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    await createReview({
      orderId: Number(orderId.value),
      rating: rating.value,
      content: content.value || null
    })
    uni.showToast({ title: '评价成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (err) {
    uni.showToast({ title: err.message || '评价失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.review-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 24rpx;
}

.order-info-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  display: flex;
  align-items: center;
}

.order-label {
  font-size: 28rpx;
  color: #999;
  margin-right: 16rpx;
}

.order-no {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.rating-section, .content-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 24rpx;
  display: block;
}

.star-row {
  display: flex;
  gap: 24rpx;
  margin-bottom: 16rpx;
}

.star-item {
  padding: 8rpx;
}

.star {
  font-size: 64rpx;
  color: #ddd;
  transition: color 0.2s;
}

.star.active {
  color: #ff9500;
}

.rating-text {
  font-size: 26rpx;
  color: #ff9500;
}

.review-textarea {
  width: 100%;
  min-height: 240rpx;
  background: #f8f8f8;
  border-radius: 12rpx;
  padding: 24rpx;
  font-size: 28rpx;
  color: #333;
  box-sizing: border-box;
}

.char-count {
  display: block;
  text-align: right;
  font-size: 24rpx;
  color: #ccc;
  margin-top: 12rpx;
}

.submit-section {
  margin-top: 48rpx;
  padding: 0 16rpx;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: linear-gradient(135deg, #ff6b35, #ff4500);
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  border-radius: 44rpx;
  border: none;
}

.submit-btn[disabled] {
  background: #ccc;
  color: #999;
}
</style>
