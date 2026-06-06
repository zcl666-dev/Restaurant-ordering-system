<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-banner">
      <div class="welcome-text">
        <h2 class="welcome-title">欢迎回来，{{ adminName }} 👋</h2>
        <p class="welcome-sub">这是今日的营业数据概览</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card" v-for="card in statCards" :key="card.label" :style="{ '--card-color': card.color }">
        <div class="stat-card-icon" :style="{ background: card.bg }">
          <span class="stat-emoji">{{ card.icon }}</span>
        </div>
        <div class="stat-card-content">
          <div class="label">{{ card.label }}</div>
          <div class="value" :style="{ color: card.color }">{{ card.value }}</div>
        </div>
      </div>
    </div>

    <el-row :gutter="20" style="margin-top: 24px;">
      <el-col :span="16">
        <div class="chart-card">
          <div class="chart-card-header">
            <div class="chart-title-bar"></div>
            <h3>近30天销售趋势</h3>
          </div>
          <div ref="salesChartRef" style="height: 350px;"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <div class="chart-card-header">
            <div class="chart-title-bar"></div>
            <h3>订单状态分布</h3>
          </div>
          <div ref="statusChartRef" style="height: 350px;"></div>
        </div>
      </el-col>
    </el-row>

    <div class="chart-card" style="margin-top: 24px;">
      <div class="chart-card-header">
        <div class="chart-title-bar"></div>
        <h3>热销商品 TOP 10</h3>
      </div>
      <div ref="topChartRef" style="height: 350px;"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getDashboardStats, getSalesStats, getTopProducts, getOrderStatusDistribution } from '../../api/dashboard'
import { getAdminInfo } from '../../utils/auth'

const adminInfo = getAdminInfo()
const adminName = computed(() => adminInfo?.username || '管理员')

const stats = ref({})
const salesChartRef = ref()
const statusChartRef = ref()
const topChartRef = ref()

const statCards = computed(() => [
  { label: '总用户数', value: stats.value.totalUsers ?? '-', color: '#409eff', icon: '👥', bg: 'linear-gradient(135deg, #e8f4fd, #d1ecfa)' },
  { label: '总订单数', value: stats.value.totalOrders ?? '-', color: '#67c23a', icon: '📦', bg: 'linear-gradient(135deg, #e8f8e0, #d4f1c4)' },
  { label: '总营收', value: stats.value.totalRevenue != null ? `¥${stats.value.totalRevenue}` : '-', color: '#e6a23c', icon: '💰', bg: 'linear-gradient(135deg, #fdf6e0, #faecc0)' },
  { label: '今日订单', value: stats.value.todayOrders ?? '-', color: '#FF6B6B', icon: '📊', bg: 'linear-gradient(135deg, #fff0f0, #ffe0e0)' },
  { label: '今日营收', value: stats.value.todayRevenue != null ? `¥${stats.value.todayRevenue}` : '-', color: '#909399', icon: '📈', bg: 'linear-gradient(135deg, #f4f4f5, #e9e9eb)' },
  { label: '待处理订单', value: stats.value.pendingOrders ?? '-', color: '#f56c6c', icon: '⏳', bg: 'linear-gradient(135deg, #fef0f0, #fde2e2)' },
  { label: '商品总数', value: stats.value.productCount ?? '-', color: '#409eff', icon: '🛍', bg: 'linear-gradient(135deg, #e8f4fd, #d1ecfa)' }
])

onMounted(async () => {
  try {
    const [statsData, salesData, topData, statusData] = await Promise.all([
      getDashboardStats(),
      getSalesStats({ period: 'daily' }),
      getTopProducts({ limit: 10 }),
      getOrderStatusDistribution()
    ])
    stats.value = statsData
    await nextTick()
    renderSalesChart(salesData)
    renderStatusChart(statusData)
    renderTopChart(topData)
  } catch (e) {
    console.error(e)
  }
})

function renderSalesChart(data) {
  const chart = echarts.init(salesChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '营收'], top: 4 },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '40px', containLabel: true },
    xAxis: { type: 'category', data: data.map(d => d.date), axisLine: { lineStyle: { color: '#e0e0e0' } }, axisLabel: { color: '#909399' } },
    yAxis: [
      { type: 'value', name: '订单数', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f0f0f0' } } },
      { type: 'value', name: '营收(元)', position: 'right', axisLine: { show: false }, splitLine: { show: false } }
    ],
    series: [
      { name: '订单数', type: 'bar', data: data.map(d => d.orderCount), itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#FF8E53' }, { offset: 1, color: '#FF6B6B' }]), borderRadius: [4, 4, 0, 0] }, barWidth: '40%' },
      { name: '营收', type: 'line', yAxisIndex: 1, data: data.map(d => d.revenue), itemStyle: { color: '#4facfe' }, lineStyle: { width: 3 }, smooth: true, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(79, 172, 254, 0.15)' }, { offset: 1, color: 'rgba(79, 172, 254, 0)' }]) } }
    ]
  })
  window.addEventListener('resize', () => chart.resize())
}

function renderStatusChart(data) {
  const chart = echarts.init(statusChartRef.value)
  const statusNames = { 0: '待支付', 1: '已支付', 2: '制作中', 3: '待取餐', 4: '已完成', 5: '已取消', 6: '已退款' }
  const colors = ['#FF6B6B', '#4facfe', '#f093fb', '#43e97b', '#67c23a', '#909399', '#e6a23c']
  const pieData = Object.entries(data).map(([k, v], i) => ({ name: statusNames[k] || `状态${k}`, value: v, itemStyle: { color: colors[i % colors.length] } }))
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: '#606266' } },
    series: [{
      type: 'pie', radius: ['45%', '70%'],
      label: { show: true, formatter: '{b}: {c}', color: '#606266' },
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.2)' } },
      data: pieData
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

function renderTopChart(data) {
  const chart = echarts.init(topChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '20px', containLabel: true },
    xAxis: { type: 'category', data: data.map(d => d.productName), axisLabel: { rotate: 30, color: '#909399' }, axisLine: { lineStyle: { color: '#e0e0e0' } } },
    yAxis: { type: 'value', name: '销量', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f0f0f0' } } },
    series: [{
      type: 'bar', data: data.map(d => d.totalQuantity),
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#FF8E53' }, { offset: 1, color: '#FF6B6B' }]), borderRadius: [6, 6, 0, 0] },
      barWidth: '50%',
      emphasis: { itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#FF6B6B' }, { offset: 1, color: '#E85555' }]) } }
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}
</script>

<style scoped>
.dashboard {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 欢迎区域 */
.welcome-banner {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border-radius: 12px;
  padding: 28px 32px;
  margin-bottom: 24px;
  box-shadow: 0 4px 16px rgba(255, 107, 107, 0.2);
}

.welcome-title {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 6px;
}

.welcome-sub {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}

/* 统计卡片 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.2s ease;
  border: 1px solid #f5f5f5;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.stat-card-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-emoji {
  font-size: 26px;
}

.stat-card-content {
  min-width: 0;
}

.stat-card .label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.stat-card .value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 图表卡片 */
.chart-card {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #f5f5f5;
}

.chart-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.chart-title-bar {
  width: 4px;
  height: 18px;
  background: linear-gradient(180deg, #FF6B6B, #FF8E53);
  border-radius: 2px;
}

.chart-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
</style>
