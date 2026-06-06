<template>
  <view class="category-product-container">
    <!-- 左侧分类栏 -->
    <scroll-view class="category-scroll" scroll-y :scroll-into-view="scrollIntoId" scroll-with-animation>
      <view
        v-for="(category, index) in categories"
        :key="category.id"
        :id="'cat-' + category.id"
        class="category-item"
        :class="{ active: currentCategory === index }"
        @click="handleCategoryClick(index)"
      >
        <view v-if="currentCategory === index" class="active-bar"></view>
        <text class="category-icon" v-if="category.icon">{{ category.icon }}</text>
        <text class="category-name">{{ category.name }}</text>
      </view>
    </scroll-view>

    <!-- 右侧商品列表 -->
    <scroll-view
      class="product-scroll"
      scroll-y
      :scroll-into-view="scrollToId"
      scroll-with-animation
      @scroll="handleScroll"
      @scrolltolower="handleScrollToLower"
    >
      <view class="product-content">
        <view
          v-for="category in categories"
          :key="category.id"
          :id="'prod-' + category.id"
          class="category-section"
        >
          <!-- 分类标题 -->
          <view class="section-header">
            <text class="section-title">{{ category.name }}</text>
          </view>

          <!-- 商品卡片列表 -->
          <view
            v-for="product in category.products"
            :key="product.id"
            class="product-card"
            @click="handleProductClick(product)"
          >
            <image class="product-image" :src="product.image" mode="aspectFill" @load="onProductImageLoad" />
            <view class="product-info">
              <text class="product-name">{{ product.name }}</text>
              <text class="product-desc" v-if="product.desc">{{ product.desc }}</text>
              <text class="product-sales" v-if="product.salesCount > 0">月售{{ product.salesCount }}</text>
              <view class="product-tags" v-if="product.tags && product.tags.length > 0">
                <text v-for="tag in product.tags" :key="tag" class="product-tag">{{ tag }}</text>
              </view>
              <view class="product-bottom">
                <view class="price-row">
                  <text class="price-symbol">¥</text>
                  <text class="product-price">{{ product.price.toFixed(0) }}</text>
                  <text class="price-unit">/份</text>
                  <text v-if="product.originalPrice" class="product-original-price">¥{{ product.originalPrice.toFixed(0) }}</text>
                </view>
                <view class="add-btn" @click.stop="handleAddToCart(product)">
                  <text class="add-icon">+</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick, getCurrentInstance } from 'vue'

const props = defineProps({
  categories: {
    type: Array,
    required: true,
    validator: (value) => {
      return value.length > 0 && value.every(item => item.id && item.name && item.products)
    }
  },
  defaultIndex: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['category-change', 'product-click', 'scroll', 'add-to-cart'])

const currentCategory = ref(props.defaultIndex)
const scrollIntoId = ref('')
const scrollToId = ref('')

let scrollTimer = null
let isClickScrolling = false
const categoryOffsetList = ref([])

const instance = getCurrentInstance()

const handleCategoryClick = (index) => {
  if (currentCategory.value === index) return

  currentCategory.value = index
  const category = props.categories[index]

  isClickScrolling = true
  scrollToId.value = ''

  nextTick(() => {
    scrollToId.value = 'prod-' + category.id
    setTimeout(() => {
      isClickScrolling = false
    }, 600)
  })

  emit('category-change', { index, category: props.categories[index] })
}

const handleScroll = (e) => {
  if (isClickScrolling) return

  if (scrollTimer) clearTimeout(scrollTimer)

  scrollTimer = setTimeout(() => {
    const scrollTop = e.detail.scrollTop || 0
    syncCategoryByScrollTop(scrollTop)
    emit('scroll', { scrollTop })
  }, 30)
}

const syncCategoryByScrollTop = (scrollTop) => {
  const positions = categoryOffsetList.value
  if (positions.length === 0) return

  let targetIndex = positions[0].index
  for (let i = positions.length - 1; i >= 0; i--) {
    if (scrollTop >= positions[i].offset - 50) {
      targetIndex = positions[i].index
      break
    }
  }

  if (targetIndex !== currentCategory.value) {
    currentCategory.value = targetIndex
    scrollIntoId.value = ''
    nextTick(() => {
      scrollIntoId.value = 'cat-' + props.categories[targetIndex].id
    })
  }
}

const calcCategoryPositions = () => {
  const query = uni.createSelectorQuery().in(instance.proxy)

  props.categories.forEach((category) => {
    query.select('#prod-' + category.id).boundingClientRect()
  })

  query.exec((res) => {
    if (!res || res.length === 0) return

    const positions = []
    let accumulatedOffset = 0

    props.categories.forEach((category, i) => {
      const rect = res[i]
      if (rect) {
        positions.push({
          id: category.id,
          index: i,
          offset: accumulatedOffset
        })
        accumulatedOffset += rect.height
      }
    })

    categoryOffsetList.value = positions
  })
}

const handleScrollToLower = () => {
  emit('scroll', { scrollToLower: true })
}

const handleProductClick = (product) => {
  emit('product-click', product)
}

const handleAddToCart = (product) => {
  emit('add-to-cart', product)
}

let imageLoadTimer = null
const onProductImageLoad = () => {
  if (imageLoadTimer) clearTimeout(imageLoadTimer)
  imageLoadTimer = setTimeout(() => {
    calcCategoryPositions()
  }, 300)
}

const refreshPositions = () => {
  setTimeout(() => {
    calcCategoryPositions()
  }, 200)
}

watch(() => props.defaultIndex, (newVal) => {
  if (newVal >= 0 && newVal < props.categories.length) {
    currentCategory.value = newVal
    handleCategoryClick(newVal)
  }
})

onMounted(() => {
  currentCategory.value = props.defaultIndex
  setTimeout(() => { calcCategoryPositions() }, 300)
  setTimeout(() => { calcCategoryPositions() }, 800)
})

onUnmounted(() => {
  if (scrollTimer) {
    clearTimeout(scrollTimer)
    scrollTimer = null
  }
})

defineExpose({
  scrollToCategory(index) {
    if (index >= 0 && index < props.categories.length) {
      handleCategoryClick(index)
    }
  },
  getCurrentCategory() {
    return {
      index: currentCategory.value,
      category: props.categories[currentCategory.value]
    }
  },
  refreshPositions
})
</script>

<style lang="scss" scoped>
.category-product-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background-color: #f5f5f5;
}

