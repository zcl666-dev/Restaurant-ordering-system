<template>
  <div class="staff-order-page">
    <!-- 左侧：商品选择区 -->
    <div class="product-area">
      <div class="page-header">
        <h2>代客点餐</h2>
      </div>

      <!-- 分类标签 -->
      <div class="category-tabs" ref="categoryTabsRef">
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="category-tab"
          :class="{ active: activeCategoryId === cat.id }"
          @click="scrollToCategory(cat.id)"
        >
          {{ cat.categoryName }}
        </div>
      </div>

      <!-- 商品列表 -->
      <div class="product-grid" ref="productGridRef">
        <div
          v-for="cat in categories"
          :key="cat.id"
          :id="'cat-' + cat.id"
          class="category-section"
        >
          <div class="category-title">{{ cat.categoryName }}</div>
          <div class="product-cards">
            <div
              v-for="product in cat.products"
              :key="product.id"
              class="product-card"
              :class="{
                'in-cart': getCartQuantity(product.id) > 0,
                'sold-out': product.stock <= 0
              }"
              @click="handleProductClick(product)"
            >
              <!-- 已加购角标 -->
              <div v-if="getCartQuantity(product.id) > 0" class="cart-badge">
                {{ getCartQuantity(product.id) }}
              </div>
              <!-- 售罄标签 -->
              <div v-if="product.stock <= 0" class="sold-out-tag">估清</div>
              <!-- 菜品图片 -->
              <div class="product-image">
                <img
                  :src="product.productImage || '/placeholder.png'"
                  :alt="product.productName"
                  @error="handleImageError"
                />
              </div>
              <!-- 菜品信息 -->
              <div class="product-info">
                <div class="product-name">{{ product.productName }}</div>
                <div class="product-meta">
                  <span class="product-price">¥{{ formatPrice(product.price) }}</span>
                  <span class="product-sales">月售 {{ product.salesCount || 0 }} 份</span>
                </div>
                <div class="product-desc" v-if="product.desc">{{ product.desc }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧：购物车面板 -->
    <div class="cart-panel">
      <div class="cart-header">
        <span class="cart-title">已选商品</span>
        <span class="cart-count">{{ cartTotalCount }} 件</span>
      </div>

      <!-- 购物车列表 -->
      <div class="cart-items">
        <div v-if="cart.length === 0" class="cart-empty">
          <div class="empty-icon">🛒</div>
          <div class="empty-text">暂无商品，请点选菜品</div>
        </div>
        <div v-for="(item, index) in cart" :key="index" class="cart-item">
          <div class="item-info">
            <div class="item-name">{{ item.productName }}</div>
            <div class="item-option" v-if="item.optionText">{{ item.optionText }}</div>
          </div>
          <div class="item-right">
            <span class="item-price">¥{{ formatPrice(item.subtotal) }}</span>
            <div class="item-quantity">
              <el-button
                size="small"
                circle
                @click="decreaseQuantity(index)"
                :disabled="false"
              >
                <el-icon><Minus /></el-icon>
              </el-button>
              <span class="qty-value">{{ item.quantity }}</span>
              <el-button
                size="small"
                circle
                type="primary"
                @click="increaseQuantity(index)"
              >
                <el-icon><Plus /></el-icon>
              </el-button>
              <el-button
                size="small"
                circle
                type="danger"
                @click="removeFromCart(index)"
                class="btn-delete"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 合计 -->
      <div class="cart-footer">
        <div class="cart-total">
          <span>合计</span>
          <span class="total-price">¥{{ formatPrice(cartTotalAmount) }}</span>
        </div>

        <!-- 桌号 -->
        <div class="cart-field">
          <label>桌号</label>
          <el-select
            v-model="tableNumber"
            placeholder="请选择桌号"
            size="small"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="t in tableList"
              :key="t.id"
              :label="t.tableNo + ' - ' + t.tableName"
              :value="t.tableNo"
            />
          </el-select>
        </div>

        <!-- 就餐方式 -->
        <div class="cart-field">
          <label>就餐方式</label>
          <el-radio-group v-model="diningType" size="small">
            <el-radio-button value="1">堂食</el-radio-button>
            <el-radio-button value="2">打包</el-radio-button>
          </el-radio-group>
        </div>

        <!-- 备注 -->
        <div class="cart-field">
          <label>备注</label>
          <el-input
            v-model="remark"
            type="textarea"
            :rows="2"
            placeholder="选填备注信息"
            size="small"
          />
        </div>

        <!-- 下单按钮 -->
        <el-button
          type="primary"
          class="submit-btn"
          :disabled="cart.length === 0"
          :loading="submitting"
          @click="handleSubmit"
        >
          确认下单
        </el-button>
      </div>
    </div>

    <!-- 规格选择弹窗 -->
    <el-dialog
      v-model="showOptionDialog"
      :title="currentProduct?.productName"
      width="420px"
      :close-on-click-modal="true"
      destroy-on-close
    >
      <div class="option-dialog-content" v-if="currentProduct">
        <div class="dialog-product-image">
          <img
            :src="currentProduct.productImage || '/placeholder.png'"
            :alt="currentProduct.productName"
            @error="handleImageError"
          />
        </div>
        <div class="dialog-product-price">¥{{ formatPrice(currentProduct.price) }}</div>

        <!-- 规格组 -->
        <div
          v-for="group in optionGroups"
          :key="group.groupId"
          class="option-group"
        >
          <div class="option-group-name">{{ group.groupName }}</div>
          <div class="option-values">
            <div
              v-for="option in group.options"
              :key="option.id"
              class="option-value"
              :class="{ selected: selectedOptions[group.groupId] === option.id }"
              @click="selectOption(group.groupId, option.id)"
            >
              {{ option.valueName }}
            </div>
          </div>
        </div>

        <!-- 数量 -->
        <div class="option-quantity">
          <span>数量</span>
          <div class="quantity-control">
            <el-button
              size="small"
              circle
              @click="dialogQuantity > 1 && dialogQuantity--"
              :disabled="dialogQuantity <= 1"
            >
              <el-icon><Minus /></el-icon>
            </el-button>
            <span class="qty-value">{{ dialogQuantity }}</span>
            <el-button size="small" circle type="primary" @click="dialogQuantity++">
              <el-icon><Plus /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button type="primary" @click="confirmAddToCart" class="dialog-add-btn">
          加入购物车 ¥{{ formatPrice(dialogSubtotal) }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Minus, Plus, Delete } from '@element-plus/icons-vue'
import { getStaffProducts, getStaffProductDetail, staffCreateOrder } from '../../api/order'
import { getTableList } from '../../api/table'

// === 数据 ===
const categories = ref([])
const activeCategoryId = ref(null)
const cart = ref([])
const tableNumber = ref('')
const diningType = ref('1')
const remark = ref('')
const submitting = ref(false)
const tableList = ref([])

// 规格弹窗
const showOptionDialog = ref(false)
const currentProduct = ref(null)
const optionGroups = ref([])
const selectedOptions = ref({})
const dialogQuantity = ref(1)

const categoryTabsRef = ref(null)
const productGridRef = ref(null)

// === 计算属性 ===
const cartTotalCount = computed(() => {
  return cart.value.reduce((sum, item) => sum + item.quantity, 0)
})

const cartTotalAmount = computed(() => {
  return cart.value.reduce((sum, item) => sum + item.subtotal, 0)
})

const dialogSubtotal = computed(() => {
  if (!currentProduct.value) return 0
  return currentProduct.value.price * dialogQuantity.value
})

// === 方法 ===
function formatPrice(price) {
  return Number(price || 0).toFixed(2)
}

function handleImageError(e) {
  e.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2Y1ZjVmNSIvPjx0ZXh0IHg9IjUwIiB5PSI1NSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZmlsbD0iI2MwYzBjMCIgZm9udC1zaXplPSIxNCI+5Yqo6ZmoPC90ZXh0Pjwvc3ZnPg=='
}

// 获取购物车中某商品的数量
function getCartQuantity(productId) {
  return cart.value
    .filter(item => item.productId === productId)
    .reduce((sum, item) => sum + item.quantity, 0)
}

// 点击商品
async function handleProductClick(product) {
  if (product.stock <= 0) return

  if (product.hasOptions === 1) {
    // 有规格，打开规格选择弹窗
    try {
      const detail = await getStaffProductDetail(product.id)
      currentProduct.value = product
      optionGroups.value = detail.optionGroups || []
      selectedOptions.value = {}
      // 默认选中每个组的第一个
      optionGroups.value.forEach(group => {
        if (group.options && group.options.length > 0) {
          const defaultOpt = group.options.find(o => o.isDefault) || group.options[0]
          selectedOptions.value[group.groupId] = defaultOpt.id
        }
      })
      dialogQuantity.value = 1
      showOptionDialog.value = true
    } catch (e) {
      ElMessage.error('获取商品详情失败')
    }
  } else {
    // 无规格，直接加入购物车
    addToCart(product, 1, null, '')
  }
}

// 选择规格
function selectOption(groupId, optionId) {
  selectedOptions.value[groupId] = optionId
}

// 构建规格快照 JSON
function buildOptionSnapshot() {
  const snapshot = []
  optionGroups.value.forEach(group => {
    const selectedId = selectedOptions.value[group.groupId]
    if (selectedId) {
      const option = group.options.find(o => o.id === selectedId)
      if (option) {
        snapshot.push({
          groupId: group.groupId,
          groupName: group.groupName,
          optionId: option.id,
          valueName: option.valueName
        })
      }
    }
  })
  return snapshot.length > 0 ? JSON.stringify(snapshot) : null
}

// 构建规格文本
function buildOptionText() {
  const parts = []
  optionGroups.value.forEach(group => {
    const selectedId = selectedOptions.value[group.groupId]
    if (selectedId) {
      const option = group.options.find(o => o.id === selectedId)
      if (option) {
        parts.push(option.valueName)
      }
    }
  })
  return parts.join(' / ')
}

// 确认加入购物车（规格弹窗）
function confirmAddToCart() {
  if (!currentProduct.value) return
  const snapshot = buildOptionSnapshot()
  const optionText = buildOptionText()
  addToCart(currentProduct.value, dialogQuantity.value, snapshot, optionText)
  showOptionDialog.value = false
}

// 加入购物车
function addToCart(product, quantity, optionSnapshot, optionText) {
  // 检查是否已有相同商品+相同规格
  const existingIndex = cart.value.findIndex(
    item => item.productId === product.id && item.optionSnapshot === optionSnapshot
  )

  if (existingIndex >= 0) {
    // 增加数量
    const item = cart.value[existingIndex]
    if (item.quantity + quantity > product.stock) {
      ElMessage.warning('库存不足')
      return
    }
    item.quantity += quantity
    item.subtotal = item.unitPrice * item.quantity
  } else {
    // 新增
    if (quantity > product.stock) {
      ElMessage.warning('库存不足')
      return
    }
    cart.value.push({
      productId: product.id,
      productName: product.productName,
      unitPrice: Number(product.price),
      quantity: quantity,
      subtotal: Number(product.price) * quantity,
      optionSnapshot: optionSnapshot,
      optionText: optionText
    })
  }
}

// 减少数量（数量为1时移除）
function decreaseQuantity(index) {
  const item = cart.value[index]
  if (item.quantity > 1) {
    item.quantity--
    item.subtotal = item.unitPrice * item.quantity
  } else {
    cart.value.splice(index, 1)
  }
}

// 从购物车删除
function removeFromCart(index) {
  cart.value.splice(index, 1)
}

// 增加数量
function increaseQuantity(index) {
  const item = cart.value[index]
  // 找到对应商品获取库存上限
  const product = findProduct(item.productId)
  if (product && item.quantity >= product.stock) {
    ElMessage.warning('库存不足')
    return
  }
  item.quantity++
  item.subtotal = item.unitPrice * item.quantity
}

// 查找商品信息
function findProduct(productId) {
  for (const cat of categories.value) {
    const found = cat.products.find(p => p.id === productId)
    if (found) return found
  }
  return null
}

// 滚动到分类
function scrollToCategory(catId) {
  activeCategoryId.value = catId
  const el = document.getElementById('cat-' + catId)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// 提交订单
async function handleSubmit() {
  if (cart.value.length === 0) {
    ElMessage.warning('请先选择商品')
    return
  }

  submitting.value = true
  try {
    const items = cart.value.map(item => ({
      productId: item.productId,
      quantity: item.quantity,
      optionSnapshot: item.optionSnapshot
    }))

    await staffCreateOrder({
      items,
      tableNumber: tableNumber.value || null,
      diningType: diningType.value,
      remark: remark.value || null
    })

    ElMessage.success('下单成功！')
    // 清空购物车
    cart.value = []
    tableNumber.value = ''
    remark.value = ''
  } catch (e) {
    ElMessage.error(e.message || '下单失败')
  } finally {
    submitting.value = false
  }
}

// 加载商品数据
async function loadProducts() {
  try {
    const data = await getStaffProducts()
    categories.value = data || []
    if (categories.value.length > 0) {
      activeCategoryId.value = categories.value[0].id
    }
  } catch (e) {
    ElMessage.error('获取商品列表失败')
  }
}

// 加载桌台列表
async function loadTables() {
  try {
    const result = await getTableList({ page: 0, size: 200 })
    tableList.value = (result.content || []).filter(t => t.status === 1)
  } catch (e) {
    console.error('获取桌台列表失败:', e)
  }
}

onMounted(() => {
  loadProducts()
  loadTables()
})
</script>

<style scoped>
.staff-order-page {
  display: flex;
  height: calc(100vh - 120px);
  gap: 0;
  background: var(--bg-page);
  border-radius: var(--radius-base);
  overflow: hidden;
}

/* 左侧商品区 */
.product-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--bg-card);
}

