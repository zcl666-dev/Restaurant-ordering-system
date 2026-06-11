import { ref, computed, onUnmounted } from 'vue'
import { ElNotification } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '../api/request'

const VOICE_KEY = 'admin_order_voice_enabled'
const LAST_ORDER_ID_KEY = 'admin_last_order_id'

// 模块级单例状态（所有组件共享）
const lastOrderId = ref(Number(localStorage.getItem(LAST_ORDER_ID_KEY)) || 0)
const newOrders = ref([])
const voiceEnabled = ref(localStorage.getItem(VOICE_KEY) !== 'false') // 默认开启
const pollingTimer = ref(null)
const isInitialized = ref(false)

/**
 * 新订单提醒 Composable
 * 提供轮询检测、弹窗通知、语音播报、语音开关功能
 */
export function useOrderNotification() {
  const router = useRouter()

  /**
   * 初始化 lastOrderId（从后端获取当前最大订单 ID）
   */
  async function initLastOrderId() {
    if (lastOrderId.value > 0) {
      isInitialized.value = true
      return
    }
    try {
      const data = await request.get('/api_admin_orders_check_new.action')
      if (data && data.lastOrderId !== undefined) {
        lastOrderId.value = data.lastOrderId
        localStorage.setItem(LAST_ORDER_ID_KEY, String(lastOrderId.value))
      }
    } catch (e) {
      console.warn('[订单提醒] 初始化 lastOrderId 失败:', e.message)
    }
    isInitialized.value = true
  }

  /**
   * 轮询检查新订单
   */
  async function checkNewOrders() {
    if (!isInitialized.value) return
    try {
      const data = await request.get('/api_admin_orders_check_new.action', {
        params: { lastOrderId: lastOrderId.value }
      })
      if (!data) return

      const orders = data.newOrders || []
      const newMaxId = data.lastOrderId

      if (newMaxId > lastOrderId.value) {
        lastOrderId.value = newMaxId
        localStorage.setItem(LAST_ORDER_ID_KEY, String(lastOrderId.value))
      }

      if (orders.length > 0) {
        newOrders.value = orders
        showNotification(orders)
        speakNotification()
      }
    } catch (e) {
      // 静默处理，避免打扰管理员
      console.warn('[订单提醒] 轮询失败:', e.message)
    }
  }

  /**
   * 弹窗通知（Element Plus Notification，非阻塞，右上角）
   */
  function showNotification(orders) {
    const count = orders.length
    const firstOrder = orders[0]
    const message = count === 1
      ? `用户 ${firstOrder.userNickName} 下了一笔新订单（¥${firstOrder.payAmount}），请及时处理`
      : `有 ${count} 笔新订单，请及时处理`

    ElNotification({
      title: '🔔 新订单提醒',
      message,
      type: 'warning',
      duration: 6000,
      onClick: () => {
        router.push('/orders')
      }
    })
  }

  /**
   * 语音播报（Web Speech API）
   */
  function speakNotification() {
    if (!voiceEnabled.value) return
    if (!window.speechSynthesis) return

    // 取消之前的语音，避免重叠
    window.speechSynthesis.cancel()

    const utterance = new SpeechSynthesisUtterance('有新的订单，请及时处理')
    utterance.lang = 'zh-CN'
    utterance.rate = 1.0
    utterance.pitch = 1.0
    window.speechSynthesis.speak(utterance)
  }

  /**
   * 设置语音开关
   */
  function setVoiceEnabled(val) {
    voiceEnabled.value = val
    localStorage.setItem(VOICE_KEY, String(val))
  }

  /**
   * 开始轮询（每 15 秒）
   */
  async function startPolling() {
    await initLastOrderId()
    // 立即检查一次
    checkNewOrders()
    // 每 15 秒轮询
    pollingTimer.value = setInterval(checkNewOrders, 15000)
  }

  /**
   * 停止轮询
   */
  function stopPolling() {
    if (pollingTimer.value) {
      clearInterval(pollingTimer.value)
      pollingTimer.value = null
    }
  }

  /**
   * 清除新订单标记
   */
  function dismissNewOrders() {
    newOrders.value = []
  }

  return {
    lastOrderId,
    newOrders,
    voiceEnabled,
    hasNewOrders: computed(() => newOrders.value.length > 0),
    startPolling,
    stopPolling,
    setVoiceEnabled,
    dismissNewOrders
  }
}
