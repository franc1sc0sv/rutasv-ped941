import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
// Los estilos globales van primero para que el CSS de cada pantalla pueda sobrescribirlos.
import './styles/tokens.css';
import './styles/global.css';
import { App } from './App';
import { SesionProvider } from './sesion/SesionContext';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <SesionProvider>
        <App />
      </SesionProvider>
    </BrowserRouter>
  </StrictMode>,
);
