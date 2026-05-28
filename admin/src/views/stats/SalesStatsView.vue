<template>
  <div>
    <div class="page-header">
      <h2>销售统计</h2>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="period" @change="loadSalesData">
        <el-radio-button value="daily">近30天</el-radio-button>
        <el-radio-button value="weekly">近12周</el-radio-button>
        <el-radio-button value="monthly">近12月</el-radio-button>
      </el-radio-group>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <div class="chart-card">
          <h3>销售趋势</h3>
          <div ref="salesChartRef" style="height: 400px;"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <h3>订单状态分布</h3>
          <div ref="statusChartRef" style="height: 400px;"></div>
        </div>
      </el-col>
    </el-row>

    <div class="chart-card" style="margin-top: 20px;">
      <h3>热销商品排行</h3>
      <div ref="topChartRef" style="height: 400px;"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getSalesStats, getTopProducts, getOrderStatusDistribution } from '../../api/dashboard'

const period = ref('daily')
const salesChartRef = ref()
const statusChartRef = ref()
const topChartRef = ref()
let salesChart, statusChart, topChart

onMounted(async () => {
  await nextTick()
  salesChart = echarts.init(salesChartRef.value)
  statusChart = echarts.init(statusChartRef.value)
  topChart = echarts.init(topChartRef.value)
  window.addEventListener('resize', () => {
    salesChart?.resize()
    statusChart?.resize()
    topChart?.resize()
  })
  loadData()
})

async function loadData() {
  await Promise.all([loadSalesData(), loadStatusData(), loadTopData()])
}

async function loadSalesData() {
  const data = await getSalesStats({ period: period.value })
  salesChart.setOption({
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
      { name: '营收', type: 'line', yAxisIndex: 1, data: data.map(d => d.revenue), itemStyle: { color: '#67c23a' }, smooth: true, areaStyle: { opacity: 0.1 } }
    ]
  })
}

async function loadStatusData() {
  const data = await getOrderStatusDistribution()
  const statusNames = { 0: '待支付', 1: '已支付', 2: '制作中', 3: '待取餐', 4: '已完成', 5: '已取消', 6: '已退款' }
  const pieData = Object.entries(data).map(([k, v]) => ({ name: statusNames[k] || `状态${k}`, value: v }))
  statusChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{ type: 'pie', radius: ['40%', '70%'], label: { show: true, formatter: '{b}\n{c}单' }, data: pieData }]
  })
}

async function loadTopData() {
  const data = await getTopProducts({ limit: 15 })
  topChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value', name: '销量' },
    yAxis: { type: 'category', data: data.map(d => d.productName).reverse(), axisLabel: { width: 100, overflow: 'truncate' } },
    series: [{
      type: 'bar', data: data.map(d => d.totalQuantity).reverse(),
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: '#79bbff' }, { offset: 1, color: '#409eff' }]) },
      barWidth: '60%'
    }]
  })
}
</script>

<style scoped>
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
