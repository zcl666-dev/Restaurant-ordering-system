<template>
  <view class="order-page">
    <view class="search-header">
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          placeholder="搜索商品"
          v-model="searchText"
          @confirm="handleSearch"
        />
        <view class="search-line"></view>
      </view>
    </view>

    <view class="loading-container" v-if="isLoading">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <view class="error-container" v-else-if="loadError" @click="fetchProductDisplay">
      <text class="error-icon">😵</text>
      <text class="error-text">{{ loadError }}</text>
      <text class="retry-text">点击重试</text>
    </view>

    <CategoryProduct
      v-else-if="categoryData.length > 0"
      ref="categoryProductRef"
      :categories="categoryData"
      :default-index="defaultIndex"
      @category-change="handleCategoryChange"
      @product-click="handleProductClick"
      @scroll="handleScroll"
      @add-to-cart="handleQuickAdd"
    />

    <view class="empty-container" v-else>
      <text class="empty-icon">🍽</text>
      <text class="empty-text">暂无商品数据</text>
    </view>

    <view class="cart-bar" v-if="cartData.totalQuantity > 0">
      <view class="cart-bar-left" @tap="showCartPopup = true">
        <view class="cart-icon-wrapper">
          <text class="cart-icon">🛒</text>
          <text class="cart-badge">{{ cartData.totalQuantity }}</text>
        </view>
        <text class="cart-total-amount">¥{{ actualPayAmount.toFixed(2) }}</text>
      </view>
      <view class="cart-bar-right">
        <text class="checkout-btn" @tap.stop="handleCheckout">去结算</text>
      </view>
    </view>

    <view class="cart-popup-overlay" :class="{ show: showCartPopup }">
      <view class="cart-popup-mask" @tap="showCartPopup = false"></view>
      <view class="cart-popup">
        <view class="cart-popup-header">
          <text class="cart-popup-title">购物车</text>
          <view class="cart-popup-close" @tap="showCartPopup = false">
            <text class="close-icon">✕</text>
          </view>
        </view>
        <scroll-view class="cart-popup-body" scroll-y>
          <view
            v-for="item in cartData.items"
            :key="item.itemId"
            class="cart-item"
          >
            <image
              class="cart-item-image"
              :src="item.productImage"
              mode="aspectFill"
              @error="onCartItemImageError($event, item)"
            />
            <view class="cart-item-info">
              <text class="cart-item-name">{{ item.productName }}</text>
              <view class="cart-item-specs" v-if="item.options && item.options.length > 0">
                <text class="cart-item-spec" v-for="(opt, idx) in item.options" :key="idx">
                  {{ opt.groupName }}: {{ opt.valueName }}
                </text>
              </view>
              <view class="cart-item-bottom">
                <view class="cart-item-price-row">
                  <text v-if="item.voucherId" class="cart-item-voucher-tag">兑换券</text>
                  <text class="cart-item-price" :class="{ 'price-free': item.voucherId }">
                    {{ item.voucherId ? '¥0.00' : '¥' + item.unitPrice.toFixed(2) }}
                  </text>
                  <text v-if="item.voucherId" class="cart-item-price-original">¥{{ item.unitPrice.toFixed(2) }}</text>
                </view>
                <view class="cart-item-stepper">
                  <text class="stepper-btn minus" @tap="handleDecrement(item)">−</text>
                  <text class="stepper-value">{{ item.quantity }}</text>
                  <text class="stepper-btn plus" @tap="handleIncrement(item)">+</text>
                </view>
              </view>
            </view>
          </view>
          <view class="cart-empty-tip" v-if="cartData.items.length === 0">
            <text class="cart-empty-icon">🛒</text>
            <text>购物车是空的</text>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 自定义 TabBar -->
    <TabBar />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import CategoryProduct from '@/components/CategoryProduct/CategoryProduct.vue'
import TabBar from '@/components/TabBar/TabBar.vue'
import { getProductDisplay } from '@/api/product.js'
import { saveSubscribe } from '@/api/user.js'
import { getCurrentCart, addToCart, updateCartItem } from '@/api/cart.js'
import { createOrder } from '@/api/order.js'
import { getTableNo } from '@/utils/tableStorage.js'

