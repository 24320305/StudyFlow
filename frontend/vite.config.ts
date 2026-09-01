import { fileURLToPath, URL } from 'node:url'
import vue from '@vitejs/plugin-vue'
import { defineConfig, loadEnv } from 'vite'
import mockPlugin from './mock/index.ts'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  // 默认使用本地 mock；连接真实后端时在 .env 中设置 VITE_USE_MOCK=false
  const useMock = env.VITE_USE_MOCK !== 'false'

  return {
    plugins: [vue(), mockPlugin({ enabled: useMock })],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    server: {
      port: 5173,
      // 关闭 mock 后，把 /api 转发到 Spring Boot 后端（默认 8080）
      proxy: useMock
        ? undefined
        : {
            '/api': {
              target: 'http://localhost:8080',
              changeOrigin: true,
            },
          },
    },
  }
})
