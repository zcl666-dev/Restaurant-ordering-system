<template>
  <view class="tabbar">
    <view
      v-for="tab in tabs"
      :key="tab.pagePath"
      class="tabbar-item"
      @tap="switchTab(tab.pagePath)"
    >
      <image
        class="tabbar-icon"
        :src="currentPath === tab.pagePath ? tab.selectedIconPath : tab.iconPath"
        mode="aspectFit"
      />
      <text
        class="tabbar-text"
        :style="{ color: currentPath === tab.pagePath ? selectedColor : color }"
      >{{ tab.text }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'

const props = defineProps({
  color: { type: String, default: '#999999' },
  selectedColor: { type: String, default: '#FF6B6B' }
})

const tabs = [
  {
    pagePath: '/pages/index/index',
    text: '首页',
    iconPath: 'https://zcl-library-management-system.oss-cn-beijing.aliyuncs.com/tabbar/index.png',
    selectedIconPath: 'https://zcl-library-management-system.oss-cn-beijing.aliyuncs.com/tabbar/index-s.png'
  },
  {
    pagePath: '/pages/order/order',
    text: '点餐',
    iconPath: 'https://zcl-library-management-system.oss-cn-beijing.aliyuncs.com/tabbar/order.png',
    selectedIconPath: 'https://zcl-library-management-system.oss-cn-beijing.aliyuncs.com/tabbar/order-s.png'
  },
  {
    pagePath: '/pages/bill/bill',
    text: '订单',
    iconPath: 'https://zcl-library-management-system.oss-cn-beijing.aliyuncs.com/tabbar/bill.png',
    selectedIconPath: 'https://zcl-library-management-system.oss-cn-beijing.aliyuncs.com/tabbar/bill-s.png'
  },
  {
    pagePath: '/pages/user/user',
    text: '个人',
    iconPath: 'https://zcl-library-management-system.oss-cn-beijing.aliyuncs.com/tabbar/user.png',
    selectedIconPath: 'https://zcl-library-management-system.oss-cn-beijing.aliyuncs.com/tabbar/user-s.png'
  }
]

const currentPath = ref('')

// 每次页面显示时获取当前页面路径
onShow(() => {
  const pages = getCurrentPages()
  if (pages.length > 0) {
    currentPath.value = '/' + pages[pages.length - 1].route
  }
})

function switchTab(pagePath) {
  if (currentPath.value === pagePath) return
  uni.switchTab({ url: pagePath })
}
</script>

<style scoped>
.tabbar {
  display: flex;
  align-items: center;
  justify-content: space-around;
  height: 110rpx;
  box-sizing: border-box;
  background-color: #ffffff;
  border-top: 1rpx solid #f0f0f0;
  padding-bottom: env(safe-area-inset-bottom);
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
}

.tabbar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  gap: 6rpx;
}

.tabbar-icon {
  width: 48rpx;
  height: 48rpx;
}

.tabbar-text {
  font-size: 22rpx;
  line-height: 1;
}
</style>
