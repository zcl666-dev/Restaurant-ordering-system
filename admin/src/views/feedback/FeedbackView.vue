<template>
  <div>
    <div class="page-header">
      <h2>用户反馈</h2>
    </div>

    <div class="filter-bar">
      <el-select v-model="filterRating" placeholder="评分筛选" clearable style="width: 130px;" @change="loadData">
        <el-option :value="5" label="5星 - 非常满意" />
        <el-option :value="4" label="4星 - 满意" />
        <el-option :value="3" label="3星 - 一般" />
        <el-option :value="2" label="2星 - 较差" />
        <el-option :value="1" label="1星 - 非常差" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column type="expand">
        <template #default="{ row }">
          <div class="order-items-expand">
            <div class="expand-header">订单商品</div>
            <div class="items-list">
              <div v-for="(item, idx) in row.items" :key="idx" class="item-row">
                <el-image
                  :src="item.productImage"
                  fit="cover"
                  class="item-image"
                  v-if="item.productImage"
                />
                <div v-else class="item-image-placeholder">🍽</div>
                <div class="item-info">
                  <div class="item-name">{{ item.productName }}</div>
                  <div class="item-price">¥{{ item.unitPrice }} × {{ item.quantity }}</div>
                </div>
                <div class="item-subtotal">¥{{ item.subtotal }}</div>
              </div>
              <div v-if="!row.items || row.items.length === 0" class="no-items">暂无商品信息</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="orderNo" label="订单号" min-width="170" />
      <el-table-column label="用户" width="100">
        <template #default="{ row }">
          <div class="user-cell">
            <el-avatar :size="24" :src="row.userAvatar" v-if="row.userAvatar" />
            <span>{{ row.userName || '-' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="订单金额" width="90">
        <template #default="{ row }">
          <span class="amount">¥{{ row.orderAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="评分" width="140">
        <template #default="{ row }">
          <div class="rating-cell">
            <span v-for="i in 5" :key="i" class="star" :class="{ active: i <= row.rating }">★</span>
            <span class="rating-num">{{ row.rating }}分</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="评价内容" min-width="200">
        <template #default="{ row }">
          <span class="review-content">{{ row.content || '用户未填写评价内容' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="评价时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        v-model:current-page="currentPage"
        @current-change="loadData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getReviewList } from '../../api/review'

const list = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const filterRating = ref(null)

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize
    }
    if (filterRating.value) {
      params.rating = filterRating.value
    }
    const res = await getReviewList(params)
    if (res && res.content) {
      list.value = res.content
      total.value = res.totalElements || 0
    }
  } catch (err) {
    console.error('加载评价列表失败:', err)
  } finally {
    loading.value = false
  }
}

function formatTime(time) {
  if (!time) return '-'
  if (Array.isArray(time)) {
    const [y, m, d, h = 0, min = 0, s = 0] = time
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(min).padStart(2, '0')}:${String(s).padStart(2, '0')}`
  }
  return String(time).replace('T', ' ').substring(0, 19)
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rating-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}

.star {
  font-size: 16px;
  color: #ddd;
}

.star.active {
  color: #ff9500;
}

.rating-num {
  font-size: 12px;
  color: #999;
  margin-left: 4px;
}

.review-content {
  color: #666;
  font-size: 13px;
}

.amount {
  color: #f56c6c;
  font-weight: 600;
}

.order-items-expand {
  padding: 12px 20px;
  background: #fafafa;
}

.expand-header {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.item-row {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid #eee;
}

.item-image {
  width: 50px;
  height: 50px;
  border-radius: 6px;
  flex-shrink: 0;
}

.item-image-placeholder {
  width: 50px;
  height: 50px;
  border-radius: 6px;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  margin-left: 12px;
}

.item-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.item-price {
  font-size: 12px;
  color: #999;
}

.item-subtotal {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-left: 12px;
}

.no-items {
  color: #999;
  font-size: 13px;
  text-align: center;
  padding: 10px;
}
</style>
