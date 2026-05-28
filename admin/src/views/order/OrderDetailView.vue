<template>
  <div v-loading="loading">
    <div class="page-header">
      <h2>订单详情</h2>
      <el-button @click="$router.back()">返回</el-button>
    </div>

    <template v-if="order">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>订单信息</template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
              <el-descriptions-item label="订单状态">
                <StatusTag :status="order.orderStatus" type="order" />
              </el-descriptions-item>
              <el-descriptions-item label="下单时间">{{ formatTime(order.createdAt) }}</el-descriptions-item>
              <el-descriptions-item label="就餐方式">{{ order.diningType || '-' }}</el-descriptions-item>
              <el-descriptions-item label="桌号">{{ order.tableNumber || '-' }}</el-descriptions-item>
              <el-descriptions-item label="备注">{{ order.remark || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>用户 & 支付</template>
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
                <span style="font-weight:700;color:#f56c6c;font-size:16px;">¥{{ order.payAmount }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="支付状态">
                <el-tag v-if="order.paymentStatus === 1" type="success" size="small">已支付</el-tag>
                <el-tag v-else-if="order.paymentStatus === 2" type="danger" size="small">已退款</el-tag>
                <el-tag v-else type="warning" size="small">未支付</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="支付方式">{{ order.paymentMethod || '-' }}</el-descriptions-item>
              <el-descriptions-item label="获得积分">{{ order.pointsEarned }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <el-card style="margin-top: 20px;">
        <template #header>商品明细</template>
        <el-table :data="order.items" border>
          <el-table-column label="图片" width="70">
            <template #default="{ row }">
              <el-image v-if="row.productImage" :src="row.productImage"
                style="width:48px;height:48px;border-radius:4px;" fit="cover" />
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
            <template #default="{ row }">¥{{ row.subtotalAmount }}</template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getOrderDetail } from '../../api/order'
import StatusTag from '../../components/StatusTag.vue'

const route = useRoute()
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
</script>
