import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: { // 개발 서버 설정 추가
    host: true, // 모든 네트워크 인터페이스에서 접근 허용
    port: 5173 // 프론트엔드 기본 포트 (변경하지 않는 경우)
  },
  css: { // PostCSS 설정 추가
    postcss: {
      plugins: [
        require('tailwindcss'),
        require('autoprefixer'),
      ],
    },
  }
})
