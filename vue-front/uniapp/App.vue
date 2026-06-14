<script>
	export default {
		onLaunch: function(options) {
			// 冷启动：先保存桌号，再检查登录（防止 redirectTo 导致桌号丢失）
			this.saveSceneTableNo(options)
			this.checkLoginStatus()
		},
		onShow: function(options) {
			// 热启动：扫码重新进入时捕获桌号
			this.saveSceneTableNo(options)
		},
		onHide: function() {},
		methods: {
			checkLoginStatus() {
				const pages = getCurrentPages()
				if (pages.length === 0) return

				const currentPage = pages[pages.length - 1]
				const currentRoute = currentPage ? currentPage.route : ''

				if (currentRoute === 'pages/login/login') return

				const token = uni.getStorageSync('token')
				if (!this.isTokenValid(token)) {
					this.clearInvalidToken()
					uni.redirectTo({ url: '/pages/login/login' })
				}
			},
			base64Decode(str) {
				try {
					const arrayBuffer = uni.base64ToArrayBuffer(str)
					const bytes = new Uint8Array(arrayBuffer)
					let result = ''
					for (let i = 0; i < bytes.length; i++) {
						result += '%' + ('0' + bytes[i].toString(16)).slice(-2)
					}
					return decodeURIComponent(result)
				} catch (e) {
					return null
				}
			},
			isTokenValid(token) {
				if (!token || typeof token !== 'string') return false
				const parts = token.split('.')
				if (parts.length !== 3) return false
				try {
					let payload = parts[1]
					payload = payload.replace(/-/g, '+').replace(/_/g, '/')
					while (payload.length % 4 !== 0) {
						payload += '='
					}
					const decoded = this.base64Decode(payload)
					if (!decoded) return false
					const data = JSON.parse(decoded)
					if (!data.exp) return true
					const now = Math.floor(Date.now() / 1000)
					return data.exp > now
				} catch (e) {
					return false
				}
			},
			clearInvalidToken() {
				try {
					uni.removeStorageSync('token')
					uni.removeStorageSync('userInfo')
				} catch (e) {}
			},
			/**
			 * 从启动参数中提取桌号并保存到本地存储
			 * 保证在任何页面跳转之前桌号已经持久化
			 */
			saveSceneTableNo(options) {
				try {
					if (options && options.scene) {
						const decoded = decodeURIComponent(options.scene)
						let tableNo = null
						if (decoded.includes('tableNo=')) {
							tableNo = decoded.split('tableNo=')[1]
						} else {
							tableNo = decoded
						}
						if (tableNo) {
							uni.setStorageSync('tableNo', tableNo)
							console.log('[App] 已保存扫码桌号:', tableNo)
						}
					}
				} catch (e) {
					console.error('[App] 保存桌号失败:', e)
				}
			}
		}
	}
</script>

<style>
	/*每个页面公共css */
</style>