const searchText = ref('')
const defaultIndex = ref(0)
const categoryProductRef = ref(null)
const categoryData = ref([])
const isLoading = ref(true)
const loadError = ref('')

const cartData = ref({
  cartId: null,
  totalQuantity: 0,
  totalAmount: 0,
  voucherDiscount: 0,
  items: []
})

// 实际应付金额 = 总价 - 券抵扣
const actualPayAmount = computed(() => {
  const total = cartData.value.totalAmount || 0
  const discount = cartData.value.voucherDiscount || 0
  return Math.max(0, total - discount)
})

const showCartPopup = ref(false)

// 微信订阅消息模板ID
const SUBSCRIBE_TMPL_IDS = [
  'owRJNezMKIuvTypD-IS_CEcgYbe3rfPz2WdWQyR889c', // 下单成功通知
  'J2kTKOFZHXW8TuRUYR-QfUPotm0uwS6ft0ikephjEjE', // 订单取消通知
  'iSuL7Y8g3WyG-4VM0tbFrEwvqB95LDqp71k4vx1OTvQ'  // 用餐提醒
]

const fetchProductDisplay = async () => {
  isLoading.value = true
  loadError.value = ''

  try {
    const list = await getProductDisplay()
    if (Array.isArray(list)) {
      categoryData.value = list.map((item) => ({
        id: item.id,
        name: item.categoryName || '',
        icon: item.icon || '',
        products: (item.products || []).map((p) => ({
          id: p.id,
          name: p.productName || '',
          desc: p.desc || '',
          price: p.price || 0,
          originalPrice: p.originalPrice || null,
          image: p.productImage || '',
          tags: p.tags || [],
          salesCount: p.salesCount || 0
        }))
      }))
    } else {
      loadError.value = '数据格式异常'
    }
  } catch (err) {
    loadError.value = err.message || '加载失败'
  } finally {
    isLoading.value = false
  }
}

const fetchCart = async () => {
  try {
    const data = await getCurrentCart()
    cartData.value.cartId = data.cartId
    cartData.value.totalQuantity = data.totalQuantity || 0
    cartData.value.totalAmount = data.totalAmount || 0
    cartData.value.voucherDiscount = data.voucherDiscount || 0
    cartData.value.items = data.items || []
  } catch (err) {
    cartData.value.cartId = null
    cartData.value.totalQuantity = 0
    cartData.value.totalAmount = 0
    cartData.value.items = []
  }
}

const buildOptionSnapshot = (options) => {
  if (!options || options.length === 0) return ''
  const list = options.map((opt) => ({
    groupId: opt.groupId,
    optionId: opt.optionId
  }))
  return JSON.stringify(list)
}

const handleIncrement = async (item) => {
  try {
    const optionSnapshot = buildOptionSnapshot(item.options)
    const res = await addToCart({
      productId: item.productId,
      optionSnapshot: optionSnapshot || null
    })
    // 立即更新汇总信息（含券抵扣），再异步拉取完整数据
    if (res) {
      cartData.value.totalQuantity = res.totalQuantity || 0
      cartData.value.totalAmount = res.totalAmount || 0
      cartData.value.voucherDiscount = res.voucherDiscount || 0
    }
    fetchCart()
  } catch (err) {
    console.error('加购失败:', err)
  }
}

const handleDecrement = async (item) => {
  const newQuantity = item.quantity - 1
  try {
    await updateCartItem(item.itemId, { quantity: newQuantity })
    // 重新拉取购物车数据（更新汇总信息和券状态）
    await fetchCart()
  } catch (err) {
    console.error('减购失败:', err)
  }
}

const onCartItemImageError = (event, item) => {
  event.target.src = ''
}

const handleSearch = () => {
  if (searchText.value.trim()) {
    uni.showToast({
      title: `搜索: ${searchText.value}`,
      icon: 'none'
    })
  }
}

const handleCategoryChange = (data) => {
}

const handleProductClick = (product) => {
  uni.navigateTo({
    url: `/pages/product-detail/product-detail?id=${product.id}`
  })
}

