<script>
	export default {
		onLaunch: function() {
			this.checkLoginStatus()
		},
		onShow: function() {},
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
			}
		}
	}
</script>

<style>
	/*每个页面公共css */
</style>