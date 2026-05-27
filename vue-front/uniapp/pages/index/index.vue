<template>
  <view class="swiper-container">
    <swiper
      class="swiper-wrapper"
      :indicator-dots="true"
      :indicator-color="indicatorColor"
      :indicator-active-color="indicatorActiveColor"
      :autoplay="true"
      :interval="interval"
      :duration="duration"
      :circular="true"
      :previous-margin="previousMargin"
      :next-margin="nextMargin"
      :display-multiple-items="displayMultipleItems"
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
          :mode="imageMode"
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

    <view class="custom-indicator" v-if="showCustomIndicator">
      <view
        v-for="(image, index) in images"
        :key="index"
        class="indicator-dot"
        :class="{ active: currentIndex === index }"
        @click="goToSlide(index)"
      ></view>
    </view>

    <view class="swiper-info" v-if="showInfo">
      <text class="current-text">{{ currentIndex + 1 }} / {{ images.length }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'

const images = reactive([
  { src: '/static/pic1.jpg', loaded: false, error: false },
  { src: '/static/pic2.png', loaded: false, error: false },
  { src: '/static/pic3.jpg', loaded: false, error: false }
])

const currentIndex = ref(0)
const interval = ref(3000)
const duration = ref(500)
const previousMargin = ref('0px')
const nextMargin = ref('0px')
const displayMultipleItems = ref(1)
const imageMode = ref('scaleToFill')
const showCustomIndicator = ref(false)
const showInfo = ref(true)
const indicatorColor = ref('rgba(255, 255, 255, 0.5)')
const indicatorActiveColor = ref('#ffffff')

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

const goToSlide = (index) => {
  currentIndex.value = index
}
</script>

<style scoped>
.swiper-container {
  width: 100%;
  max-width: 750rpx;
  margin: 0 auto;
  position: relative;
  overflow: hidden;
  background-color: #f5f5f5;
}

.swiper-wrapper {
  width: 100%;
  height: 400rpx;
}

@media screen and (min-width: 768px) {
  .swiper-wrapper {
    height: 500rpx;
  }
}

@media screen and (max-width: 320px) {
  .swiper-wrapper {
    height: 300rpx;
  }
}

.swiper-item {
  display: flex;
  align-items: center;
  justify-content: center;
}

.swiper-image {
  width: 100%;
  height: 100%;
  opacity: 0;
  transition: opacity 0.3s ease-in-out;
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

.custom-indicator {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20rpx;
}

.indicator-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.5);
  margin: 0 8rpx;
  transition: all 0.3s ease;
}

.indicator-dot.active {
  background-color: #ffffff;
  transform: scale(1.2);
  box-shadow: 0 0 10rpx rgba(255, 255, 255, 0.8);
}

.swiper-info {
  position: absolute;
  bottom: 20rpx;
  right: 20rpx;
  background-color: rgba(0, 0, 0, 0.6);
  padding: 10rpx 20rpx;
  border-radius: 20rpx;
}

.current-text {
  color: #ffffff;
  font-size: 24rpx;
}
</style>
