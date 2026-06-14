<template>
  <transition name="slide-up">
    <div v-if="visible" class="order-notification" @click="goToOrders">
      <div class="notification-body">
        <div class="notification-icon">
          <el-icon :size="20"><Bell /></el-icon>
          <span v-if="count > 0" class="badge">{{ count > 99 ? '99+' : count }}</span>
        </div>
        <div class="notification-text">
          <span class="notification-title">未处理订单</span>
          <span class="notification-count">当前有 <strong>{{ count }}</strong> 笔待处理</span>
        </div>
        <div class="notification-arrow">
          <el-icon :size="16"><ArrowRight /></el-icon>
        </div>
      </div>
      <el-icon class="close-btn" :size="14" @click.stop="dismiss">
        <Close />
      </el-icon>
    </div>
  </transition>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrderList } from '../api/order'

const router = useRouter()
const count = ref(0)
const visible = ref(false)
const dismissedCount = ref(0)
const pollingTimer = ref(null)

async function fetchCount() {
  try {
    const res = await getOrderList({ page: 0, size: 1, status: 1 })
    const newCount = res.totalElements || 0
    count.value = newCount

    if (newCount > 0 && newCount > dismissedCount.value) {
      visible.value = true
    } else if (newCount === 0) {
      visible.value = false
      dismissedCount.value = 0
    }
  } catch (e) {
    // 静默处理
  }
}

function dismiss() {
  visible.value = false
  dismissedCount.value = count.value
}

function goToOrders() {
  router.push('/orders')
}

onMounted(() => {
  fetchCount()
  pollingTimer.value = setInterval(fetchCount, 15000)
})

onUnmounted(() => {
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
  }
})
</script>

<style scoped>
.order-notification {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 2000;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border-radius: 12px;
  padding: 14px 18px;
  cursor: pointer;
  box-shadow: 0 6px 24px rgba(255, 107, 107, 0.4);
  transition: all 0.3s ease;
  min-width: 220px;
}

.order-notification:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 32px rgba(255, 107, 107, 0.5);
}

.notification-body {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notification-icon {
  position: relative;
  color: #fff;
  flex-shrink: 0;
}

.badge {
  position: absolute;
  top: -8px;
  right: -10px;
  background: #fff;
  color: #FF6B6B;
  font-size: 11px;
  font-weight: 700;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  line-height: 1;
}

.notification-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.notification-title {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 500;
}

.notification-count {
  font-size: 14px;
  color: #fff;
  font-weight: 600;
}

.notification-count strong {
  font-size: 18px;
}

.notification-arrow {
  color: rgba(255, 255, 255, 0.7);
  flex-shrink: 0;
}

.close-btn {
  position: absolute;
  top: 6px;
  right: 6px;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  padding: 2px;
  border-radius: 50%;
  transition: all 0.2s;
}

.close-btn:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
}

/* 动画 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
