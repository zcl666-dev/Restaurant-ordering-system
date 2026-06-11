import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // 代理所有 .action 请求到后端（注意加上上下文路径 /restaurant_order_war_exploded）
      '/api_admin': {
        target: 'http://localhost:8080/restaurant_order_war_exploded',
        changeOrigin: true
      },
      '/api_wx': {
        target: 'http://localhost:8080/restaurant_order_war_exploded',
        changeOrigin: true
      },
      '/api_user': {
        target: 'http://localhost:8080/restaurant_order_war_exploded',
        changeOrigin: true
      },
      '/api_product': {
        target: 'http://localhost:8080/restaurant_order_war_exploded',
        changeOrigin: true
      },
      '/api_cart': {
        target: 'http://localhost:8080/restaurant_order_war_exploded',
        changeOrigin: true
      },
      '/api_order': {
        target: 'http://localhost:8080/restaurant_order_war_exploded',
        changeOrigin: true
      },
      '/api_points': {
        target: 'http://localhost:8080/restaurant_order_war_exploded',
        changeOrigin: true
      },
      '/api_subscribe': {
        target: 'http://localhost:8080/restaurant_order_war_exploded',
        changeOrigin: true
      },
      // 代理文件上传接口
      '/api': {
        target: 'http://localhost:8080/restaurant_order_war_exploded',
        changeOrigin: true
      },
      // 代理静态资源（二维码、上传文件）
      '/qrcode': {
        target: 'http://localhost:8080/restaurant_order_war_exploded',
        changeOrigin: true
      },
      '/upload': {
        target: 'http://localhost:8080/restaurant_order_war_exploded',
        changeOrigin: true
      }
    }
  }
})
