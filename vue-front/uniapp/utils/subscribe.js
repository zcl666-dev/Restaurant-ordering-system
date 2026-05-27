import { saveSubscribe } from '@/api/user.js'

const STORAGE_KEY = 'subscribe_asked'

const TEMPLATE_IDS = {
  ORDER_FINISH: 'owRJNezMKIuvTypD-IS_CEcgYbe3rfPz2WdWQyR889c',
  ORDER_CANCEL: 'J2kTKOFZHXW8TuRUYR-QfUPotm0uwS6ft0ikephjEjE',
  MEAL_REMIND: 'iSuL7Y8g3WyG-4VM0tbFrEwvqB95LDqp71k4vx1OTvQ'
}

function getAskedTemplates() {
  try {
    const cache = uni.getStorageSync(STORAGE_KEY)
    return cache ? JSON.parse(cache) : []
  } catch {
    return []
  }
}

function setAskedTemplates(ids) {
  uni.setStorageSync(STORAGE_KEY, JSON.stringify(ids))
}

function hasAsked(templateId) {
  return getAskedTemplates().includes(templateId)
}

function markAsked(templateId) {
  const asked = getAskedTemplates()
  if (!asked.includes(templateId)) {
    asked.push(templateId)
    setAskedTemplates(asked)
  }
}

export function resetSubscribeCache() {
  uni.removeStorageSync(STORAGE_KEY)
}

function requestSubscribeMessage(tmplIds) {
  return new Promise((resolve, reject) => {
    wx.requestSubscribeMessage({
      tmplIds: tmplIds,
      success: resolve,
      fail: reject
    })
  })
}

async function saveEachTemplate(result, tmplIds) {
  for (const tmplId of tmplIds) {
    const subscribeStatus = result[tmplId]
    if (subscribeStatus === undefined) continue
    const isAccepted = subscribeStatus === 'accept'
    markAsked(tmplId)
    try {
      await saveSubscribe({
        templateId: tmplId,
        status: isAccepted ? 1 : 0
      })
    } catch (err) {
      console.error('保存订阅状态失败:', tmplId, err)
    }
  }
}

export async function requestOrderSubscribe() {
  const allTmplIds = Object.values(TEMPLATE_IDS)
  const unaskedIds = allTmplIds.filter(id => !hasAsked(id))

  if (unaskedIds.length === 0) {
    return
  }

  try {
    const result = await requestSubscribeMessage(unaskedIds)
    await saveEachTemplate(result, unaskedIds)
  } catch (err) {
    console.error('订阅消息弹窗异常:', err)
    for (const tmplId of unaskedIds) {
      markAsked(tmplId)
    }
  }
}