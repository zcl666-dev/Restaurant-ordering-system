<template>
  <view class="category-product-container">
    <scroll-view
      class="category-scroll"
      scroll-y
      :scroll-into-view="scrollIntoId"
      scroll-with-animation
    >
      <view
        v-for="(category, index) in categories"
        :key="category.id"
        :id="'category-' + category.id"
        class="category-item"
        :class="{ active: currentCategory === index }"
        @click="handleCategoryClick(index)"
      >
        <text class="category-name">{{ category.name }}</text>
        <view v-if="currentCategory === index" class="active-indicator"></view>
      </view>
    </scroll-view>

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
          :id="'product-' + category.id"
          class="category-section"
        >
          <view class="section-header">
            <text class="section-title">{{ category.name }}</text>
          </view>
          <view class="product-list">
            <view
              v-for="product in category.products"
              :key="product.id"
              class="product-item"
              @click="handleProductClick(product)"
            >
              <image class="product-image" :src="product.image" mode="aspectFill" @load="onProductImageLoad" />
              <view class="product-info">
                <text class="product-name">{{ product.name }}</text>
                <text class="product-desc">{{ product.desc }}</text>
                <view class="product-price-row">
                  <text class="product-price">¥{{ product.price.toFixed(2) }}</text>
                  <text v-if="product.originalPrice" class="product-original-price">¥{{ product.originalPrice.toFixed(2) }}</text>
                </view>
                <view class="product-tags">
                  <text v-for="tag in product.tags" :key="tag" class="product-tag">{{ tag }}</text>
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

const emit = defineEmits(['category-change', 'product-click', 'scroll'])

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
    scrollToId.value = 'product-' + category.id
    setTimeout(() => {
      isClickScrolling = false
    }, 600)
  })

  emit('category-change', {
    index,
    category: props.categories[index]
  })
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
      scrollIntoId.value = 'category-' + props.categories[targetIndex].id
    })
  }
}

const calcCategoryPositions = () => {
  const query = uni.createSelectorQuery().in(instance.proxy)

  props.categories.forEach((category) => {
    query.select('#product-' + category.id).boundingClientRect()
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
  setTimeout(() => {
    calcCategoryPositions()
  }, 300)
  setTimeout(() => {
    calcCategoryPositions()
  }, 800)
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

.category-scroll {
  width: 200rpx;
  height: 100%;
  background-color: #f8f8f8;
  border-right: 1rpx solid #e0e0e0;
}

.category-item {
  position: relative;
  display: flex;
  align-items: center;
  padding: 30rpx 20rpx;
  font-size: 28rpx;
  color: #666666;
  background-color: #f8f8f8;
  transition: all 0.3s ease;

  &.active {
    background-color: #ffffff;
    color: #FF6B6B;
    font-weight: 600;
  }

  &:active {
    background-color: #f0f0f0;
  }
}

.category-name {
  flex: 1;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.active-indicator {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 6rpx;
  height: 40rpx;
  background-color: #FF6B6B;
  border-radius: 0 3rpx 3rpx 0;
}

.product-scroll {
  flex: 1;
  height: 100%;
  background-color: #ffffff;
}

.product-content {
  padding: 20rpx;
}

.category-section {
  margin-bottom: 30rpx;
}

.section-header {
  padding: 15rpx 0;
  margin-bottom: 15rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333333;
}

.product-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
}

.product-item {
  width: calc(50% - 10rpx);
  margin-bottom: 20rpx;
  background-color: #ffffff;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
  }
}

.product-image {
  width: 100%;
  height: 200rpx;
  background-color: #f5f5f5;
}

.product-info {
  padding: 15rpx;
}

.product-name {
  display: block;
  font-size: 26rpx;
  font-weight: 500;
  color: #333333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 8rpx;
}

.product-desc {
  display: block;
  font-size: 22rpx;
  color: #999999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 10rpx;
}

.product-price-row {
  display: flex;
  align-items: baseline;
  gap: 10rpx;
  margin-bottom: 10rpx;
}

.product-price {
  font-size: 32rpx;
  font-weight: 600;
  color: #FF6B6B;
}

.product-original-price {
  font-size: 22rpx;
  color: #cccccc;
  text-decoration: line-through;
}

.product-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.product-tag {
  font-size: 20rpx;
  color: #FF6B6B;
  background-color: #fff5f5;
  padding: 4rpx 10rpx;
  border-radius: 4rpx;
}

@media screen and (min-width: 768px) {
  .category-scroll {
    width: 180rpx;
  }

  .product-item {
    width: calc(33.33% - 15rpx);
  }

  .product-image {
    height: 240rpx;
  }
}

@media screen and (max-width: 320px) {
  .category-scroll {
    width: 160rpx;
  }

  .category-item {
    padding: 25rpx 15rpx;
    font-size: 24rpx;
  }

  .product-image {
    height: 160rpx;
  }
}
</style>
