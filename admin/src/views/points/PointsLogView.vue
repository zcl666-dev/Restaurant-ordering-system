<template>
  <div>
    <div class="page-header">
      <h2>积分流水</h2>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索用户昵称/备注"
        clearable
        style="width: 220px;"
        @keyup.enter="loadData"
      />
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userNickname" label="用户" width="120" />
      <el-table-column label="变动类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type === 1 ? 'success' : 'danger'" size="small">
            {{ row.type === 1 ? '收入' : '支出' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="积分变动" width="110">
        <template #default="{ row }">
          <span :style="{ color: row.type === 1 ? '#67c23a' : '#f56c6c', fontWeight: 600 }">
            {{ row.type === 1 ? '+' : '-' }}{{ row.pointsChange }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="balanceAfter" label="变动后余额" width="110" />
      <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
      <el-table-column label="关联订单" width="160">
        <template #default="{ row }">
          <span v-if="row.orderNo">{{ row.orderNo }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
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
import { getPointLogList } from '../../api/points'

const list = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const keyword = ref('')

onMounted(() => loadData())

async function loadData() {
  loading.value = true
  try {
    const params = { page: currentPage.value - 1, size: pageSize }
    if (keyword.value) params.keyword = keyword.value
    const data = await getPointLogList(params)
    list.value = data.content
    total.value = data.totalElements
  } finally {
    loading.value = false
  }
}

function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 19) : ''
}
</script>

<style scoped>
.text-muted {
  color: #c0c4cc;
}
</style>
