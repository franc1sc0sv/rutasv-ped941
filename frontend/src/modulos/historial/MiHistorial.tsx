// Mi historial (Fig. 7). Rol dueno: Historial.
// Endpoints: GET /historial ([RutaCalculada], hoy responde 501 pendiente), GET /estaciones (nombres).
// Para conectarla: implementa miHistorial en el core; esta pantalla ya pinta la lista sola.
import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/cliente';
import type { RutaCalculada } from '../../api/tipos';
import { fecha, km, nsAMs } from '../../comun/formato';
import { Aviso, BotonEnlace, Esqueleto, EstadoVacio, Pagina, Pendiente } from '../../comun/ui';
import { usePedido } from '../../comun/usePedido';
import { useEstaciones } from '../../comun/useEstaciones';
import { urlRuta, type EstadoRuta } from '../ruta/estadoRuta';
import './historial.css';

export function MiHistorial() {
  const historial = usePedido(() => api.get<RutaCalculada[]>('/historial'), []);
  const { porCodigo } = useEstaciones();
  const nombre = (c: string) => porCodigo.get(c)?.nombre ?? c;

  let cuerpo: ReactNode;
  if (historial.cargando) {
    cuerpo = (
      <div className="tarjeta">
        <Esqueleto lineas={4} etiqueta="Cargando tu historial…" />
      </div>
    );
  } else if (historial.error?.pendiente) {
    cuerpo = <Pendiente rol="Historial" mensaje={historial.error.message} endpoint="GET /api/historial" />;
  } else if (historial.error) {
    cuerpo = <Aviso tono="error" titulo="No se pudo cargar tu historial">{historial.error.message}</Aviso>;
  } else {
    // Mas reciente primero, aunque el backend ya las mande en otro orden.
    const lista = [...(historial.datos ?? [])].sort((a, b) => b.fecha.localeCompare(a.fecha));
    cuerpo =
      lista.length === 0 ? (
        <EstadoVacio
          icono="historial"
          titulo="Aún no calculas rutas"
          accion={
            <BotonEnlace a="/buscar" variante="primario" icono="buscar">
              Buscar una ruta
            </BotonEnlace>
          }
        >
          Cada ruta que calcules quedará aquí para volver a verla con un toque.
        </EstadoVacio>
      ) : (
        <ul className="lista-limpia viajes">
          {lista.map((r, i) => (
            <li key={`${r.fecha}-${i}`}>
              {/* Pasa la entrada en el state: Resultado de ruta la pinta sin recalcular (no la duplica en el historial). */}
              <Link className="viaje" to={urlRuta(r.origen, r.destino)} state={{ entrada: r } satisfies EstadoRuta}>
                <span className="viaje__min">
                  <span className="cifra">{r.minutos}</span>
                  <small>min</small>
                </span>
                <span className="viaje__trayecto">
                  <span className="viaje__estacion">{nombre(r.origen)}</span>
                  <span className="oculto-visual"> a </span>
                  <span className="viaje__estacion viaje__estacion--destino">{nombre(r.destino)}</span>
                </span>
                <span className="viaje__datos">
                  <time dateTime={r.fecha}>{fecha(r.fecha)}</time>
                  <span>{km(r.km)}</span>
                  <span>cálculo {nsAMs(r.tiempoCalculoNs)}</span>
                </span>
              </Link>
            </li>
          ))}
        </ul>
      );
  }

  return (
    <Pagina angosta figura="Fig. 7" titulo="Mi historial" descripcion="Tus rutas calculadas, de la más reciente a la más antigua.">
      {cuerpo}
    </Pagina>
  );
}