.product-area .page-header {
  padding: 16px 20px 0;
  margin-bottom: 0;
}

.product-area .page-header h2 {
  font-size: 20px;
}

/* 分类标签 */
.category-tabs {
  display: flex;
  gap: 8px;
  padding: 12px 20px;
  overflow-x: auto;
  flex-shrink: 0;
  border-bottom: 1px solid #f0f0f0;
}

.category-tabs::-webkit-scrollbar {
  display: none;
}

.category-tab {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.2s;
  background: #f5f5f5;
  color: var(--text-regular);
}

.category-tab.active {
  background: var(--primary);
  color: #fff;
}

.category-tab:hover:not(.active) {
  background: #eee;
}

/* 商品网格 */
.product-grid {
  flex: 1;
  overflow-y: auto;
  padding: 0 20px 20px;
}

.category-section {
  margin-top: 16px;
}

.category-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  padding-left: 10px;
  border-left: 3px solid var(--primary);
}

.product-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
}

.product-card {
  position: relative;
  background: #fff;
  border-radius: var(--radius-base);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
  box-shadow: var(--shadow-sm);
}

.product-card:hover:not(.sold-out) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.product-card.in-cart {
  border-color: var(--primary);
  background: #fff8f8;
}

.product-card.sold-out {
  opacity: 0.5;
  cursor: not-allowed;
  filter: grayscale(0.5);
}

