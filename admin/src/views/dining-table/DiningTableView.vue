<template>
  <div class="dining-table-container">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="桌号">
          <el-input
            v-model="searchForm.tableNo"
            placeholder="请输入桌号"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="桌台名称">
          <el-input
            v-model="searchForm.tableName"
            placeholder="请输入桌台名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>桌台列表</span>
          <div class="header-buttons">
            <el-button type="primary" @click="handleAdd">新增桌台</el-button>
            <el-button type="success" @click="handleBatchGenerate" :loading="batchLoading">
              批量生成二维码
            </el-button>
          </div>
        </div>
      </template>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="tableList"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="tableNo" label="桌号" width="120" />
        <el-table-column prop="tableName" label="桌台名称" min-width="150" />
        <el-table-column prop="seatCount" label="座位数" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="二维码" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.qrCodeUrl"
              :src="row.qrCodeUrl"
              :preview-src-list="[row.qrCodeUrl]"
              fit="cover"
              class="qr-thumbnail"
            />
            <span v-else class="no-qr">未生成</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="350" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
            <el-button
              v-if="!row.qrCodeUrl"
              type="success"
              link
              @click="handleGenerateQr(row)"
            >
              生成二维码
            </el-button>
            <el-button
              v-if="row.qrCodeUrl"
              type="warning"
              link
              @click="handleRegenerateQr(row)"
            >
              重新生成
            </el-button>
            <el-button
              v-if="row.qrCodeUrl"
              type="info"
              link
              @click="handleDownloadQr(row)"
            >
              下载二维码
            </el-button>
            <el-button
              v-if="row.qrCodeUrl"
              type="primary"
              link
              @click="handleViewQr(row)"
            >
              查看二维码
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增桌台' : '编辑桌台'"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="桌号" prop="tableNo">
          <el-input
            v-model="formData.tableNo"
            placeholder="如：A01、B01、VIP01"
            :disabled="dialogType === 'edit'"
          />
        </el-form-item>
        <el-form-item label="桌台名称" prop="tableName">
          <el-input v-model="formData.tableName" placeholder="请输入桌台名称" />
        </el-form-item>
        <el-form-item label="座位数" prop="seatCount">
          <el-input-number v-model="formData.seatCount" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 二维码预览弹窗 -->
    <el-dialog v-model="qrDialogVisible" title="二维码预览" width="400px">
      <div class="qr-preview">
        <el-image
          :src="currentQrUrl"
          fit="contain"
          class="qr-preview-image"
        />
        <p class="qr-table-info">{{ currentTable?.tableNo }} - {{ currentTable?.tableName }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getTableList,
  getTableDetail,
  addTable,
  updateTable,
  deleteTable,
  generateQrCode,
  batchGenerateQrCode,
  downloadQrCode
} from '../../api/table'

// 搜索表单
const searchForm = reactive({
  tableNo: '',
  tableName: '',
  status: null
})

// 表格数据
const loading = ref(false)
const tableList = ref([])
const selectedRows = ref([])

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 弹窗
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)
const submitLoading = ref(false)

// 表单数据
const formData = reactive({
  id: null,
  tableNo: '',
  tableName: '',
  seatCount: 4,
  status: 1
})

// 表单校验规则
const formRules = {
  tableNo: [
    { required: true, message: '请输入桌号', trigger: 'blur' }
  ],
  tableName: [
    { required: true, message: '请输入桌台名称', trigger: 'blur' }
  ],
  seatCount: [
    { required: true, message: '请输入座位数', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 二维码相关
const batchLoading = ref(false)
const qrDialogVisible = ref(false)
const currentQrUrl = ref('')
const currentTable = ref(null)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize,
      tableNo: searchForm.tableNo || undefined,
      tableName: searchForm.tableName || undefined,
      status: searchForm.status
    }
    const result = await getTableList(params)
    tableList.value = result.content
    pagination.total = result.totalElements
  } catch (error) {
    console.error('获取桌台列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.tableNo = ''
  searchForm.tableName = ''
  searchForm.status = null
  handleSearch()
}

// 分页
const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadData()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadData()
}

// 选择
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 新增
const handleAdd = () => {
  dialogType.value = 'add'
  formData.id = null
  formData.tableNo = ''
  formData.tableName = ''
  formData.seatCount = 4
  formData.status = 1
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogType.value = 'edit'
  formData.id = row.id
  formData.tableNo = row.tableNo
  formData.tableName = row.tableName
  formData.seatCount = row.seatCount
  formData.status = row.status
  dialogVisible.value = true
}

// 提交
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data = {
      tableNo: formData.tableNo,
      tableName: formData.tableName,
      seatCount: formData.seatCount,
      status: formData.status
    }
    if (dialogType.value === 'add') {
      await addTable(data)
      ElMessage.success('创建成功')
    } else {
      await updateTable(formData.id, data)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('操作失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 删除
const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除当前桌台吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  try {
    await deleteTable(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 生成二维码
const handleGenerateQr = async (row) => {
  try {
    await generateQrCode(row.id)
    ElMessage.success('生成成功')
    loadData()
  } catch (error) {
    console.error('生成二维码失败:', error)
  }
}

// 重新生成二维码
const handleRegenerateQr = async (row) => {
  await ElMessageBox.confirm('确定重新生成二维码吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  try {
    await generateQrCode(row.id)
    ElMessage.success('重新生成成功')
    loadData()
  } catch (error) {
    console.error('重新生成二维码失败:', error)
  }
}

// 下载二维码
const handleDownloadQr = async (row) => {
  try {
    const blob = await downloadQrCode(row.id)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = row.tableNo + '.png'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (error) {
    console.error('下载二维码失败:', error)
  }
}

// 查看二维码
const handleViewQr = (row) => {
  currentTable.value = row
  currentQrUrl.value = row.qrCodeUrl
  qrDialogVisible.value = true
}

// 批量生成
const handleBatchGenerate = async () => {
  batchLoading.value = true
  try {
    const result = await batchGenerateQrCode()
    ElMessage.success(`批量生成完成：成功${result.successCount}个，失败${result.failCount}个，耗时${result.timeCost}ms`)
    loadData()
  } catch (error) {
    console.error('批量生成失败:', error)
  } finally {
    batchLoading.value = false
  }
}

// 关闭弹窗
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dining-table-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-buttons {
  display: flex;
  gap: 10px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.qr-thumbnail {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  cursor: pointer;
}

.no-qr {
  color: #909399;
  font-size: 13px;
}

.qr-preview {
  text-align: center;
}

.qr-preview-image {
  width: 300px;
  height: 300px;
}

.qr-table-info {
  margin-top: 15px;
  font-size: 16px;
  color: #303133;
}
</style>
