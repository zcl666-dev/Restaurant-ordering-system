<template>
  <div>
    <div class="page-header">
      <h2>积分商城</h2>
      <el-button type="primary" @click="openDialog(null)">添加兑换商品</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column label="商品图片" width="90">
        <template #default="{ row }">
          <el-image v-if="row.productImage" :src="row.productImage"
            style="width:50px;height:50px;border-radius:6px;" fit="cover" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="productName" label="商品名称" min-width="160" />
      <el-table-column prop="pointsRequired" label="所需积分" width="110" />
      <el-table-column label="兑换数量" width="130">
        <template #default="{ row }">
          <template v-if="row.exchangeQuantity > 0">
            <span :style="{ color: row.remainCount <= 0 ? '#f56c6c' : '#67c23a', fontWeight: 600 }">
              {{ row.remainCount }}
            </span>
            <span style="color:#999;"> / {{ row.exchangeQuantity }}</span>
          </template>
          <span v-else>不限量</span>
        </template>
      </el-table-column>
      <el-table-column prop="expireDays" label="保留天数" width="100" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="添加时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-popconfirm title="确定删除该兑换商品？删除后商品将恢复为普通商品。" @confirm="handleDelete(row.id)">
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editId ? '编辑兑换商品' : '添加兑换商品'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="选择商品" required>
          <el-select v-model="form.productId" placeholder="请选择商品" style="width: 100%;"
            filterable :disabled="!!editId">
            <el-option v-for="p in productList" :key="p.id" :label="p.productName" :value="p.id">
              <div style="display:flex;align-items:center;gap:8px;">
                <el-image v-if="p.productImage" :src="p.productImage"
                  style="width:30px;height:30px;border-radius:4px;" fit="cover" />
                <span>{{ p.productName }}</span>
                <span style="color:#999;margin-left:auto;">¥{{ p.price }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="兑换积分" required>
          <el-input-number v-model="form.pointsRequired" :min="1" :step="10" />
        </el-form-item>
        <el-form-item label="兑换数量">
          <el-input-number v-model="form.exchangeQuantity" :min="0" />
          <span class="form-tip">0 表示不限量</span>
        </el-form-item>
        <el-form-item label="保留天数">
          <el-input-number v-model="form.expireDays" :min="1" :max="365" />
          <span class="form-tip">兑换券的有效天数</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option :value="1" label="上架" />
            <el-option :value="0" label="下架" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPointsMallList, createPointsMallItem, updatePointsMallItem, deletePointsMallItem } from '../../api/points'
import { getProductList } from '../../api/product'
import { ElMessage } from 'element-plus'

const list = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10

const dialogVisible = ref(false)
const saving = ref(false)
const editId = ref(null)
const productList = ref([])

const form = ref({
  productId: null,
  pointsRequired: 100,
  exchangeQuantity: 0,
  expireDays: 7,
  status: 1
})

onMounted(() => {
  loadData()
  loadProducts()
})

async function loadData() {
  loading.value = true
  try {
    const data = await getPointsMallList({ page: currentPage.value - 1, size: pageSize })
    list.value = data.content
    total.value = data.totalElements
  } finally {
    loading.value = false
  }
}

async function loadProducts() {
  try {
    const data = await getProductList({ page: 0, size: 999 })
    productList.value = data.content
  } catch (e) {
    // handled by interceptor
  }
}

function openDialog(row) {
  if (row) {
    editId.value = row.id
    form.value = {
      productId: row.productId,
      pointsRequired: row.pointsRequired,
      exchangeQuantity: row.exchangeQuantity,
      expireDays: row.expireDays,
      status: row.status
    }
  } else {
    editId.value = null
    form.value = { productId: null, pointsRequired: 100, exchangeQuantity: 0, expireDays: 7, status: 1 }
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.productId) {
    ElMessage.warning('请选择商品')
    return
  }
  if (!form.value.pointsRequired || form.value.pointsRequired < 1) {
    ElMessage.warning('请输入兑换积分')
    return
  }
  saving.value = true
  try {
    if (editId.value) {
      await updatePointsMallItem({ ...form.value, id: editId.value })
    } else {
      await createPointsMallItem(form.value)
    }
    ElMessage.success(editId.value ? '更新成功' : '添加成功')
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await deletePointsMallItem(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // handled by interceptor
  }
}

function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 19) : ''
}
</script>

<style scoped>
.form-tip {
  color: #909399;
  font-size: 12px;
  margin-left: 10px;
}
</style>
