/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // 토스 컬러 팔레트
        toss: {
          blue: {
            50: '#EBF5FF',
            100: '#D6EBFF',
            200: '#ADD6FF',
            300: '#85C2FF',
            400: '#5CADFF',
            500: '#3182F6',  // Primary Blue
            600: '#1B64DA',
            700: '#0D4EAF',
            800: '#063B83',
            900: '#022857',
          },
          gray: {
            50: '#F9FAFB',
            100: '#F2F4F6',
            200: '#E5E8EB',
            300: '#D1D6DB',
            400: '#B0B8C1',
            500: '#8B95A1',
            600: '#6B7684',
            700: '#4E5968',
            800: '#333D4B',
            900: '#191F28',
          },
          red: {
            50: '#FFF0F0',
            100: '#FFE0E0',
            500: '#F04452',
          },
          green: {
            50: '#E8F9EE',
            100: '#D0F4DE',
            500: '#30C85E',
          },
          yellow: {
            50: '#FFF8E6',
            100: '#FFF1CC',
            500: '#FFAA00',
          },
        },
      },
      fontFamily: {
        toss: [
          'Toss Product Sans',
          '-apple-system',
          'BlinkMacSystemFont',
          'Segoe UI',
          'Roboto',
          'Helvetica Neue',
          'Arial',
          'sans-serif',
        ],
      },
      fontSize: {
        // 토스 타이포그래피
        'display-1': ['56px', { lineHeight: '1.2', fontWeight: '700' }],
        'display-2': ['48px', { lineHeight: '1.25', fontWeight: '700' }],
        'heading-1': ['32px', { lineHeight: '1.3', fontWeight: '700' }],
        'heading-2': ['24px', { lineHeight: '1.35', fontWeight: '700' }],
        'heading-3': ['20px', { lineHeight: '1.4', fontWeight: '600' }],
        'body-1': ['16px', { lineHeight: '1.5', fontWeight: '400' }],
        'body-2': ['14px', { lineHeight: '1.5', fontWeight: '400' }],
        'caption': ['12px', { lineHeight: '1.4', fontWeight: '400' }],
      },
      boxShadow: {
        'toss-1': '0 2px 8px rgba(0, 0, 0, 0.08)',
        'toss-2': '0 4px 16px rgba(0, 0, 0, 0.12)',
        'toss-3': '0 8px 32px rgba(0, 0, 0, 0.16)',
      },
      borderRadius: {
        'toss': '16px',
        'toss-sm': '12px',
        'toss-lg': '24px',
      },
      animation: {
        'fade-in': 'fadeIn 0.3s ease-out',
        'slide-up': 'slideUp 0.3s ease-out',
        'slide-down': 'slideDown 0.3s ease-out',
        'scale-in': 'scaleIn 0.2s ease-out',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { transform: 'translateY(20px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        slideDown: {
          '0%': { transform: 'translateY(-20px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        scaleIn: {
          '0%': { transform: 'scale(0.95)', opacity: '0' },
          '100%': { transform: 'scale(1)', opacity: '1' },
        },
      },
    },
  },
  plugins: [],
}
