import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// En desarrollo (y en `npm run preview`) /api se reenvia al backend Java (puerto 7070),
// asi el navegador nunca hace pedidos de otro origen y no hace falta CORS.
const proxy = {
  '/api': {
    target: 'http://localhost:7070',
    changeOrigin: true,
  },
};

export default defineConfig({
  plugins: [react()],
  server: { port: 5173, proxy },
  preview: { port: 4173, proxy },
});
