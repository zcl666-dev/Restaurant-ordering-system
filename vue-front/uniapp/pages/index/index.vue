<template>
  <view class="index-page">
    <!-- 顶部店铺信息区域 -->
    <view class="header-section" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="shop-info">
          <text class="shop-name">308商业帝国</text>
          <text class="shop-subtitle">财院店</text>
          <view class="shop-status">
            <view class="status-dot"></view>
            <text class="status-text">营业中</text>
          </view>
        </view>
        <view class="table-info" v-if="tableNo">
          <text class="table-label">桌号</text>
          <text class="table-number">{{ tableNo }}</text>
        </view>
      </view>
    </view>

    <!-- Banner轮播图 -->
    <view class="banner-section">
      <swiper
        class="banner-swiper"
        :indicator-dots="true"
        :indicator-color="'rgba(255, 255, 255, 0.4)'"
        :indicator-active-color="'#ffffff'"
        :autoplay="true"
        :interval="3000"
        :duration="500"
        :circular="true"
      >
        <swiper-item v-for="(banner, index) in bannerList" :key="index">
          <image class="banner-image" :src="banner" mode="aspectFill" />
        </swiper-item>
      </swiper>
    </view>

    <!-- 公告栏 -->
    <view class="notice-section">
      <view class="notice-card">
        <view class="notice-left">
          <text class="notice-icon">📢</text>
          <text class="notice-label">帝国公告</text>
        </view>
        <view class="notice-content">
          <swiper
            class="notice-swiper"
            :vertical="true"
            :autoplay="true"
            :interval="3000"
            :circular="true"
            :show-indicator="false"
          >
            <swiper-item v-for="(notice, index) in noticeList" :key="index">
              <text class="notice-text">{{ notice }}</text>
            </swiper-item>
          </swiper>
        </view>
      </view>
    </view>

    <!-- 个人中心卡片 -->
    <view class="user-card-section">
      <view class="user-card" @tap="goToUser">
        <image class="user-card-bg" src="https://zcl-library-management-system.oss-cn-beijing.aliyuncs.com/宣传图/4.jpg" mode="aspectFill" />
        <view class="user-card-mask">
          <view class="user-card-left">
            <image class="user-avatar" :src="userInfo.avatar || '/static/default-avatar.png'" mode="aspectFill" />
            <view class="user-name-wrapper">
              <text class="user-name">{{ userInfo.nickName || '未登录' }}</text>
              <text class="user-tip" v-if="!userInfo.nickName">点击登录账号</text>
            </view>
          </view>
          <view class="user-card-right">
            <view class="user-stat">
              <text class="stat-value">{{ userInfo.couponCount || 0 }}</text>
              <text class="stat-label">优惠券</text>
            </view>
            <view class="stat-divider"></view>
            <view class="user-stat">
              <text class="stat-value">{{ userInfo.balance || '0.00' }}</text>
              <text class="stat-label">余额</text>
            </view>
            <view class="stat-divider"></view>
            <view class="user-stat">
              <text class="stat-value">{{ userInfo.points || 0 }}</text>
              <text class="stat-label">积分</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 功能导航区 -->
    <view class="nav-section">
      <view class="nav-grid">
        <view class="nav-item" @tap="goToOrder">
          <view class="nav-icon-wrapper">
            <text class="nav-icon">📋</text>
          </view>
          <text class="nav-text">我的订单</text>
        </view>
        <view class="nav-item" @tap="goToProfile">
          <view class="nav-icon-wrapper">
            <text class="nav-icon">👤</text>
          </view>
          <text class="nav-text">个人信息</text>
        </view>
        <view class="nav-item" @tap="goToPoints">
          <view class="nav-icon-wrapper">
            <text class="nav-icon">🎁</text>
          </view>
          <text class="nav-text">积分商城</text>
        </view>
      </view>
    </view>

    <!-- 推荐商品 -->
    <view class="recommend-section">
      <view class="section-header">
        <view class="section-title-wrapper">
          <text class="section-icon">🔥</text>
          <text class="section-title">帝国推荐</text>
        </view>
      </view>
      <scroll-view class="recommend-scroll" scroll-x="true" :show-scrollbar="false">
        <view class="recommend-list">
          <view
            class="recommend-item"
            v-for="product in recommendList"
            :key="product.id"
            @tap="goToProductDetail(product.id)"
          >
            <image class="recommend-image" :src="product.productImage" mode="aspectFill" />
            <view class="recommend-info">
              <text class="recommend-name">{{ product.productName }}</text>
              <text class="recommend-desc">{{ product.desc || '美味推荐' }}</text>
              <view class="recommend-bottom">
                <text class="recommend-price">¥{{ product.price }}</text>
                <text class="recommend-sales">月售{{ product.salesCount || 0 }}</text>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 底部留白 -->
    <view class="bottom-spacer"></view>

    <!-- 自定义 TabBar -->
    <TabBar />
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { saveTableNo, getTableNo } from '../../utils/tableStorage.js'
import { getUserInfo } from '../../api/user.js'
import { getRecommendProducts } from '../../api/product.js'
import TabBar from '@/components/TabBar/TabBar.vue'

