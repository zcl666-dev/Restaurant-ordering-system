<template>
  <el-container class="admin-layout">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <span class="logo-icon">🍽</span>
        <span v-if="!isCollapse" class="logo-text">餐厅管理</span>
      </div>
      <el-menu :default-active="activeMenu" router :collapse="isCollapse" background-color="transparent"
        text-color="rgba(255,255,255,0.65)" active-text-color="#ffffff" class="sidebar-menu">
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>数据概览</template>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/categories">
          <el-icon><Menu /></el-icon>
          <template #title>分类管理</template>
        </el-menu-item>
        <el-menu-item index="/products">
          <el-icon><Goods /></el-icon>
          <template #title>商品管理</template>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><List /></el-icon>
          <template #title>订单管理</template>
        </el-menu-item>
        <el-menu-item index="/staff-order">
          <el-icon><Service /></el-icon>
          <template #title>代客点餐</template>
        </el-menu-item>
        <el-menu-item index="/stats">
          <el-icon><TrendCharts /></el-icon>
          <template #title>销售统计</template>
        </el-menu-item>
        <el-menu-item index="/dining-tables">
          <el-icon><Grid /></el-icon>
          <template #title>桌台管理</template>
        </el-menu-item>
        <el-sub-menu index="/points">
          <template #title>
            <el-icon><Coin /></el-icon>
            <span>积分管理</span>
          </template>
          <el-menu-item index="/point-logs">积分流水</el-menu-item>
          <el-menu-item index="/points-mall">积分商城</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <div class="page-title-wrapper">
            <div class="page-title-bar"></div>
            <span class="page-title">{{ currentTitle }}</span>
          </div>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="admin-name">
              <el-icon><UserFilled /></el-icon>
              {{ adminInfo?.username || '管理员' }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>

    <!-- 未处理订单悬浮提示 -->
    <OrderNotification />
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAdminInfo, removeToken } from '../../utils/auth'
import { useOrderNotification } from '../../composables/useOrderNotification'
import OrderNotification from '../../components/OrderNotification.vue'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)
const adminInfo = getAdminInfo()

// 新订单提醒
const { startPolling, stopPolling } = useOrderNotification()

onMounted(() => {
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/products')) return '/products'
  if (path.startsWith('/orders')) return '/orders'
  if (path.startsWith('/dining-tables')) return '/dining-tables'
  if (path.startsWith('/point-logs') || path.startsWith('/points-mall')) return path
  return path
})

const currentTitle = computed(() => route.meta.title || '数据概览')

function handleCommand(cmd) {
  if (cmd === 'logout') {
    removeToken()
    router.push('/login')
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.sidebar {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  transition: width 0.3s;
  overflow: hidden;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.15);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  white-space: nowrap;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 2px;
}

.sidebar-menu {
  border-right: none;
  padding: 8px 0;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}

.sidebar-menu .el-menu-item {
  margin: 4px 8px;
  border-radius: 8px;
  height: 48px;
  line-height: 48px;
  transition: all 0.3s ease;
}

.sidebar-menu .el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.08) !important;
}

.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.3), rgba(255, 142, 83, 0.2)) !important;
  color: #fff !important;
  font-weight: 600;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);
  padding: 0 24px;
  height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
  transition: color 0.2s ease;
  padding: 6px;
  border-radius: 6px;
}

.collapse-btn:hover {
  color: #FF6B6B;
  background-color: #fff5f5;
}

.page-title-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-title-bar {
  width: 3px;
  height: 18px;
  background: linear-gradient(180deg, #FF6B6B, #FF8E53);
  border-radius: 2px;
}

.page-title {
  font-size: 17px;
  font-weight: 700;
  color: #1a1a2e;
}

.header-right {
  display: flex;
  align-items: center;
}

.admin-name {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
  padding: 6px 12px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.admin-name:hover {
  background-color: #f5f5f5;
  color: #FF6B6B;
}

.main-content {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
