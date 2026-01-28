import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '../../', '');
  return {
    plugins: [
      react(),
      tailwindcss(),
    ],
    envDir: '../../',
    server: {
      port: parseInt(env.VITE_PORT) || 5175,
      host: true,
      proxy: {
        '/api': {
          target: env.VITE_BACKEND_URL || 'http://127.0.0.1:8085',
          changeOrigin: true,
        },
        '/trustee-api': {
          target: env.VITE_TRUSTEE_URL || 'http://127.0.0.1:8088',
          rewrite: (path) => path.replace(/^\/trustee-api/, '/api'),
          changeOrigin: true,
        }
      }
    },
  }
})
