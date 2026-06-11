<template>
  <div>
    <div class="page-header">
      <h2>订单管理</h2>
      <div class="voice-switch">
        <span class="voice-label">语音提醒</span>
        <el-switch v-model="voiceEnabled" @change="onVoiceChange" />
      </div>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索订单号/桌号"
        clearable
        style="width: 200px;"
        @keyup.enter="loadData"
      />
      <el-select v-model="filterStatus" placeholder="订单状态" clearable style="width: 130px;" @change="loadData">
        <el-option :value="0" label="待支付" />
        <el-option :value="1" label="已支付" />
        <el-option :value="2" label="制作中" />
        <el-option :value="3" label="待取餐" />
        <el-option :value="4" label="已完成" />
        <el-option :value="5" label="已取消" />
        <el-option :value="6" label="已退款" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期"
        end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 260px;" @change="loadData" />
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="orderNo" label="订单号" min-width="170" />
      <el-table-column prop="userNickName" label="用户" width="100" />
      <el-table-column label="金额" width="100">
        <template #default="{ row }">¥{{ row.payAmount }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <StatusTag :status="row.orderStatus" type="order" />
        </template>
      </el-table-column>
      <el-table-column prop="diningType" label="就餐方式" width="90" />
      <el-table-column label="桌号" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.tableNumber" type="success" size="small">{{ row.tableNumber }}</el-tag>
          <span v-else class="no-table">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="itemCount" label="商品数" width="70" />
      <el-table-column label="下单时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push(`/orders/${row.id}`)">详情</el-button>
          <el-dropdown @command="(cmd) => handleStatusChange(row.id, cmd)" v-if="row.orderStatus < 4">
            <el-button size="small" type="warning">
              更改状态 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :command="2" v-if="row.orderStatus <= 1">制作中</el-dropdown-item>
                <el-dropdown-item :command="3" v-if="row.orderStatus <= 2">待取餐</el-dropdown-item>
                <el-dropdown-item :command="4" v-if="row.orderStatus <= 3">已完成</el-dropdown-item>
                <el-dropdown-item :command="5" divided>取消订单</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination background layout="total, prev, pager, next" :total="total" :page-size="pageSize"
        v-model:current-page="currentPage" @current-change="loadData" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOrderList, updateOrderStatus } from '../../api/order'
import StatusTag from '../../components/StatusTag.vue'
import { ElMessage } from 'element-plus'
import { useOrderNotification } from '../../composables/useOrderNotification'

const { voiceEnabled, setVoiceEnabled } = useOrderNotification()

function onVoiceChange(val) {
  setVoiceEnabled(val)
}

const list = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const filterStatus = ref(null)
const dateRange = ref(null)
const keyword = ref('')

onMounted(() => loadData())

async function loadData() {
  loading.value = true
  try {
    const params = { page: currentPage.value - 1, size: pageSize }
    if (filterStatus.value !== null && filterStatus.value !== '') params.status = filterStatus.value
    if (keyword.value) params.keyword = keyword.value
    if (dateRange.value) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const data = await getOrderList(params)
    list.value = data.content
    total.value = data.totalElements
  } finally {
    loading.value = false
  }
}

function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 19) : ''
}

async function handleStatusChange(id, status) {
  await updateOrderStatus(id, status)
  ElMessage.success('状态已更新')
  loadData()
}
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.voice-switch {
  display: flex;
  align-items: center;
  gap: 8px;
}

.voice-label {
  font-size: 14px;
  color: #606266;
}

.no-table {
  color: #c0c4cc;
}
</style>
