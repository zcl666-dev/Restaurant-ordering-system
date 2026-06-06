import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import { copyFileSync, mkdirSync, existsSync, readdirSync, statSync } from 'fs'
import { join } from 'path'

function copyDir(src, dest) {
  if (!existsSync(src)) return
  mkdirSync(dest, { recursive: true })
  const entries = readdirSync(src, { withFileTypes: true })
  for (const entry of entries) {
    const srcPath = join(src, entry.name)
    const destPath = join(dest, entry.name)
    if (entry.isDirectory()) {
      copyDir(srcPath, destPath)
    } else {
      copyFileSync(srcPath, destPath)
    }
  }
}

function copyStaticPlugin() {
  return {
    name: 'copy-static',
    closeBundle() {
      const srcDir = join(__dirname, 'static')
      const destDir = join(__dirname, 'unpackage/dist/build/mp-weixin/static')
      copyDir(srcDir, destDir)
      console.log('✅ static files copied (build)')
    },
    configureServer(server) {
      server.httpServer?.once('listening', () => {
        setTimeout(() => {
          const srcDir = join(__dirname, 'static')
          const destDir = join(__dirname, 'unpackage/dist/dev/mp-weixin/static')
          copyDir(srcDir, destDir)
          console.log('✅ static files copied (dev)')
        }, 3000)
      })
    }
  }
}

export default defineConfig({
  plugins: [uni(), copyStaticPlugin()]
})
