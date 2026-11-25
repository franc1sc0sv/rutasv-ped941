import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Vite corre en Node; se declara solo lo que se usa para no agregar @types/node.
declare const process: { env: Record<string, string | undefined> };

// En desarrollo (y en `npm run preview`) /api se reenvia al backend Java (puerto 7070),
// asi el navegador nunca hace pedidos de otro origen y no hace falta CORS.
const proxy = {
  '/api': {
    // RUTASV_API permite apuntar a otro puerto (por ejemplo el script demo.sh).
    target: process.env.RUTASV_API ?? 'http://localhost:7070',
    changeOrigin: true,
  },
};

export default defineConfig({
  plugins: [react()],
  server: { port: 5173, proxy },
  preview: { port: 4173, proxy },
});
