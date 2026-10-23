// Ventana base. El modo depende del rol (RN-1):
// - ADMIN: menu lateral oscuro tipo diagrama de linea (en telefono se abre con el boton "Menú").
// - PASAJERO: encabezado claro y barra de pestanas abajo (en escritorio las pestanas pasan al encabezado).
import { useEffect, useRef, useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { Boton, Icono } from '../comun/ui';
import { useSesion } from '../sesion/SesionContext';
import { Marca } from './Marca';
import { ENTRADAS_ADMIN, ENTRADAS_PASAJERO, type Entrada } from './navegacion';
import './ventana.css';

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
        <span className="usuario__rol">{sesion.rol === 'ADMIN' ? 'Administración' : 'Pasajero'}</span>
      </span>
      <Boton
        tamano="sm"
        variante="fantasma"
        icono="salir"
        soloIcono={compacto}
        aria-label={compacto ? 'Cerrar sesión' : undefined}
        title="Cerrar sesión"
        onClick={async () => {
          await cerrarSesion();
          navegar('/login', { replace: true });
        }}
      >
        {compacto ? null : 'Salir'}
      </Boton>
    </div>
  );
}

/** Enlace del menu lateral: una estacion sobre la linea. */
function Estacion({ e }: { e: Entrada }) {
  return (
    <NavLink to={e.a} className={({ isActive }) => `linea-menu__enlace${isActive ? ' es-activo' : ''}`}>
      <span className="linea-menu__punto" aria-hidden="true" />
      <span>{e.texto}</span>
    </NavLink>
  );
}

export function Ventana() {
  const { esAdmin } = useSesion();
  return esAdmin ? <VentanaAdmin /> : <VentanaPasajero />;
}

function SaltarAlContenido() {
  return (
    <a className="saltar" href="#contenido">
      Saltar al contenido
    </a>
  );
}

function VentanaAdmin() {
  const [abierto, setAbierto] = useState(false);
  const ubicacion = useLocation();
  const refBoton = useRef<HTMLButtonElement>(null);
  const refMenu = useRef<HTMLElement>(null);

  // Con el menu abierto en telefono, el resto de la pagina queda inerte: el foco no se escapa detras del velo.
  useEffect(() => {
    const hermanos = refMenu.current?.parentElement?.querySelectorAll<HTMLElement>(':scope > :not(#menu-lateral)') ?? [];
    hermanos.forEach((el) => (abierto ? el.setAttribute('inert', '') : el.removeAttribute('inert')));
  }, [abierto]);
  // El encabezado dice en que lado de la app estas; el nombre de la pantalla ya esta en su h1.
  const contexto = ubicacion.pathname.startsWith('/admin') ? 'Panel de administración' : 'Vista de pasajero';

  useEffect(() => setAbierto(false), [ubicacion.pathname]);

  useEffect(() => {
    if (!abierto) return;
    // Al abrir en telefono, el foco entra al menu; Escape lo cierra y devuelve el foco al boton.
    refMenu.current?.querySelector<HTMLElement>('a')?.focus();
    const alTeclear = (ev: KeyboardEvent) => {
      if (ev.key === 'Escape') {
        setAbierto(false);
        window.setTimeout(() => refBoton.current?.focus(), 0);
      }
    };
    window.addEventListener('keydown', alTeclear);
    return () => window.removeEventListener('keydown', alTeclear);
  }, [abierto]);

  return (
    <div className="ventana ventana--admin">
      <SaltarAlContenido />
      <aside ref={refMenu} id="menu-lateral" className={`lateral${abierto ? ' es-abierto' : ''}`} aria-label="Menú principal">
        <div className="lateral__cabeza">
          <Marca className="marca--invertida" />
          <Boton
            soloIcono
            variante="fantasma"
            className="lateral__cerrar"
            aria-label="Cerrar menú"
            onClick={() => {
              setAbierto(false);
              window.setTimeout(() => refBoton.current?.focus(), 0);
            }}
          >
            <Icono nombre="cerrar" />
          </Boton>
        </div>
        <nav className="lateral__nav" aria-label="Secciones">
          <p className="linea-menu__grupo" id="grupo-admin">
            Administración
          </p>
          <ul className="linea-menu" aria-labelledby="grupo-admin">
            {ENTRADAS_ADMIN.map((e) => (
              <li key={e.a}>
                <Estacion e={e} />
              </li>
            ))}
          </ul>
          <p className="linea-menu__grupo" id="grupo-pasajero">
            Vista de pasajero
          </p>
          <ul className="linea-menu" aria-labelledby="grupo-pasajero">
            {ENTRADAS_PASAJERO.map((e) => (
              <li key={e.a}>
                <Estacion e={e} />
              </li>
            ))}
          </ul>
        </nav>
        <p className="lateral__pie">PED941 · Universidad Don Bosco</p>
      </aside>
      <div className={`velo${abierto ? ' es-visible' : ''}`} onClick={() => setAbierto(false)} aria-hidden="true" />

      <div className="ventana__principal">
        <header className="encabezado">
          <Boton
            ref={refBoton}
            tamano="sm"
            icono="menu"
            className="encabezado__menu"
            aria-expanded={abierto}
            aria-controls="menu-lateral"
            onClick={() => setAbierto((v) => !v)}
          >
            Menú
          </Boton>
          <span className="encabezado__titulo">{contexto}</span>
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
      <SaltarAlContenido />
      <header className="encabezado encabezado--pasajero">
        <Marca />
        <nav className="pestanas-arriba" aria-label="Navegación principal">
          {ENTRADAS_PASAJERO.map((e) => (
            <NavLink key={e.a} to={e.a} className={({ isActive }) => `pestanas-arriba__enlace${isActive ? ' es-activo' : ''}`}>
              {e.texto}
            </NavLink>
          ))}
        </nav>
        <Usuario compacto />
      </header>
      <main id="contenido" className="contenido contenido--pasajero" tabIndex={-1}>
        <Outlet />
      </main>
      <nav className="pestanas" aria-label="Navegación principal (móvil)">
        {ENTRADAS_PASAJERO.map((e) => (
          <NavLink key={e.a} to={e.a} className={({ isActive }) => `pestanas__enlace${isActive ? ' es-activo' : ''}`}>
            <Icono nombre={e.icono} tamano={22} />
            <span>{e.corto}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
