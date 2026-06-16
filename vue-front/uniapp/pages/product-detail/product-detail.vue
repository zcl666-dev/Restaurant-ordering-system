<template>
  <view class="product-detail-page">
    <scroll-view class="detail-scroll" scroll-y v-if="!isLoading && !loadError && product">
      <view class="image-wrapper">
        <image
          class="product-image"
          :src="product.productImage"
          mode="aspectFill"
          @error="onImageError"
        />
        <view class="image-gradient"></view>
      </view>

      <view class="product-base-info">
        <text class="product-name">{{ product.productName }}</text>
        <view class="price-sales-row">
          <view class="price-wrapper">
            <text class="price-symbol">¥</text>
            <text class="product-price">{{ product.price.toFixed(2) }}</text>
          </view>
          <text class="product-sales">已售 {{ product.salesCount }}</text>
        </view>
      </view>

      <view class="section-divider"></view>

      <view class="product-desc-section">
        <view class="section-label-row">
          <view class="section-label-bar"></view>
          <text class="section-label">商品描述</text>
        </view>
        <text class="product-desc">{{ product.description }}</text>
      </view>

      <view class="section-divider"></view>

      <view class="product-specs-section" v-if="product.optionGroups && product.optionGroups.length > 0">
        <view
          v-for="group in product.optionGroups"
          :key="group.groupId"
          class="spec-group"
        >
          <text class="spec-group-name">{{ group.groupName }}</text>
          <view class="spec-options">
            <view
              v-for="option in group.options"
              :key="option.id"
              class="spec-option"
              :class="{ active: selectedOptions[group.groupId] === option.id }"
              @click="selectOption(group.groupId, option.id)"
            >
              <text class="spec-option-text">{{ option.valueName }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="bottom-placeholder"></view>
    </scroll-view>

    <view class="loading-container" v-if="isLoading">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <view class="error-container" v-else-if="loadError" @click="fetchProductDetail">
      <text class="error-icon">😵</text>
      <text class="error-text">{{ loadError }}</text>
      <text class="retry-text">点击重试</text>
    </view>

    <view class="bottom-bar">
      <button class="add-cart-btn" @click="addToCartHandler">
        <text class="btn-icon">🛒</text>
        <text class="btn-text">加入购物车</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { getProductDetail } from '@/api/product.js'
import { addToCart } from '@/api/cart.js'

const productId = ref(null)
const product = ref(null)
const isLoading = ref(true)
const loadError = ref('')
const selectedOptions = reactive({})
const imageError = ref(false)

const fetchProductDetail = async () => {
  if (!productId.value) {
    loadError.value = '商品ID无效'
    isLoading.value = false
    return
  }

  isLoading.value = true
  loadError.value = ''

  try {
    const data = await getProductDetail(productId.value)
    product.value = data

    const defaultSelections = {}
    if (data.optionGroups && data.optionGroups.length > 0) {
      data.optionGroups.forEach((group) => {
        const defaultOption = group.options.find((opt) => opt.isDefault)
        if (defaultOption) {
          defaultSelections[group.groupId] = defaultOption.id
        } else if (group.options.length > 0) {
          defaultSelections[group.groupId] = group.options[0].id
        }
      })
    }
    Object.assign(selectedOptions, defaultSelections)
  } catch (err) {
    loadError.value = err.message || '加载失败'
  } finally {
    isLoading.value = false
  }
}

const selectOption = (groupId, optionId) => {
  selectedOptions[groupId] = optionId
}

const addToCartHandler = async () => {
  if (!product.value) return

  const optionSnapshot = buildOptionSnapshot()

  try {
    await addToCart({
      productId: product.value.id,
      optionSnapshot: optionSnapshot || null
    })

    uni.showToast({
      title: '已加入购物车',
      icon: 'success'
    })

    setTimeout(() => {
      uni.navigateBack()
    }, 500)
  } catch (err) {
    console.error('加购失败:', err)
  }
}

const buildOptionSnapshot = () => {
  if (!product.value.optionGroups || product.value.optionGroups.length === 0) return ''
  const list = product.value.optionGroups
    .filter((group) => selectedOptions[group.groupId] != null)
    .map((group) => ({
      groupId: group.groupId,
      optionId: selectedOptions[group.groupId]
    }))
  if (list.length === 0) return ''
  return JSON.stringify(list)
}

const onImageError = () => {
  imageError.value = true
}

onLoad((options) => {
  if (options.id) {
    productId.value = options.id
  }
  fetchProductDetail()
})

onPullDownRefresh(async () => {
  await fetchProductDetail()
  uni.stopPullDownRefresh()
})
</script>

<style lang="scss" scoped>
.product-detail-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.detail-scroll {
  flex: 1;
  overflow: hidden;
}

.image-wrapper {
  position: relative;
  width: 100%;
  height: 520rpx;
}

.product-image {
  width: 100%;
  height: 100%;
  background-color: #f0f0f0;
}

.image-gradient {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 80rpx;
  background: linear-gradient(180deg, transparent, #ffffff);
}

.product-base-info {
  padding: 24rpx 30rpx 30rpx;
  background-color: #fff;
}

.product-name {
  font-size: 38rpx;
  font-weight: 700;
  color: #333;
  line-height: 1.4;
  display: block;
}

.price-sales-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 20rpx;
}

.price-wrapper {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}

.price-symbol {
  font-size: 28rpx;
  color: #FF6B6B;
  font-weight: 600;
}

.product-price {
  font-size: 44rpx;
  color: #FF6B6B;
  font-weight: 700;
}

.product-sales {
  font-size: 24rpx;
  color: #999;
}

.section-divider {
  height: 16rpx;
  background-color: #f5f5f5;
}

.product-desc-section {
  padding: 28rpx 30rpx;
  background-color: #fff;
}

.section-label-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.section-label-bar {
  width: 6rpx;
  height: 28rpx;
  background: linear-gradient(180deg, #FF6B6B, #FF8E53);
  border-radius: 3rpx;
}

.section-label {
  font-size: 28rpx;
  color: #333;
  font-weight: 600;
}

.product-desc {
  font-size: 28rpx;
  color: #666;
  line-height: 1.8;
  display: block;
}

.product-specs-section {
  padding: 28rpx 30rpx;
  background-color: #fff;
}

.spec-group {
  margin-bottom: 30rpx;
}

.spec-group:last-child {
  margin-bottom: 0;
}

.spec-group-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 600;
  display: block;
  margin-bottom: 16rpx;
}

.spec-options {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.spec-option {
  padding: 14rpx 36rpx;
  border-radius: 12rpx;
  border: 2rpx solid #e8e8e8;
  background-color: #fff;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.95);
  }
}

.spec-option.active {
  border-color: #FF6B6B;
  background-color: #FFF0F0;
  box-shadow: 0 2rpx 12rpx rgba(255, 107, 107, 0.15);
}

.spec-option-text {
  font-size: 26rpx;
  color: #666;
}

.spec-option.active .spec-option-text {
  color: #FF6B6B;
  font-weight: 600;
}

.bottom-placeholder {
  height: 140rpx;
}

.loading-container,
.error-container {
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
  color: #999;
}

.error-icon {
  font-size: 64rpx;
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

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16rpx 30rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  background-color: #fff;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.06);
  z-index: 100;
}

.add-cart-btn {
  width: 100%;
  height: 92rpx;
  line-height: 92rpx;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 46rpx;
  border: none;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 107, 0.3);
  transition: all 0.2s ease;
}

.add-cart-btn::after {
  border: none;
}

.add-cart-btn:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 16rpx rgba(255, 107, 107, 0.2);
}

.btn-icon {
  font-size: 36rpx;
}

.btn-text {
  font-size: 32rpx;
}
</style>
