<template>
  <div>
    <div class="page-header">
      <h2>{{ isEdit ? '编辑商品' : '新增商品' }}</h2>
      <el-button @click="$router.back()">返回</el-button>
    </div>

    <el-card style="max-width: 700px;">
      <el-form :model="form" label-width="100px" v-loading="loading">
        <el-form-item label="商品分类" required>
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width:100%;">
            <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名称" required>
          <el-input v-model="form.productName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品图片">
          <el-input v-model="form.productImage" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
        </el-form-item>
        <el-form-item label="价格" required>
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="0.5" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="商品类型">
          <el-select v-model="form.productType">
            <el-option :value="0" label="普通商品" />
            <el-option :value="1" label="可兑换商品" />
          </el-select>
        </el-form-item>
        <el-form-item label="有规格选项">
          <el-switch v-model="form.hasOptions" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="推荐">
          <el-switch v-model="form.isRecommend" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="热销">
          <el-switch v-model="form.isHot" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option :value="1" label="上架" />
            <el-option :value="0" label="下架" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail, createProduct, updateProduct } from '../../api/product'
import { getCategoryList } from '../../api/category'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const loading = ref(false)
const saving = ref(false)
const categories = ref([])

const form = ref({
  categoryId: null, productName: '', productImage: '', description: '',
  price: 0, stock: 0, productType: 0, hasOptions: 0, isRecommend: 0, isHot: 0, status: 1
})

onMounted(async () => {
  categories.value = await getCategoryList()
  if (isEdit.value) {
    loading.value = true
    try {
      const data = await getProductDetail(route.params.id)
      form.value = {
        categoryId: data.categoryId, productName: data.productName,
        productImage: data.productImage, description: data.description,
        price: data.price, stock: data.stock, productType: data.productType,
        hasOptions: data.hasOptions, isRecommend: data.isRecommend,
        isHot: data.isHot, status: data.status
      }
    } finally {
      loading.value = false
    }
  }
})

async function handleSave() {
  if (!form.value.productName) {
    ElMessage.warning('请输入商品名称')
    return
  }
  if (!form.value.categoryId) {
    ElMessage.warning('请选择分类')
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await updateProduct(route.params.id, form.value)
    } else {
      await createProduct(form.value)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    router.push('/products')
  } finally {
    saving.value = false
  }
}
</script>