/* ========== 左侧分类栏 ========== */
.category-scroll {
  width: 180rpx;
  height: 100%;
  background-color: #f5f5f5;
}

.category-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 28rpx 12rpx;
  font-size: 24rpx;
  color: #666;
  transition: all 0.3s ease;

  &.active {
    background-color: #fff;
    color: #333;
    font-weight: 600;
  }
}

.active-bar {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 6rpx;
  height: 40rpx;
  background: linear-gradient(180deg, #FF6B6B, #FF8E53);
  border-radius: 0 3rpx 3rpx 0;
}

.category-icon {
  font-size: 40rpx;
  margin-bottom: 8rpx;
}

.category-name {
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

/* ========== 右侧商品列表 ========== */
.product-scroll {
  flex: 1;
  height: 100%;
  background-color: #fff;
}

.product-content {
  padding: 0 20rpx 120rpx;
}

.category-section {
  margin-bottom: 16rpx;
}

.section-header {
  padding: 24rpx 8rpx 16rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

/* ========== 商品卡片 ========== */
.product-card {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid #f5f5f5;

  &:active {
    background-color: #fafafa;
  }
}

.product-image {
  width: 180rpx;
  height: 180rpx;
  border-radius: 14rpx;
  background-color: #f5f5f5;
  flex-shrink: 0;
}

.product-info {
  flex: 1;
  margin-left: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.product-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  font-size: 22rpx;
  color: #999;
  margin-top: 6rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}

.product-sales {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
}

.product-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 8rpx;
}

.product-tag {
  font-size: 20rpx;
  color: #FF6B6B;
  background-color: #fff5f5;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  border: 1rpx solid rgba(255, 107, 107, 0.15);
}

.product-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10rpx;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 2rpx;
}

.price-symbol {
  font-size: 22rpx;
  font-weight: 600;
  color: #FF6B6B;
}

.product-price {
  font-size: 36rpx;
  font-weight: 700;
  color: #FF6B6B;
  line-height: 1;
}

.price-unit {
  font-size: 22rpx;
  color: #999;
  margin-left: 4rpx;
}

.product-original-price {
  font-size: 22rpx;
  color: #ccc;
  text-decoration: line-through;
  margin-left: 10rpx;
}

.add-btn {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 107, 0.3);
  transition: transform 0.2s ease;

  &:active {
    transform: scale(0.9);
  }
}

.add-icon {
  font-size: 36rpx;
  color: #fff;
  font-weight: 300;
  line-height: 1;
}
</style>
