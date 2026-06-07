<template>
  <view class="index-page">
    <!-- 欢迎区域 -->
    <view class="welcome-section">
      <view class="welcome-text">
        <text class="welcome-greeting">{{ greetingText }}</text>
        <text class="welcome-sub">今天想吃点什么？</text>
      </view>
    </view>

    <!-- 轮播图 -->
    <view class="swiper-container">
      <swiper
        class="swiper-wrapper"
        :indicator-dots="true"
        :indicator-color="'rgba(255, 255, 255, 0.4)'"
        :indicator-active-color="'#ffffff'"
        :autoplay="true"
        :interval="3000"
        :duration="500"
        :circular="true"
        @change="onSwiperChange"
      >
        <swiper-item
          v-for="(image, index) in images"
          :key="index"
          class="swiper-item"
        >
          <image
            class="swiper-image"
            :src="image.src"
            mode="aspectFill"
            :lazy-load="true"
            @error="onImageError(index)"
            @load="onImageLoad(index)"
            :class="{ 'image-loaded': image.loaded, 'image-error': image.error }"
          />
          <view v-if="image.error" class="error-placeholder">
            <text class="error-text">图片加载失败</text>
          </view>
        </swiper-item>
      </swiper>
    </view>

    <!-- 快捷入口 -->
    <view class="quick-entry-section">
      <view class="quick-entry-grid">
        <view class="quick-entry-item" @tap="goToOrder">
          <view class="entry-icon-wrapper entry-icon-order">
            <text class="entry-icon">🍜</text>
          </view>
          <text class="entry-text">点餐</text>
        </view>
        <view class="quick-entry-item" @tap="goToBill">
          <view class="entry-icon-wrapper entry-icon-bill">
            <text class="entry-icon">📋</text>
          </view>
          <text class="entry-text">订单</text>
        </view>
        <view class="quick-entry-item" @tap="goToUser">
          <view class="entry-icon-wrapper entry-icon-points">
            <text class="entry-icon">⭐</text>
          </view>
          <text class="entry-text">积分</text>
        </view>
        <view class="quick-entry-item" @tap="goToUser">
          <view class="entry-icon-wrapper entry-icon-user">
            <text class="entry-icon">👤</text>
          </view>
          <text class="entry-text">我的</text>
        </view>
      </view>
    </view>

    <!-- 热门推荐引导 -->
    <view class="recommend-section">
      <view class="section-header">
        <view class="section-title-wrapper">
          <view class="section-title-bar"></view>
          <text class="section-title">热门推荐</text>
        </view>
        <text class="section-more" @tap="goToOrder">去看看 →</text>
      </view>
      <view class="recommend-hint" @tap="goToOrder">
        <text class="recommend-hint-text">点击进入点餐页面，探索美味佳肴</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

const images = reactive([
  { src: '/static/pic1.jpg', loaded: false, error: false },
  { src: '/static/pic2.png', loaded: false, error: false },
  { src: '/static/pic3.jpg', loaded: false, error: false }
])

const currentIndex = ref(0)

const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了 🌙'
  if (hour < 11) return '早上好 ☀️'
  if (hour < 14) return '中午好 🌤'
  if (hour < 18) return '下午好 🌅'
  return '晚上好 🌙'
})

// 页面加载时获取桌号参数
onMounted(() => {
  // #ifdef MP-WEIXIN
  // 小程序环境下从页面参数获取桌号
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage?.options || {}
  if (options.tableNo) {
    uni.setStorageSync('tableNo', options.tableNo)
    uni.showToast({
      title: `已识别桌号: ${options.tableNo}`,
      icon: 'success'
    })
  }
  // #endif
})

// 页面显示时也检查参数（处理扫码进入场景）
const onLoad = (options) => {
  if (options && options.tableNo) {
    uni.setStorageSync('tableNo', options.tableNo)
    uni.showToast({
      title: `已识别桌号: ${options.tableNo}`,
      icon: 'success'
    })
  }
}

// 导出onLoad供页面使用
defineExpose({ onLoad })

const onSwiperChange = (e) => {
  currentIndex.value = e.detail.current
}

const onImageLoad = (index) => {
  images[index].loaded = true
  images[index].error = false
}

const onImageError = (index) => {
  images[index].error = true
  images[index].loaded = false
}

const goToOrder = () => {
  uni.switchTab({ url: '/pages/order/order' })
}

const goToBill = () => {
  uni.switchTab({ url: '/pages/bill/bill' })
}

const goToUser = () => {
  uni.switchTab({ url: '/pages/user/user' })
}
</script>

<style lang="scss" scoped>
.index-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 欢迎区域 */
.welcome-section {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  padding: 40rpx 32rpx 50rpx;
  border-radius: 0 0 32rpx 32rpx;
}

.welcome-text {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.welcome-greeting {
  font-size: 40rpx;
  font-weight: 700;
  color: #fff;
}

.welcome-sub {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.85);
}

/* 轮播图 */
.swiper-container {
  width: 100%;
  padding: 0 24rpx;
  margin-top: -20rpx;
  position: relative;
  z-index: 10;
}

.swiper-wrapper {
  width: 100%;
  height: 340rpx;
  border-radius: 20rpx;
  overflow: hidden;
}

.swiper-item {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20rpx;
  overflow: hidden;
}

.swiper-image {
  width: 100%;
  height: 100%;
  opacity: 0;
  transition: opacity 0.3s ease-in-out;
  border-radius: 20rpx;
}

.swiper-image.image-loaded {
  opacity: 1;
}

.swiper-image.image-error {
  opacity: 0.5;
}

.error-placeholder {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: rgba(0, 0, 0, 0.6);
  padding: 20rpx 40rpx;
  border-radius: 10rpx;
}

.error-text {
  color: #ffffff;
  font-size: 28rpx;
}

/* 快捷入口 */
.quick-entry-section {
  margin: 30rpx 24rpx 0;
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 36rpx 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.quick-entry-grid {
  display: flex;
  justify-content: space-around;
}

.quick-entry-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14rpx;
  transition: transform 0.2s ease;

  &:active {
    transform: scale(0.92);
  }
}

.entry-icon-wrapper {
  width: 96rpx;
  height: 96rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.entry-icon-order {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
}

.entry-icon-bill {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.entry-icon-points {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.entry-icon-user {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.entry-icon {
  font-size: 44rpx;
}

.entry-text {
  font-size: 24rpx;
  color: #333;
  font-weight: 500;
}

/* 热门推荐 */
.recommend-section {
  margin: 24rpx 24rpx 0;
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.section-title-wrapper {
  display: flex;
  align-items: center;
  gap: 12rpx;
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

.section-more {
  font-size: 24rpx;
  color: #FF6B6B;
}

.recommend-hint {
  background: linear-gradient(135deg, #fff5f5 0%, #fff0f0 100%);
  border-radius: 16rpx;
  padding: 40rpx 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx dashed rgba(255, 107, 107, 0.3);
  transition: transform 0.2s ease;

  &:active {
    transform: scale(0.98);
  }
}

.recommend-hint-text {
  font-size: 26rpx;
  color: #FF6B6B;
}
</style>
