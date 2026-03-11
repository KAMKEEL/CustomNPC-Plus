import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { REPO_NAME } from './src/config.js'

export default defineConfig({
  plugins: [react()],
  base: `/${REPO_NAME}/`,
  build: {
    outDir: '../../.github/pages-dist',
    emptyOutDir: true,
  }
})
