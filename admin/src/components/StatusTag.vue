<template>
  <el-tag :type="tagType" size="small">{{ text }}</el-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: Number, required: true },
  type: { type: String, default: 'order' }
})

const orderStatusMap = {
  0: { text: '待支付', type: 'warning' },
  1: { text: '待制作', type: 'primary' },
  2: { text: '制作中', type: '' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'info' }
}

const productStatusMap = {
  0: { text: '已下架', type: 'info' },
  1: { text: '上架中', type: 'success' },
  2: { text: '已售罄', type: 'danger' }
}

const userStatusMap = {
  0: { text: '已禁用', type: 'danger' },
  1: { text: '正常', type: 'success' }
}

const categoryStatusMap = {
  0: { text: '已禁用', type: 'info' },
  1: { text: '启用中', type: 'success' }
}

const statusMaps = {
  order: orderStatusMap,
  product: productStatusMap,
  user: userStatusMap,
  category: categoryStatusMap
}

const currentMap = computed(() => statusMaps[props.type] || {})
const tagType = computed(() => currentMap.value[props.status]?.type || 'info')
const text = computed(() => currentMap.value[props.status]?.text || '未知')
</script>
