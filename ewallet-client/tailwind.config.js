/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        primary: '#3B82F6',
        secondary: '#0F172A',
        tertiary: '#10B981',
        neutral: '#64748B',
      },
    },
  },
  plugins: [],
};
