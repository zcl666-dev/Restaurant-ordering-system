<template>
  <div>
    <div class="page-header">
      <h2>销售统计</h2>
    </div>

    <div class="period-card">
      <div class="period-header">
        <div class="period-bar"></div>
        <span class="period-title">数据周期</span>
      </div>
      <el-radio-group v-model="period" @change="loadSalesData">
        <el-radio-button value="daily">近30天</el-radio-button>
        <el-radio-button value="weekly">近12周</el-radio-button>
        <el-radio-button value="monthly">近12月</el-radio-button>
      </el-radio-group>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <div class="chart-card">
          <div class="chart-card-header">
            <div class="chart-title-bar"></div>
            <h3>销售趋势</h3>
          </div>
          <div ref="salesChartRef" style="height: 400px;"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <div class="chart-card-header">
            <div class="chart-title-bar"></div>
            <h3>订单状态分布</h3>
          </div>
          <div ref="statusChartRef" style="height: 400px;"></div>
        </div>
      </el-col>
    </el-row>

    <div class="chart-card" style="margin-top: 20px;">
      <div class="chart-card-header">
        <div class="chart-title-bar"></div>
        <h3>热销商品排行</h3>
      </div>
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
    legend: { data: ['订单数', '营收'], top: 4 },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '40px', containLabel: true },
    xAxis: { type: 'category', data: data.map(d => d.date), axisLine: { lineStyle: { color: '#e0e0e0' } }, axisLabel: { color: '#909399' } },
    yAxis: [
      { type: 'value', name: '订单数', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f0f0f0' } } },
      { type: 'value', name: '营收(元)', position: 'right', axisLine: { show: false }, splitLine: { show: false } }
    ],
    series: [
      { name: '订单数', type: 'bar', data: data.map(d => d.orderCount), itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#FF8E53' }, { offset: 1, color: '#FF6B6B' }]), borderRadius: [4, 4, 0, 0] } },
      { name: '营收', type: 'line', yAxisIndex: 1, data: data.map(d => d.revenue), itemStyle: { color: '#4facfe' }, lineStyle: { width: 3 }, smooth: true, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(79, 172, 254, 0.15)' }, { offset: 1, color: 'rgba(79, 172, 254, 0)' }]) } }
    ]
  })
}

async function loadStatusData() {
  const data = await getOrderStatusDistribution()
  const statusNames = { 0: '待支付', 1: '已支付', 2: '制作中', 3: '待取餐', 4: '已完成', 5: '已取消', 6: '已退款' }
  const colors = ['#FF6B6B', '#4facfe', '#f093fb', '#43e97b', '#67c23a', '#909399', '#e6a23c']
  const pieData = Object.entries(data).map(([k, v], i) => ({ name: statusNames[k] || `状态${k}`, value: v, itemStyle: { color: colors[i % colors.length] } }))
  statusChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: '#606266' } },
    series: [{ type: 'pie', radius: ['45%', '70%'], label: { show: true, formatter: '{b}\n{c}单' }, data: pieData }]
  })
}

async function loadTopData() {
  const data = await getTopProducts({ limit: 15 })
  topChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '20px', containLabel: true },
    xAxis: { type: 'value', name: '销量', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f0f0f0' } } },
    yAxis: { type: 'category', data: data.map(d => d.productName).reverse(), axisLabel: { width: 100, overflow: 'truncate', color: '#606266' } },
    series: [{
      type: 'bar', data: data.map(d => d.totalQuantity).reverse(),
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: '#FF8E53' }, { offset: 1, color: '#FF6B6B' }]), borderRadius: [0, 6, 6, 0] },
      barWidth: '60%',
      emphasis: { itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: '#FF6B6B' }, { offset: 1, color: '#E85555' }]) } }
    }]
  })
}
</script>

<style scoped>
.period-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #f5f5f5;
}

.period-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.period-bar {
  width: 4px;
  height: 18px;
  background: linear-gradient(180deg, #FF6B6B, #FF8E53);
  border-radius: 2px;
}

.period-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

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
