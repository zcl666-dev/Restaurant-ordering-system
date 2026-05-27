<template>
  <view class="product-detail-page">
    <scroll-view class="detail-scroll" scroll-y v-if="!isLoading && !loadError && product">
      <image
        class="product-image"
        :src="product.productImage"
        mode="aspectFill"
        @error="onImageError"
      />

      <view class="product-base-info">
        <text class="product-name">{{ product.productName }}</text>
        <view class="price-sales-row">
          <text class="product-price">¥{{ product.price.toFixed(2) }}</text>
          <text class="product-sales">已售 {{ product.salesCount }}</text>
        </view>
      </view>

      <view class="section-divider"></view>

      <view class="product-desc-section">
        <text class="section-label">商品描述</text>
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
      <text class="loading-text">加载中...</text>
    </view>

    <view class="error-container" v-else-if="loadError" @click="fetchProductDetail">
      <text class="error-text">{{ loadError }}</text>
      <text class="retry-text">点击重试</text>
    </view>

    <view class="bottom-bar">
      <button class="add-cart-btn" @click="addToCartHandler">加入购物车</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
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

.product-image {
  width: 100%;
  height: 500rpx;
  background-color: #eee;
}

.product-base-info {
  padding: 30rpx;
  background-color: #fff;
}

.product-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  line-height: 1.4;
}

.price-sales-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 20rpx;
}

.product-price {
  font-size: 40rpx;
  color: #FF6B6B;
  font-weight: bold;
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
  padding: 30rpx;
  background-color: #fff;
}

.section-label {
  font-size: 28rpx;
  color: #999;
  display: block;
}

.product-desc {
  font-size: 30rpx;
  color: #333;
  margin-top: 16rpx;
  line-height: 1.6;
  display: block;
}

.product-specs-section {
  padding: 30rpx;
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
  font-weight: bold;
  display: block;
  margin-bottom: 16rpx;
}

.spec-options {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.spec-option {
  padding: 14rpx 32rpx;
  border-radius: 8rpx;
  border: 2rpx solid #ddd;
  background-color: #fff;
  transition: all 0.2s;
}

.spec-option.active {
  border-color: #FF6B6B;
  background-color: #FFF0F0;
}

.spec-option-text {
  font-size: 26rpx;
  color: #333;
}

.spec-option.active .spec-option-text {
  color: #FF6B6B;
  font-weight: bold;
}

.bottom-placeholder {
  height: 120rpx;
}

.loading-container,
.error-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.loading-text {
  font-size: 28rpx;
  color: #999;
}

.error-text {
  font-size: 28rpx;
  color: #FF6B6B;
  margin-bottom: 20rpx;
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
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background-color: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.add-cart-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background-color: #FF6B6B;
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 44rpx;
  border: none;
  text-align: center;
}

.add-cart-btn::after {
  border: none;
}
</style>