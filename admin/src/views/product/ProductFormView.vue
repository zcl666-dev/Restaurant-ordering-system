<template>
  <div>
    <div class="page-header">
      <h2>{{ isEdit ? '编辑商品' : '新增商品' }}</h2>
      <el-button @click="$router.back()">返回</el-button>
    </div>

    <div class="form-container" v-loading="loading">
      <!-- 基本信息 -->
      <div class="form-section">
        <div class="form-section-header">
          <div class="form-section-bar"></div>
          <span class="form-section-title">基本信息</span>
        </div>
        <el-form :model="form" label-width="100px">
          <el-form-item label="商品分类" required>
            <el-select v-model="form.categoryId" placeholder="请选择分类" style="width:100%;">
              <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="商品名称" required>
            <el-input v-model="form.productName" placeholder="请输入商品名称" />
          </el-form-item>
          <el-form-item label="商品图片">
            <div class="image-upload-area">
              <div class="image-slot" @click="triggerFileSelect">
                <img v-if="imagePreviewUrl" :src="imagePreviewUrl" class="preview-img" />
                <el-icon v-else class="plus-icon"><Plus /></el-icon>
              </div>
              <input
                ref="fileInputRef"
                type="file"
                accept="image/*"
                hidden
                @change="onFileSelected"
              />
              <div v-if="selectedFile" class="file-info">
                <span class="file-name">{{ selectedFile.name }}</span>
                <el-button type="danger" size="small" text @click="clearFile">取消</el-button>
              </div>
              <div v-else-if="form.productImage && !imagePreviewUrl" class="file-info">
                <span class="file-name current-url">当前: {{ form.productImage }}</span>
              </div>
            </div>
          </el-form-item>
          <el-form-item label="商品描述">
            <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
          </el-form-item>
        </el-form>
      </div>

      <!-- 价格与库存 -->
      <div class="form-section">
        <div class="form-section-header">
          <div class="form-section-bar"></div>
          <span class="form-section-title">价格与库存</span>
        </div>
        <el-form :model="form" label-width="100px">
          <el-form-item label="价格" required>
            <el-input-number v-model="form.price" :min="0" :precision="2" :step="0.5" />
          </el-form-item>
          <el-form-item label="库存">
            <el-input-number v-model="form.stock" :min="0" />
          </el-form-item>
          <el-form-item label="商品类型">
            <el-select v-model="form.isExchangeable">
              <el-option :value="0" label="普通商品" />
              <el-option :value="1" label="可兑换商品" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <!-- 高级设置 -->
      <div class="form-section">
        <div class="form-section-header">
          <div class="form-section-bar"></div>
          <span class="form-section-title">高级设置</span>
        </div>
        <el-form :model="form" label-width="100px">
          <el-form-item label="有规格选项">
            <el-switch v-model="form.hasOptions" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item v-if="form.hasOptions === 1" label="规格组">
            <el-select
              v-model="form.optionGroupIds"
              multiple
              collapse-tags
              collapse-tags-tooltip
              placeholder="请选择规格组"
              style="width:100%;"
            >
              <el-option
                v-for="g in optionGroups"
                :key="g.id"
                :label="g.groupName"
                :value="g.id"
              />
            </el-select>
            <div class="form-tip">选择此商品可使用的规格组（如辣度、份量等）</div>
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
        </el-form>
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button type="primary" :loading="saving" @click="handleSave" size="large">保存</el-button>
        <el-button @click="$router.back()" size="large">取消</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail, createProduct, updateProduct } from '../../api/product'
import { getCategoryList } from '../../api/category'
import { uploadImage } from '../../api/oss'
import { getOptionGroups } from '../../api/option'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const loading = ref(false)
const saving = ref(false)
const categories = ref([])
const optionGroups = ref([])

// 图片上传相关
const selectedFile = ref(null)
const imagePreviewUrl = ref('')
const fileInputRef = ref(null)
const originalImageUrl = ref('') // 编辑模式下保存原始图片 URL

const form = ref({
  categoryId: null, productName: '', productImage: '', description: '',
  price: 0, stock: 0, productType: 0, hasOptions: 0, isRecommend: 0, isHot: 0, isExchangeable: 0, status: 1,
  optionGroupIds: []
})

onMounted(async () => {
  const [catList, groupList] = await Promise.all([getCategoryList(), getOptionGroups()])
  categories.value = catList
  optionGroups.value = groupList
  if (isEdit.value) {
    loading.value = true
    try {
      const data = await getProductDetail(route.params.id)
      form.value = {
        categoryId: data.categoryId, productName: data.productName,
        productImage: data.productImage, description: data.description,
        price: data.price, stock: data.stock, productType: data.productType,
        hasOptions: data.hasOptions, isRecommend: data.isRecommend,
        isHot: data.isHot, isExchangeable: data.isExchangeable, status: data.status,
        optionGroupIds: data.optionGroupIds || []
      }
      // 编辑模式下显示已有图片
      if (data.productImage) {
        imagePreviewUrl.value = data.productImage
        originalImageUrl.value = data.productImage
      }
    } finally {
      loading.value = false
    }
  }
})

// 图片选择相关函数
function triggerFileSelect() {
  fileInputRef.value.click()
}

function onFileSelected(e) {
  const file = e.target.files[0]
  if (!file) return
  // 校验文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  // 校验文件大小（10MB）
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 10MB')
    return
  }
  selectedFile.value = file
  imagePreviewUrl.value = URL.createObjectURL(file)
  // 清空 input 以便重复选择同一文件
  e.target.value = ''
}

function clearFile() {
  selectedFile.value = null
  // 恢复原始图片（编辑模式）或清空（新增模式）
  imagePreviewUrl.value = originalImageUrl.value
  form.value.productImage = originalImageUrl.value
}

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
    // 如果有新选择的图片，先上传到 OSS（通过后端代理）
    if (selectedFile.value) {
      ElMessage.info('正在上传图片...')
      const imageUrl = await uploadImage(selectedFile.value)
      form.value.productImage = imageUrl
    }
    // 保存商品
    if (isEdit.value) {
      await updateProduct(route.params.id, form.value)
    } else {
      await createProduct(form.value)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    router.push('/products')
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.form-container {
  max-width: 720px;
}

.form-section {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #f5f5f5;
}

.form-section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f5f5f5;
}

.form-section-bar {
  width: 4px;
  height: 18px;
  background: linear-gradient(180deg, #FF6B6B, #FF8E53);
  border-radius: 2px;
}

.form-section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.form-actions {
  background: #fff;
  border-radius: 10px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #f5f5f5;
  display: flex;
  gap: 12px;
}

/* 图片上传区域 */
.image-upload-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.image-slot {
  width: 148px;
  height: 148px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.3s;
  overflow: hidden;
  background: #fafafa;
}

.image-slot:hover {
  border-color: #409eff;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.plus-icon {
  font-size: 28px;
  color: #8c939d;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-name {
  font-size: 13px;
  color: #606266;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-name.current-url {
  color: #909399;
  font-size: 12px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