// Banner轮播图
const bannerList = reactive([
  'https://zcl-library-management-system.oss-cn-beijing.aliyuncs.com/宣传图/1.jpg',
  'https://zcl-library-management-system.oss-cn-beijing.aliyuncs.com/宣传图/2.jpg',
  'https://zcl-library-management-system.oss-cn-beijing.aliyuncs.com/宣传图/3.jpg'
])

// 公告列表
const noticeList = reactive([
  '308商业帝国，舌尖上的美食疆域',
  '入主308帝国，尝遍人间风味',
  '美食筑帝国，风味聚308',
  '问鼎308商业帝国，尽享地道好滋味'
])

// 状态栏高度
const statusBarHeight = ref(0)

// 桌号
const tableNo = ref('')

// 用户信息
const userInfo = reactive({
  avatar: '',
  nickName: '',
  points: 0,
  balance: '0.00',
  couponCount: 0
})

// 推荐商品列表
const recommendList = ref([])

/**
 * 解析 scene 参数，提取桌号
 */
function parseScene(scene) {
  if (!scene) return null
  const decoded = decodeURIComponent(scene)
  if (decoded.includes('tableNo=')) {
    return decoded.split('tableNo=')[1]
  }
  return decoded
}

/**
 * 页面加载生命周期
 */
onLoad((options) => {
  // 获取状态栏高度
  const sysInfo = uni.getSystemInfoSync()
  statusBarHeight.value = sysInfo.statusBarHeight || 0

  console.log('[index] onLoad options:', JSON.stringify(options))

  // 优先从 scene 参数获取桌号（扫码进入）
  if (options && options.scene) {
    const parsedTableNo = parseScene(options.scene)
    if (parsedTableNo) {
      tableNo.value = parsedTableNo
      saveTableNo(parsedTableNo)
      return
    }
  }

  // 其次从 options.tableNo 获取
  if (options && options.tableNo) {
    tableNo.value = options.tableNo
    saveTableNo(options.tableNo)
    return
  }

  // 最后从本地存储读取
  const stored = getTableNo()
  if (stored) {
    tableNo.value = stored
  }
})

/**
 * 页面显示时加载数据
 */
onShow(() => {
  loadUserInfo()
  loadRecommendProducts()
})

/**
 * 加载用户信息
 */
const loadUserInfo = async () => {
  const token = uni.getStorageSync('token')
  if (!token) return

  try {
    const data = await getUserInfo()
    if (data) {
      userInfo.avatar = data.avatarUrl || ''
      userInfo.nickName = data.nickName || ''
      userInfo.points = data.points || 0
      userInfo.balance = data.balance || '0.00'
      userInfo.couponCount = data.couponCount || 0
    }
  } catch (e) {
    console.log('获取用户信息失败', e)
  }
}

/**
 * 加载推荐商品
 */
const loadRecommendProducts = async () => {
  try {
    const data = await getRecommendProducts()
    if (data && Array.isArray(data)) {
      recommendList.value = data.slice(0, 10)
    }
  } catch (e) {
    console.log('获取推荐商品失败', e)
  }
}

// 页面跳转
const goToOrder = () => {
  uni.switchTab({ url: '/pages/order/order' })
}

const goToBill = () => {
  uni.switchTab({ url: '/pages/bill/bill' })
}

const goToUser = () => {
  uni.switchTab({ url: '/pages/user/user' })
}

const goToProfile = () => {
  uni.navigateTo({ url: '/pages/profile/profile' })
}

const goToPoints = () => {
  uni.navigateTo({ url: '/pages/points/points' })
}

const goToProductDetail = (id) => {
  uni.navigateTo({ url: `/pages/product-detail/product-detail?id=${id}` })
}
</script>

<style lang="scss" scoped>
.index-page {
  min-height: 100vh;
  background-color: #FE7C5E;
  padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
}

