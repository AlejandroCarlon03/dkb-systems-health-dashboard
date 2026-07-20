import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// The backend runs on 8090 (see application.properties). In dev, Vite serves the UI on 5173
// and proxies API calls to the backend so there are no CORS issues. In a production build, the
// output is emitted straight into Spring's static resources so the app ships as a single JAR.
// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8090',
      '/actuator': 'http://localhost:8090',
    },
  },
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
  },
})
