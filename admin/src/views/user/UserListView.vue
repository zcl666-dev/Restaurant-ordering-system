<template>
  <div>
    <div class="page-header">
      <h2>用户管理</h2>
    </div>

    <div class="filter-bar">
      <el-input v-model="keyword" placeholder="搜索昵称" clearable style="width: 240px;" @clear="loadData"
        @keyup.enter="loadData">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="头像" width="70">
        <template #default="{ row }">
          <el-avatar :src="row.avatarUrl" :size="36">{{ (row.nickName || '?')[0] }}</el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="nickName" label="昵称" min-width="120" />
      <el-table-column label="余额" width="100">
        <template #default="{ row }">¥{{ row.balance }}</template>
      </el-table-column>
      <el-table-column prop="pointsBalance" label="积分" width="80" />
      <el-table-column label="累计消费" width="110">
        <template #default="{ row }">¥{{ row.totalSpentAmount }}</template>
      </el-table-column>
      <el-table-column prop="totalOrderCount" label="订单数" width="80" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <StatusTag :status="row.status" type="user" />
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-popconfirm title="确定禁用该用户？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button size="small" type="danger" :disabled="row.status === 0">禁用</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination background layout="total, prev, pager, next" :total="total" :page-size="pageSize"
        v-model:current-page="currentPage" @current-change="loadData" />
    </div>

    <el-dialog v-model="editVisible" title="编辑用户" width="450px">
      <el-form label-width="80px">
        <el-form-item label="余额">
          <el-input-number v-model="editForm.balance" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="积分">
          <el-input-number v-model="editForm.pointsBalance" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status">
            <el-option :value="1" label="正常" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserList, updateUser } from '../../api/user'
import StatusTag from '../../components/StatusTag.vue'
import { ElMessage } from 'element-plus'

const list = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const keyword = ref('')

const editVisible = ref(false)
const editId = ref(null)
const editForm = ref({ balance: 0, pointsBalance: 0, status: 1 })

onMounted(() => loadData())

async function loadData() {
  loading.value = true
  try {
    const data = await getUserList({ page: currentPage.value - 1, size: pageSize, keyword: keyword.value })
    list.value = data.content
    total.value = data.totalElements
  } finally {
    loading.value = false
  }
}

function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 19) : ''
}

function handleEdit(row) {
  editId.value = row.id
  editForm.value = { balance: row.balance, pointsBalance: row.pointsBalance, status: row.status }
  editVisible.value = true
}

async function handleSave() {
  await updateUser(editId.value, editForm.value)
  ElMessage.success('更新成功')
  editVisible.value = false
  loadData()
}

async function handleDelete(id) {
  await updateUser(id, { status: 0 })
  ElMessage.success('已禁用')
  loadData()
}
</script>
