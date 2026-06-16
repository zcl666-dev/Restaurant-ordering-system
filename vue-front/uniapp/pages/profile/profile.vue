<template>
  <view class="profile-page">
    <!-- 头像 -->
    <view class="section">
      <view class="section-title">个人信息</view>
      <view class="cell" @tap="chooseAvatar">
        <text class="cell-label">头像</text>
        <view class="cell-right">
          <image v-if="form.avatarUrl" class="avatar-preview" :src="form.avatarUrl" mode="aspectFill" />
          <view v-else class="avatar-placeholder">
            <text class="avatar-placeholder-text">{{ (form.nickName || '用')[0] }}</text>
          </view>
          <text class="cell-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 姓名 -->
    <view class="section">
      <view class="cell">
        <text class="cell-label">姓名</text>
        <view class="cell-right">
          <input class="cell-input" v-model="form.nickName" placeholder="请输入姓名" placeholder-class="placeholder" />
        </view>
      </view>
    </view>

    <!-- 手机 -->
    <view class="section">
      <view class="cell" @tap="editPhone">
        <text class="cell-label">手机</text>
        <view class="cell-right">
          <text class="cell-value">{{ maskedPhone || '未绑定' }}</text>
          <text class="cell-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 性别 -->
    <view class="section">
      <view class="cell">
        <text class="cell-label">性别</text>
        <view class="cell-right gender-group">
          <view class="gender-option" @tap="form.gender = 1">
            <view class="gender-radio" :class="{ active: form.gender === 1 }">
              <view v-if="form.gender === 1" class="gender-dot"></view>
            </view>
            <text class="gender-text">男</text>
          </view>
          <view class="gender-option" @tap="form.gender = 2">
            <view class="gender-radio" :class="{ active: form.gender === 2 }">
              <view v-if="form.gender === 2" class="gender-dot"></view>
            </view>
            <text class="gender-text">女</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 生日 -->
    <view class="section">
      <picker mode="date" :value="form.birthday" @change="onDateChange" start="1900-01-01" end="2026-12-31">
        <view class="cell">
          <text class="cell-label">生日</text>
          <view class="cell-right">
            <text class="cell-value">{{ form.birthday || '请选择' }}</text>
            <text class="cell-arrow">›</text>
          </view>
        </view>
      </picker>
    </view>

    <!-- 保存按钮 -->
    <view class="save-wrapper">
      <button class="save-btn" @tap="handleSave" :disabled="saving">保存</button>
    </view>

    <!-- 手机输入弹窗 -->
    <view v-if="showPhoneModal" class="modal-mask" @tap="showPhoneModal = false">
      <view class="modal-content" @tap.stop>
        <view class="modal-header">
          <text class="modal-title">修改手机号</text>
        </view>
        <view class="modal-body">
          <input class="phone-input" v-model="tempPhone" type="number" maxlength="11"
            placeholder="请输入手机号" placeholder-class="placeholder" />
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel" @tap="showPhoneModal = false">取消</button>
          <button class="modal-btn confirm" @tap="confirmPhone">确定</button>
        </view>
      </view>
    </view>

  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getUserInfo, updateUserProfile } from '@/api/user.js'

const form = reactive({
  nickName: '',
  avatarUrl: '',
  phone: '',
  gender: 0,
  birthday: ''
})

const saving = ref(false)
const showPhoneModal = ref(false)
const tempPhone = ref('')

const maskedPhone = computed(() => {
  if (!form.phone || form.phone.length < 7) return form.phone
  return form.phone.substring(0, 3) + '****' + form.phone.substring(7)
})

const fetchUserInfo = async () => {
  try {
    const data = await getUserInfo()
    if (data) {
      Object.assign(form, {
        nickName: data.nickName || '',
        avatarUrl: data.avatarUrl || '',
        phone: data.phone || '',
        gender: data.gender || 0,
        birthday: data.birthday || ''
      })
    }
  } catch (err) {
    console.error('获取用户信息失败:', err)
  }
}

const chooseAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      // Upload to server
      const baseUrl = require('@/utils/config.js').default.baseURL
      uni.uploadFile({
        url: baseUrl + '/api/upload',
        filePath: tempFilePath,
        name: 'file',
        header: {
          'Authorization': 'Bearer ' + uni.getStorageSync('token')
        },
        success: (uploadRes) => {
          try {
            const result = JSON.parse(uploadRes.data)
            if (result.code === 200 && result.data) {
              form.avatarUrl = result.data
            } else {
              uni.showToast({ title: '上传失败', icon: 'none' })
            }
          } catch (e) {
            uni.showToast({ title: '上传失败', icon: 'none' })
          }
        },
        fail: () => {
          uni.showToast({ title: '上传失败', icon: 'none' })
        }
      })
    }
  })
}

const editPhone = () => {
  tempPhone.value = form.phone || ''
  showPhoneModal.value = true
}

const confirmPhone = () => {
  const phone = tempPhone.value.trim()
  if (phone && !/^1[3-9]\d{9}$/.test(phone)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  form.phone = phone
  showPhoneModal.value = false
}

const onDateChange = (e) => {
  form.birthday = e.detail.value
}

const handleSave = async () => {
  if (!form.nickName.trim()) {
    uni.showToast({ title: '请输入姓名', icon: 'none' })
    return
  }
  saving.value = true
  try {
    await updateUserProfile({
      nickName: form.nickName.trim(),
      avatarUrl: form.avatarUrl,
      phone: form.phone,
      gender: form.gender,
      birthday: form.birthday || null
    })
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (err) {
    uni.showToast({ title: '保存失败', icon: 'none' })
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchUserInfo()
})

onPullDownRefresh(async () => {
  await fetchUserInfo()
  uni.stopPullDownRefresh()
})
</script>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 40rpx;
}

.section {
  margin: 24rpx 24rpx 0;
  background-color: #fff;
  border-radius: 16rpx;
  overflow: hidden;

  &:first-child {
    margin-top: 24rpx;
  }
}

.section-title {
  padding: 24rpx 32rpx 0;
  font-size: 28rpx;
  color: #333;
  font-weight: 600;
}

.cell {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 32rpx;
  min-height: 100rpx;

  &:active {
    background-color: #f8f8f8;
  }
}

.cell-label {
  font-size: 30rpx;
  color: #333;
  flex-shrink: 0;
}

.cell-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex: 1;
  justify-content: flex-end;
}

.cell-input {
  flex: 1;
  text-align: right;
  font-size: 30rpx;
  color: #333;
}

.cell-value {
  font-size: 30rpx;
  color: #999;
}

.cell-arrow {
  font-size: 36rpx;
  color: #ccc;
  flex-shrink: 0;
}

.placeholder {
  color: #ccc;
}

/* 头像 */
.avatar-preview {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
}

.avatar-placeholder {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-placeholder-text {
  font-size: 32rpx;
  font-weight: 700;
  color: #fff;
}

/* 性别选择 */
.gender-group {
  gap: 32rpx !important;
}

.gender-option {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.gender-radio {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  border: 2rpx solid #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.2s;

  &.active {
    border-color: #FF6B6B;
  }
}

.gender-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  background-color: #FF6B6B;
}

.gender-text {
  font-size: 30rpx;
  color: #333;
}

/* 保存按钮 */
.save-wrapper {
  padding: 60rpx 40rpx;
  padding-bottom: calc(60rpx + env(safe-area-inset-bottom));
}

.save-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #FF4D4F, #FF6B6B);
  color: #fff;
  font-size: 34rpx;
  font-weight: 600;
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  letter-spacing: 4rpx;

  &:active {
    opacity: 0.9;
    transform: scale(0.98);
  }

  &[disabled] {
    opacity: 0.5;
  }
}

/* 手机输入弹窗 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal-content {
  width: 600rpx;
  background-color: #fff;
  border-radius: 24rpx;
  overflow: hidden;
}

.modal-header {
  padding: 32rpx;
  text-align: center;
  border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.modal-body {
  padding: 32rpx;
}

.phone-input {
  width: 100%;
  height: 88rpx;
  border: 2rpx solid #eee;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 32rpx;
  color: #333;
  box-sizing: border-box;
}

.modal-footer {
  display: flex;
  border-top: 1rpx solid #f0f0f0;
}

.modal-btn {
  flex: 1;
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  border: none;
  border-radius: 0;
  background: transparent;

  &::after {
    display: none;
  }

  &.cancel {
    color: #999;
    border-right: 1rpx solid #f0f0f0;
  }

  &.confirm {
    color: #FF4D4F;
    font-weight: 600;
  }
}
</style>