const handleQuickAdd = async (product) => {
  try {
    const res = await addToCart({
      productId: product.id,
      optionSnapshot: null
    })
    // 立即更新汇总信息（含券抵扣），再异步拉取完整数据
    if (res) {
      cartData.value.totalQuantity = res.totalQuantity || 0
      cartData.value.totalAmount = res.totalAmount || 0
      cartData.value.voucherDiscount = res.voucherDiscount || 0
    }
    fetchCart()
    uni.showToast({ title: '已加入购物车', icon: 'success' })
  } catch (err) {
    uni.showToast({ title: err.message || '加购失败', icon: 'none' })
  }
}

const handleScroll = (data) => {
}

const handleCheckout = () => {
  console.log('handleCheckout 被点击, tmplIds:', SUBSCRIBE_TMPL_IDS)
  // 必须在用户点击的同步调用栈中直接调用，不能放在 await 之后
  uni.requestSubscribeMessage({
    tmplIds: SUBSCRIBE_TMPL_IDS,
    success: async (res) => {
      console.log('订阅授权成功:', JSON.stringify(res))
      // 将授权结果保存到后端数据库
      for (const tmplId of SUBSCRIBE_TMPL_IDS) {
        const status = res[tmplId] === 'accept' ? 1 : 0
        try {
          await saveSubscribe({ templateId: tmplId, status })
          console.log('保存订阅成功:', tmplId, status)
        } catch (err) {
          console.error('保存订阅失败:', tmplId, err)
        }
      }
    },
    fail: (err) => {
      console.log('订阅授权失败:', JSON.stringify(err))
    },
    complete: async () => {
      console.log('订阅授权complete，开始创建订单')
      try {
        const tableNo = getTableNo()
        const orderData = {}
        if (tableNo) {
          orderData.tableNumber = tableNo
          orderData.diningType = 1
        }
        const res = await createOrder(orderData)
        uni.navigateTo({
          url: `/pages/order-detail/order-detail?id=${res.orderId}`
        })
      } catch (err) {
        uni.showToast({
          title: err.message || '下单失败',
          icon: 'none'
        })
      }
    }
  })
}


const handleImageLoad = () => {
  if (categoryProductRef.value) {
    categoryProductRef.value.refreshPositions()
  }
}

onMounted(() => {
  fetchProductDisplay()
  fetchCart()
})

onShow(() => {
  fetchCart()
})
</script>

<style lang="scss" scoped>
.order-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.search-header {
  padding: 16rpx 24rpx;
  background-color: #ffffff;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 0;
  z-index: 100;
}

.search-box {
  display: flex;
  align-items: center;
  background-color: #f5f5f5;
  border-radius: 40rpx;
  padding: 16rpx 28rpx;
  position: relative;
  transition: background-color 0.3s ease;
}

.search-line {
  position: absolute;
  left: 28rpx;
  top: 50%;
  transform: translateY(-50%);
  width: 4rpx;
  height: 24rpx;
  background: linear-gradient(180deg, #FF6B6B, #FF8E53);
  border-radius: 2rpx;
  margin-right: 12rpx;
}

.search-icon {
  font-size: 28rpx;
  margin-left: 16rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  background-color: transparent;
  padding-left: 16rpx;
}

.loading-container,
.error-container,
.empty-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
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
  color: #999999;
}

.error-icon {
  font-size: 64rpx;
  margin-bottom: 8rpx;
}

.error-text {
  font-size: 28rpx;
  color: #FF6B6B;
}

.retry-text {
  font-size: 26rpx;
  color: #07c160;
  text-decoration: underline;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 16rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999999;
}

:deep(.product-scroll) {
  padding-bottom: 120rpx;
}

.dining-type-selector {
  display: flex;
  background-color: rgba(255, 255, 255, 0.15);
  border-radius: 30rpx;
  padding: 4rpx;
}

.dining-option {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.7);
  padding: 10rpx 24rpx;
  border-radius: 26rpx;
  transition: all 0.2s ease;

  &.active {
    background: linear-gradient(135deg, #FF6B6B, #FF8E53);
    color: #fff;
    font-weight: 600;
  }
}

