// Ventana base. El modo depende del rol (RN-1):
// - ADMIN: menu lateral a la izquierda (en telefono se abre con el boton "Menu").
// - PASAJERO: barra de pestanas abajo, como los bocetos moviles.
import { useEffect, useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useSesion } from '../sesion/SesionContext';
import { ENTRADAS_ADMIN, ENTRADAS_PASAJERO, type Entrada } from './navegacion';
import './ventana.css';

function Marca() {
  return (
    <span className="marca">
      <svg viewBox="0 0 32 32" width="28" height="28" aria-hidden="true">
        <rect width="32" height="32" rx="8" fill="currentColor" />
        <path d="M8 22 L14 12 L20 18 L25 9" fill="none" stroke="var(--superficie)" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" />
        <circle cx="8" cy="22" r="2.6" fill="var(--superficie)" />
        <circle cx="25" cy="9" r="2.6" fill="var(--superficie)" />
      </svg>
      <span className="marca__texto">RutaSV</span>
    </span>
  );
}

function Usuario({ compacto = false }: { compacto?: boolean }) {
  const { sesion, cerrarSesion } = useSesion();
  const navegar = useNavigate();
  if (!sesion) return null;
  const inicial = sesion.usuario.slice(0, 1).toUpperCase();
  return (
    <div className="usuario">
      <span className="usuario__avatar" aria-hidden="true">
        {inicial}
      </span>
      <span className="usuario__datos">
        <span className="usuario__nombre">{sesion.usuario}</span>
        <span className="insignia insignia--rol">{sesion.rol === 'ADMIN' ? 'Admin' : 'Pasajero'}</span>
      </span>
      <button
        type="button"
        className="boton boton--chico"
        onClick={async () => {
          await cerrarSesion();
          navegar('/login', { replace: true });
        }}
      >
        {compacto ? 'Salir' : 'Cerrar sesión'}
      </button>
    </div>
  );
}

function Enlace({ e, alHacerClic }: { e: Entrada; alHacerClic?: () => void }) {
  return (
    <NavLink to={e.a} className={({ isActive }) => `menu__enlace ${isActive ? 'es-activo' : ''}`} onClick={alHacerClic}>
      {e.icono}
      <span>{e.texto}</span>
    </NavLink>
  );
}

export function Ventana() {
  const { esAdmin } = useSesion();
  return esAdmin ? <VentanaAdmin /> : <VentanaPasajero />;
}

function VentanaAdmin() {
  const [abierto, setAbierto] = useState(false);
  const ubicacion = useLocation();
  useEffect(() => setAbierto(false), [ubicacion.pathname]);

  useEffect(() => {
    if (!abierto) return;
    const alTeclear = (ev: KeyboardEvent) => ev.key === 'Escape' && setAbierto(false);
    window.addEventListener('keydown', alTeclear);
    return () => window.removeEventListener('keydown', alTeclear);
  }, [abierto]);

  return (
    <div className="ventana ventana--admin">
      <a className="saltar" href="#contenido">
        Saltar al contenido
      </a>
      <aside id="menu-lateral" className={`lateral ${abierto ? 'es-abierto' : ''}`} aria-label="Menú principal">
        <div className="lateral__marca">
          <Marca />
        </div>
        <nav className="menu">
          <p className="menu__grupo">Administración</p>
          {ENTRADAS_ADMIN.map((e) => (
            <Enlace key={e.a} e={e} />
          ))}
          <p className="menu__grupo">Vista de pasajero</p>
          {ENTRADAS_PASAJERO.map((e) => (
            <Enlace key={e.a} e={e} />
          ))}
        </nav>
        <p className="lateral__pie texto-suave">PED941 · UDB</p>
      </aside>
      {abierto && <div className="velo" onClick={() => setAbierto(false)} aria-hidden="true" />}

      <div className="ventana__principal">
        <header className="encabezado">
          <button
            type="button"
            className="boton boton--chico encabezado__menu"
            aria-expanded={abierto}
            aria-controls="menu-lateral"
            onClick={() => setAbierto((v) => !v)}
          >
            <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
              <path d="M4 6h16M4 12h16M4 18h16" />
            </svg>
            Menú
          </button>
          <span className="encabezado__titulo">Panel de administración</span>
          <Usuario />
        </header>
        <main id="contenido" className="contenido" tabIndex={-1}>
          <Outlet />
        </main>
      </div>
    </div>
  );
}

function VentanaPasajero() {
  return (
    <div className="ventana ventana--pasajero">
      <a className="saltar" href="#contenido">
        Saltar al contenido
      </a>
      <header className="encabezado encabezado--pasajero">
        <Marca />
        <Usuario compacto />
      </header>
      <main id="contenido" className="contenido contenido--pasajero" tabIndex={-1}>
        <Outlet />
      </main>
      <nav className="pestanas" aria-label="Navegación principal">
        {ENTRADAS_PASAJERO.map((e) => (
          <NavLink key={e.a} to={e.a} className={({ isActive }) => `pestanas__enlace ${isActive ? 'es-activo' : ''}`}>
            {e.icono}
            <span>{e.corto}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
