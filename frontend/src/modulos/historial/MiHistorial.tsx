// Mi historial (Fig. 7). Rol dueno: Historial.
// Endpoints: GET /historial ([RutaCalculada], hoy responde 501 pendiente), GET /estaciones (nombres).
// Para conectarla: implementa miHistorial en el core; esta pantalla ya pinta la lista sola.
import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/cliente';
import type { RutaCalculada } from '../../api/tipos';
import { Cargando } from '../../comun/Cargando';
import { fecha, km, nsAMs } from '../../comun/formato';
import { MensajeError } from '../../comun/MensajeError';
import { Pagina } from '../../comun/Pagina';
import { Pendiente } from '../../comun/Pendiente';
import { usePedido } from '../../comun/usePedido';
import { useEstaciones } from '../../comun/useEstaciones';
import './historial.css';

export function MiHistorial() {
  const historial = usePedido(() => api.get<RutaCalculada[]>('/historial'), []);
  const { porCodigo } = useEstaciones();
  const nombre = (c: string) => porCodigo.get(c)?.nombre ?? c;

  let cuerpo: ReactNode;
  if (historial.cargando) {
    cuerpo = <Cargando />;
  } else if (historial.error?.pendiente) {
    cuerpo = <Pendiente rol="Historial" mensaje={historial.error.message} endpoint="GET /api/historial" />;
  } else if (historial.error) {
    cuerpo = <MensajeError mensaje={historial.error.message} />;
  } else {
    // Mas reciente primero, aunque el backend ya las mande en otro orden.
    const lista = [...(historial.datos ?? [])].sort((a, b) => b.fecha.localeCompare(a.fecha));
    cuerpo =
      lista.length === 0 ? (
        <div className="tarjeta vacio">
          <p>Aún no has calculado rutas.</p>
          <Link className="boton boton--primario" to="/buscar">
            Buscar una ruta
          </Link>
        </div>
      ) : (
        <ul className="lista-limpia historial">
          {lista.map((r, i) => (
            <li key={`${r.fecha}-${i}`} className="tarjeta historial__item">
              <Link className="historial__enlace" to={`/ruta?origen=${encodeURIComponent(r.origen)}&destino=${encodeURIComponent(r.destino)}`}>
                <div className="historial__trayecto">
                  <strong>{nombre(r.origen)}</strong>
                  <span aria-hidden="true">→</span>
                  <span className="oculto-visual">a</span>
                  <strong>{nombre(r.destino)}</strong>
                </div>
                <div className="historial__datos texto-suave">
                  <span>{fecha(r.fecha)}</span>
                  <span>{km(r.km)}</span>
                  <span>cálculo {nsAMs(r.tiempoCalculoNs)}</span>
                </div>
                <span className="historial__min">
                  {r.minutos}
                  <small>min</small>
                </span>
              </Link>
            </li>
          ))}
        </ul>
      );
  }

  return (
    <Pagina figura="Fig. 7 · Historial" titulo="Mi historial" descripcion="Tus rutas calculadas, de la más reciente a la más antigua.">
      {cuerpo}
    </Pagina>
  );
}
