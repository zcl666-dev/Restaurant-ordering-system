<template>
  <div class="dashboard">
    <div class="stat-cards">
      <div class="stat-card" v-for="card in statCards" :key="card.label">
        <div class="label">{{ card.label }}</div>
        <div class="value" :style="{ color: card.color }">{{ card.value }}</div>
      </div>
    </div>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <div class="chart-card">
          <h3>近30天销售趋势</h3>
          <div ref="salesChartRef" style="height: 350px;"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <h3>订单状态分布</h3>
          <div ref="statusChartRef" style="height: 350px;"></div>
        </div>
      </el-col>
    </el-row>

    <div class="chart-card" style="margin-top: 20px;">
      <h3>热销商品 TOP 10</h3>
      <div ref="topChartRef" style="height: 350px;"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getDashboardStats, getSalesStats, getTopProducts, getOrderStatusDistribution } from '../../api/dashboard'

const stats = ref({})
const salesChartRef = ref()
const statusChartRef = ref()
const topChartRef = ref()

const statCards = computed(() => [
  { label: '总用户数', value: stats.value.totalUsers ?? '-', color: '#409eff' },
  { label: '总订单数', value: stats.value.totalOrders ?? '-', color: '#67c23a' },
  { label: '总营收', value: stats.value.totalRevenue != null ? `¥${stats.value.totalRevenue}` : '-', color: '#e6a23c' },
  { label: '今日订单', value: stats.value.todayOrders ?? '-', color: '#f56c6c' },
  { label: '今日营收', value: stats.value.todayRevenue != null ? `¥${stats.value.todayRevenue}` : '-', color: '#909399' },
  { label: '待处理订单', value: stats.value.pendingOrders ?? '-', color: '#f56c6c' },
  { label: '商品总数', value: stats.value.productCount ?? '-', color: '#409eff' }
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
    legend: { data: ['订单数', '营收'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: data.map(d => d.date) },
    yAxis: [
      { type: 'value', name: '订单数' },
      { type: 'value', name: '营收(元)', position: 'right' }
    ],
    series: [
      { name: '订单数', type: 'bar', data: data.map(d => d.orderCount), itemStyle: { color: '#409eff' } },
      { name: '营收', type: 'line', yAxisIndex: 1, data: data.map(d => d.revenue), itemStyle: { color: '#67c23a' }, smooth: true }
    ]
  })
  window.addEventListener('resize', () => chart.resize())
}

function renderStatusChart(data) {
  const chart = echarts.init(statusChartRef.value)
  const statusNames = { 0: '待支付', 1: '已支付', 2: '制作中', 3: '待取餐', 4: '已完成', 5: '已取消', 6: '已退款' }
  const pieData = Object.entries(data).map(([k, v]) => ({ name: statusNames[k] || `状态${k}`, value: v }))
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie', radius: ['40%', '70%'],
      label: { show: true, formatter: '{b}: {c}' },
      data: pieData
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

function renderTopChart(data) {
  const chart = echarts.init(topChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: data.map(d => d.productName), axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', name: '销量' },
    series: [{
      type: 'bar', data: data.map(d => d.totalQuantity),
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#409eff' }, { offset: 1, color: '#79bbff' }]) },
      barWidth: '50%'
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}
</script>

<style scoped>
.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.chart-card h3 {
  font-size: 16px;
  margin-bottom: 16px;
  color: #303133;
}
</style>
