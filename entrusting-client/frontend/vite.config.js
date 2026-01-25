import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
  ],
  server: {
    port: 5173,
    host: true,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true,
      },
      '/trustee-api': {
        target: 'http://127.0.0.1:8082',
        rewrite: (path) => path.replace(/^\/trustee-api/, '/api'),
        changeOrigin: true,
      }
    }
  },
})
