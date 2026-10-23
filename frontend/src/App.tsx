// Mapa de rutas de la app. Pasajero: /buscar, /ruta, /historial. Admin: todo lo anterior + /admin/*.
import { Navigate, Route, Routes } from 'react-router-dom';
import { Ventana } from './layout/Ventana';
import { BuscarRuta } from './modulos/busqueda/BuscarRuta';
import { HistorialGlobal } from './modulos/historial/HistorialGlobal';
import { MiHistorial } from './modulos/historial/MiHistorial';
import { RedVial } from './modulos/historial/RedVial';
import { RegistroParadas } from './modulos/paradas/RegistroParadas';
import { ResultadoRuta } from './modulos/ruta/ResultadoRuta';
import { Despacho } from './modulos/terminal/Despacho';
import { RequiereAdmin, RequiereSesion, SoloInvitado } from './sesion/Guardas';
import { Login } from './sesion/Login';
import { inicioPara, useSesion } from './sesion/SesionContext';

function Inicio() {
  const { sesion } = useSesion();
  return <Navigate to={inicioPara(sesion)} replace />;
}

export function App() {
  return (
    <Routes>
      <Route
        path="/login"
        element={
          <SoloInvitado>
            <Login />
          </SoloInvitado>
        }
      />
      <Route element={<RequiereSesion />}>
        <Route element={<Ventana />}>
          <Route path="/buscar" element={<BuscarRuta />} />
          <Route path="/ruta" element={<ResultadoRuta />} />
          <Route path="/historial" element={<MiHistorial />} />
          <Route path="/admin" element={<RequiereAdmin />}>
            <Route index element={<Navigate to="red" replace />} />
            <Route path="red" element={<RedVial />} />
            <Route path="paradas" element={<RegistroParadas />} />
            <Route path="despacho" element={<Despacho />} />
            <Route path="historial-global" element={<HistorialGlobal />} />
          </Route>
        </Route>
      </Route>
      <Route path="*" element={<Inicio />} />
    </Routes>
  );
}
