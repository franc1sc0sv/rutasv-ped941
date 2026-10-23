// Guardas de ruta: RequiereSesion (cualquier usuario) y RequiereAdmin (RN-1).
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { Cargando } from '../comun/ui';
import { inicioPara, useSesion } from './SesionContext';

export function RequiereSesion() {
  const { sesion, verificando } = useSesion();
  const ubicacion = useLocation();
  if (verificando) return <Cargando texto="Verificando sesión…" pantallaCompleta />;
  if (!sesion) return <Navigate to="/login" replace state={{ desde: ubicacion.pathname + ubicacion.search }} />;
  return <Outlet />;
}

export function RequiereAdmin() {
  const { sesion } = useSesion();
  if (sesion?.rol !== 'ADMIN') return <Navigate to={inicioPara(sesion)} replace />;
  return <Outlet />;
}

/** Para /login: si ya hay sesion, manda a la pantalla de inicio de su rol. */
export function SoloInvitado({ children }: { children: JSX.Element }) {
  const { sesion, verificando } = useSesion();
  if (verificando) return <Cargando texto="Verificando sesión…" pantallaCompleta />;
  if (sesion) return <Navigate to={inicioPara(sesion)} replace />;
  return children;
}
