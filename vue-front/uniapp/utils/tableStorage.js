/**
 * 桌号本地存储工具
 * 提供桌号的保存、读取、清除功能
 */

const TABLE_NO_KEY = 'tableNo'

/**
 * 保存桌号到本地存储
 * @param {string} tableNo 桌号，如 "A01"
 */
export function saveTableNo(tableNo) {
  if (tableNo) {
    uni.setStorageSync(TABLE_NO_KEY, tableNo)
    console.log('[tableStorage] 已保存桌号:', tableNo)
  }
}

/**
 * 读取本地存储的桌号
 * @returns {string|null} 桌号，未存储时返回 null
 */
export function getTableNo() {
  return uni.getStorageSync(TABLE_NO_KEY) || null
}

/**
 * 清除本地存储的桌号
 */
export function clearTableNo() {
  uni.removeStorageSync(TABLE_NO_KEY)
  console.log('[tableStorage] 已清除桌号')
}
