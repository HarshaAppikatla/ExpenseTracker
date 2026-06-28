/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#2563EB',
          hover: '#1D4ED8',
        },
        secondary: {
          DEFAULT: '#14B8A6',
        },
        success: '#22C55E',
        warning: '#F59E0B',
        danger: '#EF4444',
        info: '#3B82F6',
        light: {
          bg: '#F8FAFC',
          surface: '#FFFFFF',
          border: '#E2E8F0',
          text: '#0F172A',
          textSecondary: '#64748B',
        },
        dark: {
          bg: '#0F172A',
          surface: '#1E293B',
          card: '#1E293B',
          border: '#334155',
          text: '#F8FAFC',
        }
      },
      borderRadius: {
        btn: '12px',
        card: '16px',
        dialog: '20px',
        input: '12px',
        img: '16px',
      },
      fontFamily: {
        sans: ['Inter', 'Roboto', 'sans-serif'],
      },
      spacing: {
        '4': '4px',
        '8': '8px',
        '12': '12px',
        '16': '16px',
        '24': '24px',
        '32': '32px',
        '40': '40px',
        '48': '48px',
        '64': '64px',
      }
    },
  },
  plugins: [],
}
