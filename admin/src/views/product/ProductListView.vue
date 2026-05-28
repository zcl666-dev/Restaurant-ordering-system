<template>
  <div>
    <div class="page-header">
      <h2>商品管理</h2>
      <el-button type="primary" @click="$router.push('/products/create')">新增商品</el-button>
    </div>

    <div class="filter-bar">
      <el-input v-model="keyword" placeholder="搜索商品名称" clearable style="width: 200px;" @clear="loadData"
        @keyup.enter="loadData">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="filterCategory" placeholder="选择分类" clearable style="width: 150px;" @change="loadData">
        <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
      </el-select>
      <el-select v-model="filterStatus" placeholder="商品状态" clearable style="width: 120px;" @change="loadData">
        <el-option :value="1" label="上架中" />
        <el-option :value="0" label="已下架" />
        <el-option :value="2" label="已售罄" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="图片" width="70">
        <template #default="{ row }">
          <el-image v-if="row.productImage" :src="row.productImage"
            style="width:48px;height:48px;border-radius:4px;" fit="cover" />
        </template>
      </el-table-column>
      <el-table-column prop="productName" label="商品名称" min-width="150" />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column label="价格" width="90">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="70" />
      <el-table-column prop="salesCount" label="销量" width="70" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <StatusTag :status="row.status" type="product" />
        </template>
      </el-table-column>
      <el-table-column label="推荐/热销" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.isRecommend" size="small" type="warning" style="margin-right:4px;">推荐</el-tag>
          <el-tag v-if="row.isHot" size="small" type="danger">热销</el-tag>
          <span v-if="!row.isRecommend && !row.isHot">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push(`/products/${row.id}/edit`)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'"
            @click="handleToggle(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-popconfirm title="确定下架该商品？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
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
import { getProductList, deleteProduct, toggleProductStatus } from '../../api/product'
import { getCategoryList } from '../../api/category'
import StatusTag from '../../components/StatusTag.vue'
import { ElMessage } from 'element-plus'

const list = ref([])
const categories = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const keyword = ref('')
const filterCategory = ref(null)
const filterStatus = ref(null)

onMounted(async () => {
  categories.value = await getCategoryList()
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const params = { page: currentPage.value - 1, size: pageSize }
    if (keyword.value) params.keyword = keyword.value
    if (filterCategory.value) params.categoryId = filterCategory.value
    if (filterStatus.value !== null && filterStatus.value !== '') params.status = filterStatus.value
    const data = await getProductList(params)
    list.value = data.content
    total.value = data.totalElements
  } finally {
    loading.value = false
  }
}

async function handleToggle(row) {
  const newStatus = row.status === 1 ? 0 : 1
  await toggleProductStatus(row.id, newStatus)
  ElMessage.success('状态已更新')
  loadData()
}

async function handleDelete(id) {
  await deleteProduct(id)
  ElMessage.success('已删除')
  loadData()
}
</script>