.cart-badge {
  position: absolute;
  top: 6px;
  right: 6px;
  background: var(--primary);
  color: #fff;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  z-index: 2;
}

.sold-out-tag {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 4px 16px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
  z-index: 2;
}

.product-image {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: #f5f5f5;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  padding: 8px 10px 10px;
}

.product-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 34px;
}

.product-meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 6px;
}

.product-price {
  font-size: 16px;
  font-weight: 700;
  color: #ff4d4f;
}

.product-sales {
  font-size: 11px;
  color: var(--text-secondary);
}

.product-desc {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

/* 右侧购物车面板 */
.cart-panel {
  width: 340px;
  background: var(--bg-card);
  display: flex;
  flex-direction: column;
  border-left: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.cart-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.cart-count {
  font-size: 13px;
  color: var(--text-secondary);
}

.cart-items {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
}

.cart-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-secondary);
}

.empty-icon {
  font-size: 40px;
  margin-bottom: 8px;
}

.empty-text {
  font-size: 13px;
}

.cart-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.cart-item:last-child {
  border-bottom: none;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-option {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.item-right {
  text-align: right;
  margin-left: 12px;
}

.item-price {
  font-size: 13px;
  font-weight: 600;
  color: #ff4d4f;
}

.item-quantity {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
}

.qty-value {
  font-size: 14px;
  font-weight: 600;
  min-width: 20px;
  text-align: center;
}

/* 购物车底部 */
.cart-footer {
  border-top: 1px solid #f0f0f0;
  padding: 16px;
}

.cart-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
  color: var(--text-primary);
}

.total-price {
  font-size: 20px;
  font-weight: 700;
  color: #ff4d4f;
}

.cart-field {
  margin-bottom: 10px;
}

.cart-field label {
  display: block;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  margin-top: 8px;
}

/* 规格弹窗 */
.option-dialog-content {
  max-height: 400px;
  overflow-y: auto;
}

.dialog-product-image {
  width: 100%;
  height: 180px;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 12px;
  background: #f5f5f5;
}

.dialog-product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.dialog-product-price {
  font-size: 22px;
  font-weight: 700;
  color: #ff4d4f;
  margin-bottom: 16px;
}

.option-group {
  margin-bottom: 16px;
}

.option-group-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.option-values {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.option-value {
  padding: 6px 16px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  background: #f5f5f5;
  color: var(--text-regular);
  border: 1px solid transparent;
}

.option-value.selected {
  background: #fff0f0;
  color: var(--primary);
  border-color: var(--primary);
}

.option-value:hover:not(.selected) {
  background: #eee;
}

.option-quantity {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-top: 1px solid #f0f0f0;
  margin-top: 8px;
  font-size: 14px;
  color: var(--text-primary);
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dialog-add-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 8px;
}
</style>
