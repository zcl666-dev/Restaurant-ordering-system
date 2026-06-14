<template>
  <view class="index-page">
    <!-- 桌号提示栏 -->
    <view class="table-bar" v-if="tableNo">
      <view class="table-bar-inner">
        <text class="table-icon">🪑</text>
        <text class="table-text">当前桌号：<text class="table-no">{{ tableNo }}</text></text>
      </view>
    </view>
    <view class="table-bar table-bar-hint" v-else>
      <view class="table-bar-inner">
        <text class="table-icon">📱</text>
        <text class="table-text">请扫描桌台二维码或选择堂食/外带</text>
      </view>
    </view>

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

    <!-- 自定义 TabBar -->
    <TabBar />
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { saveTableNo, getTableNo } from '../../utils/tableStorage.js'
import TabBar from '@/components/TabBar/TabBar.vue'

const images = reactive([
  { src: '/static/pic1.jpg', loaded: false, error: false },
  { src: '/static/pic2.png', loaded: false, error: false },
  { src: '/static/pic3.jpg', loaded: false, error: false }
])

const currentIndex = ref(0)
const tableNo = ref('')

const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了 🌙'
  if (hour < 11) return '早上好 ☀️'
  if (hour < 14) return '中午好 🌤'
  if (hour < 18) return '下午好 🌅'
  return '晚上好 🌙'
})

/**
 * 解析 scene 参数，提取桌号
 * 微信小程序扫码进入时，scene 值格式为 "tableNo=A01"
 */
function parseScene(scene) {
  if (!scene) return null
  // scene 可能是 URL 编码的，先解码
  const decoded = decodeURIComponent(scene)
  // 支持 "tableNo=A01" 和直接 "A01" 两种格式
  if (decoded.includes('tableNo=')) {
    return decoded.split('tableNo=')[1]
  }
  return decoded
}

/**
 * 页面加载生命周期（UniApp）
 * 扫码进入时微信自动传入 options.scene
 */
onLoad((options) => {
  console.log('[index] onLoad options:', JSON.stringify(options))

  // 优先从 scene 参数获取桌号（扫码进入）
  if (options && options.scene) {
    const parsedTableNo = parseScene(options.scene)
    if (parsedTableNo) {
      tableNo.value = parsedTableNo
      saveTableNo(parsedTableNo)
      uni.showToast({ title: `已识别桌号: ${parsedTableNo}`, icon: 'success' })
      return
    }
  }

  // 其次从 options.tableNo 获取（直接页面跳转）
  if (options && options.tableNo) {
    tableNo.value = options.tableNo
    saveTableNo(options.tableNo)
    uni.showToast({ title: `已识别桌号: ${options.tableNo}`, icon: 'success' })
    return
  }

  // 最后从本地存储读取（之前扫码保存过的）
  const stored = getTableNo()
  if (stored) {
    tableNo.value = stored
  }
})

onMounted(() => {
  // 兜底：如果 onLoad 没拿到，再从存储读一次
  if (!tableNo.value) {
    const stored = getTableNo()
    if (stored) {
      tableNo.value = stored
    }
  }
})

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
  padding-bottom: calc(110rpx + env(safe-area-inset-bottom));
}

/* 桌号提示栏 */
.table-bar {
  background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
  padding: 20rpx 32rpx;
}

.table-bar-hint {
  background: linear-gradient(135deg, #909399 0%, #606266 100%);
}

.table-bar-inner {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.table-icon {
  font-size: 32rpx;
}

.table-text {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
}

.table-no {
  font-size: 30rpx;
  font-weight: 700;
  color: #fff;
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