/* ========== 顶部店铺信息 ========== */
.header-section {
  background: linear-gradient(135deg, #FE7C5E 0%, #FF9B7B 100%);
  padding: 20rpx 32rpx 50rpx;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.shop-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.shop-name {
  font-size: 40rpx;
  font-weight: 700;
  color: #ffffff;
}

.shop-subtitle {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.85);
}

.shop-status {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 8rpx;
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background-color: #4ade80;
}

.status-text {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.9);
}

.table-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: rgba(255, 255, 255, 0.2);
  padding: 16rpx 24rpx;
  border-radius: 16rpx;
}

.table-label {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.8);
}

.table-number {
  font-size: 36rpx;
  font-weight: 700;
  color: #ffffff;
}

/* ========== Banner轮播图 ========== */
.banner-section {
  padding: 0 24rpx;
  margin-top: -20rpx;
  position: relative;
  z-index: 10;
}

.banner-swiper {
  width: 100%;
  height: 420rpx;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(254, 124, 94, 0.2);
}

.banner-image {
  width: 100%;
  height: 100%;
  border-radius: 24rpx;
  /* 宽度100%，高度自适应裁剪填满 */
  object-fit: cover;
}

/* ========== 公告栏 ========== */
.notice-section {
  padding: 20rpx 24rpx 0;
}

.notice-card {
  display: flex;
  align-items: center;
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 20rpx 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.notice-left {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding-right: 20rpx;
  border-right: 2rpx solid #f0f0f0;
}

.notice-icon {
  font-size: 32rpx;
}

.notice-label {
  font-size: 26rpx;
  font-weight: 600;
  color: #FE7C5E;
  white-space: nowrap;
}

.notice-content {
  flex: 1;
  margin-left: 20rpx;
  height: 48rpx;
  overflow: hidden;
}

.notice-swiper {
  height: 48rpx;
}

.notice-text {
  font-size: 24rpx;
  color: #666666;
  line-height: 48rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ========== 个人中心卡片 ========== */
.user-card-section {
  padding: 20rpx 24rpx 0;
}

.user-card {
  position: relative;
  height: 220rpx;
  border-radius: 30rpx;
  overflow: hidden;
}

.user-card-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-card-mask {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 30rpx;
  background: linear-gradient(135deg, rgba(254, 124, 94, 0.85) 0%, rgba(255, 155, 123, 0.85) 100%);
}

.user-card-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.user-avatar {
  width: 90rpx;
  height: 90rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.5);
}

.user-name-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.user-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #ffffff;
}

.user-tip {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.7);
}

.user-card-right {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.user-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.stat-value {
  font-size: 30rpx;
  font-weight: 700;
  color: #ffffff;
}

.stat-label {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.8);
}

.stat-divider {
  width: 2rpx;
  height: 40rpx;
  background: rgba(255, 255, 255, 0.3);
}

/* ========== 功能导航区 ========== */
.nav-section {
  padding: 20rpx 24rpx 0;
}

.nav-grid {
  display: flex;
  justify-content: space-around;
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  transition: transform 0.2s ease;

  &:active {
    transform: scale(0.92);
  }
}

.nav-icon-wrapper {
  width: 90rpx;
  height: 90rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FFF4EF 0%, #FFE8DE 100%);
  border-radius: 24rpx;
}

.nav-icon {
  font-size: 40rpx;
}

.nav-text {
  font-size: 24rpx;
  color: #333333;
  font-weight: 500;
}

/* ========== 推荐商品 ========== */
.recommend-section {
  padding: 20rpx 24rpx 0;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.section-title-wrapper {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.section-icon {
  font-size: 32rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #333333;
}

.recommend-scroll {
  white-space: nowrap;
}

.recommend-list {
  display: inline-flex;
  gap: 20rpx;
  padding-right: 24rpx;
}

.recommend-item {
  width: 280rpx;
  background-color: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
  display: inline-flex;
  flex-direction: column;
  transition: transform 0.2s ease;

  &:active {
    transform: scale(0.96);
  }
}

.recommend-image {
  width: 280rpx;
  height: 200rpx;
}

.recommend-info {
  padding: 16rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  white-space: normal;
}

.recommend-name {
  font-size: 26rpx;
  font-weight: 600;
  color: #333333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-desc {
  font-size: 22rpx;
  color: #999999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 4rpx;
}

.recommend-price {
  font-size: 30rpx;
  font-weight: 700;
  color: #FE7C5E;
}

.recommend-sales {
  font-size: 20rpx;
  color: #999999;
}

/* ========== 底部留白 ========== */
.bottom-spacer {
  height: 120rpx;
}
</style>