/* 购物车底部栏 */
.cart-bar {
  position: fixed;
  bottom: 110rpx;
  left: 0;
  right: 0;
  height: 110rpx;
  background-color: rgba(51, 51, 51, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
  z-index: 200;
  border-radius: 24rpx 24rpx 0 0;
}

.cart-bar-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.cart-icon-wrapper {
  position: relative;
}

.cart-icon {
  font-size: 52rpx;
}

.cart-badge {
  position: absolute;
  top: -10rpx;
  right: -14rpx;
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  color: #fff;
  font-size: 20rpx;
  min-width: 36rpx;
  height: 36rpx;
  line-height: 36rpx;
  text-align: center;
  border-radius: 18rpx;
  padding: 0 8rpx;
  font-weight: 600;
}

.cart-total-amount {
  font-size: 38rpx;
  font-weight: bold;
  color: #ffffff;
}

.cart-bar-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.checkout-btn {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  color: #fff;
  font-size: 30rpx;
  font-weight: bold;
  padding: 18rpx 48rpx;
  border-radius: 40rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 107, 107, 0.4);
}

/* 购物车弹出层 */
.cart-popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  visibility: hidden;
  opacity: 0;
  transition: all 0.3s ease;

  &.show {
    visibility: visible;
    opacity: 1;

    .cart-popup {
      transform: translateY(0);
    }
  }
}

.cart-popup-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
}

.cart-popup {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ffffff;
  border-radius: 28rpx 28rpx 0 0;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
  padding-bottom: calc(100rpx + env(safe-area-inset-bottom));
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.cart-popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx 32rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.cart-popup-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.cart-popup-close {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: #f5f5f5;
}

.close-icon {
  font-size: 28rpx;
  color: #666;
}

.cart-popup-body {
  flex: 1;
  max-height: calc(70vh - 100rpx);
  padding: 0 32rpx;
}

.cart-item {
  display: flex;
  padding: 24rpx 0;
  border-bottom: 2rpx solid #f8f8f8;
}

.cart-item:last-child {
  border-bottom: none;
}

.cart-item-image {
  width: 140rpx;
  height: 140rpx;
  border-radius: 16rpx;
  background-color: #f5f5f5;
  flex-shrink: 0;
}

.cart-item-info {
  flex: 1;
  margin-left: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
  padding-right: 20rpx;
}

.cart-item-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cart-item-specs {
  display: flex;
  gap: 8rpx;
  margin-top: 8rpx;
  flex-wrap: wrap;
}

.cart-item-spec {
  font-size: 22rpx;
  color: #999;
  background-color: #f5f5f5;
  padding: 4rpx 14rpx;
  border-radius: 6rpx;
}

.cart-item-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8rpx;
}

.cart-item-price {
  font-size: 32rpx;
  color: #FF6B6B;
  font-weight: bold;
  flex-shrink: 0;
}

.cart-item-price-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.cart-item-voucher-tag {
  font-size: 20rpx;
  color: #FF6B6B;
  background: #fff5f5;
  border: 1rpx solid #FFDADA;
  border-radius: 6rpx;
  padding: 2rpx 10rpx;
  flex-shrink: 0;
}

.price-free {
  color: #67c23a;
}

.cart-item-price-original {
  font-size: 24rpx;
  color: #ccc;
  text-decoration: line-through;
}

.cart-item-stepper {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-shrink: 0;
}

.stepper-btn {
  width: 56rpx;
  height: 56rpx;
  line-height: 56rpx;
  text-align: center;
  font-size: 36rpx;
  border-radius: 50%;
  flex-shrink: 0;
  transition: all 0.2s ease;

  &.minus {
    color: #666;
    background-color: #f5f5f5;
  }

  &.plus {
    color: #fff;
    background: linear-gradient(135deg, #FF6B6B, #FF8E53);
    box-shadow: 0 2rpx 8rpx rgba(255, 107, 107, 0.3);
  }

  &:active {
    transform: scale(0.9);
  }
}

.stepper-value {
  min-width: 60rpx;
  text-align: center;
  font-size: 28rpx;
  color: #333;
  font-weight: 600;
  flex-shrink: 0;
}

.cart-empty-tip {
  padding: 60rpx 0;
  text-align: center;
  font-size: 28rpx;
  color: #999;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.cart-empty-icon {
  font-size: 64rpx;
}
</style>
