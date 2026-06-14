<template>
  <div v-loading="loading">
    <div class="page-header">
      <h2>订单详情</h2>
      <div class="header-actions">
        <template v-if="order">
          <el-button v-if="order.orderStatus === 1" type="success" @click="handleStartProduction">开始制作</el-button>
          <el-button v-if="order.orderStatus === 1" type="danger" @click="handleReject">拒绝订单</el-button>
          <el-button v-if="order.orderStatus === 2" type="primary" @click="handleCompleteProduction">完成订单</el-button>
        </template>
        <el-button @click="$router.back()">返回</el-button>
      </div>
    </div>

    <template v-if="order">
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="detail-card">
            <div class="detail-card-header">
              <div class="detail-card-bar"></div>
              <span class="detail-card-title">订单信息</span>
            </div>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
              <el-descriptions-item label="订单状态">
                <StatusTag :status="order.orderStatus" type="order" />
              </el-descriptions-item>
              <el-descriptions-item label="下单时间">{{ formatTime(order.createdAt) }}</el-descriptions-item>
              <el-descriptions-item label="就餐方式">{{ order.diningType === '1' ? '堂食' : order.diningType === '2' ? '打包' : '-' }}</el-descriptions-item>
              <el-descriptions-item label="桌号">{{ order.tableNumber || '-' }}</el-descriptions-item>
              <el-descriptions-item label="备注">{{ order.remark || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="detail-card">
            <div class="detail-card-header">
              <div class="detail-card-bar"></div>
              <span class="detail-card-title">用户 & 支付</span>
            </div>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="用户">
                <div style="display:flex;align-items:center;gap:8px;">
                  <el-avatar :src="order.userAvatarUrl" :size="28">{{ (order.userNickName || '?')[0] }}</el-avatar>
                  {{ order.userNickName }}
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="订单总额">¥{{ order.totalAmount }}</el-descriptions-item>
              <el-descriptions-item label="优惠金额">¥{{ order.discountAmount }}</el-descriptions-item>
              <el-descriptions-item label="实付金额">
                <span style="font-weight:700;color:#FF6B6B;font-size:16px;">¥{{ order.payAmount }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="支付状态">
                <el-tag v-if="order.paymentStatus === 1" type="success" size="small">已支付</el-tag>
                <el-tag v-else-if="order.paymentStatus === 2" type="danger" size="small">已退款</el-tag>
                <el-tag v-else type="warning" size="small">未支付</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="支付方式">{{ order.paymentMethod || '-' }}</el-descriptions-item>
              <el-descriptions-item label="获得积分">{{ order.pointsEarned }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-col>
      </el-row>

      <div class="detail-card" style="margin-top: 20px;">
        <div class="detail-card-header">
          <div class="detail-card-bar"></div>
          <span class="detail-card-title">商品明细</span>
        </div>
        <el-table :data="order.items" border>
          <el-table-column label="图片" width="70">
            <template #default="{ row }">
              <el-image v-if="row.productImage" :src="row.productImage"
                style="width:48px;height:48px;border-radius:6px;" fit="cover" />
            </template>
          </el-table-column>
          <el-table-column prop="productName" label="商品名称" min-width="150" />
          <el-table-column label="规格" min-width="150">
            <template #default="{ row }">
              <el-tag v-for="opt in (row.options || [])" :key="opt.optionId" size="small"
                style="margin-right:4px;">{{ opt.groupName }}: {{ opt.valueName }}</el-tag>
              <span v-if="!row.options?.length">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="70" />
          <el-table-column label="单价" width="90">
            <template #default="{ row }">¥{{ row.unitPrice }}</template>
          </el-table-column>
          <el-table-column label="小计" width="100">
            <template #default="{ row }">
              <span style="font-weight:600;">¥{{ row.subtotalAmount }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, startProduction, rejectOrder, completeProduction } from '../../api/order'
import StatusTag from '../../components/StatusTag.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const order = ref(null)

onMounted(async () => {
  loading.value = true
  try {
    order.value = await getOrderDetail(route.params.id)
  } finally {
    loading.value = false
  }
})

function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 19) : ''
}

async function handleStartProduction() {
  try {
    await ElMessageBox.confirm('确定开始制作该订单？', '确认', { type: 'info' })
    await startProduction(order.value.id)
    ElMessage.success('已开始制作')
    order.value.orderStatus = 2
  } catch (e) { /* 取消 */ }
}

async function handleReject() {
  try {
    await ElMessageBox.confirm('确定拒绝该订单？将自动退款给用户。', '确认拒绝', { type: 'warning' })
    await rejectOrder(order.value.id)
    ElMessage.success('已拒绝并退款')
    order.value.orderStatus = 4
  } catch (e) { /* 取消 */ }
}

async function handleCompleteProduction() {
  try {
    await ElMessageBox.confirm('确定完成该订单的制作？', '确认', { type: 'success' })
    await completeProduction(order.value.id)
    ElMessage.success('订单已完成')
    order.value.orderStatus = 3
  } catch (e) { /* 取消 */ }
}
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.detail-card {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #f5f5f5;
}

.detail-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.detail-card-bar {
  width: 4px;
  height: 18px;
  background: linear-gradient(180deg, #FF6B6B, #FF8E53);
  border-radius: 2px;
}

.detail-card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
</style>
