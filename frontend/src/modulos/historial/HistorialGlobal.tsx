// Historial global (Fig. 11). Rol dueno: Historial. Solo ADMIN.
// Endpoint: GET /estadisticas -> { totalBusquedas, rutaMasConsultada, tiempoPromedioNs, top5 } (hoy 501 pendiente).
import type { ReactNode } from 'react';
import { api } from '../../api/cliente';
import type { Estadisticas } from '../../api/tipos';
import { nsAMs } from '../../comun/formato';
import { Aviso, Boton, Esqueleto, EstadoVacio, Pagina, Pendiente, Tarjeta } from '../../comun/ui';
import { usePedido } from '../../comun/usePedido';
import './historial.css';

export function HistorialGlobal() {
  const est = usePedido(() => api.get<Estadisticas>('/estadisticas'), []);

  let cuerpo: ReactNode;
  if (est.cargando) {
    cuerpo = (
      <div className="tarjeta">
        <Esqueleto lineas={3} etiqueta="Cargando estadísticas…" />
      </div>
    );
  } else if (est.error?.pendiente) {
    cuerpo = <Pendiente rol="Historial" mensaje={est.error.message} endpoint="GET /api/estadisticas" />;
  } else if (est.error || !est.datos) {
    cuerpo = <Aviso tono="error" titulo="No se pudieron cargar las estadísticas">{est.error?.message ?? 'El servidor no envió datos.'}</Aviso>;
  } else {
    const d = est.datos;
    cuerpo = (
      <>
        <dl className="cifras" aria-label="Resumen">
          <div className="cifras__item cifras__item--principal">
            <dt>Búsquedas en total</dt>
            <dd className="cifra">{d.totalBusquedas.toLocaleString('es-SV')}</dd>
          </div>
          <div className="cifras__item">
            <dt>Ruta más consultada</dt>
            <dd className="cifras__texto">{d.rutaMasConsultada || 'Sin datos'}</dd>
          </div>
          <div className="cifras__item">
            <dt>Tiempo promedio de cálculo</dt>
            <dd className="cifra">{nsAMs(d.tiempoPromedioNs)}</dd>
          </div>
        </dl>
        <Tarjeta titulo="Las 5 rutas más consultadas">
          {d.top5.length === 0 ? (
            <EstadoVacio compacto icono="estadisticas" titulo="Todavía no hay rutas consultadas" />
          ) : (
            <ol className="top5">
              {d.top5.map((r, i) => (
                <li key={`${r}-${i}`}>
                  <span className="top5__pos cifra">{i + 1}</span>
                  <span>{r}</span>
                </li>
              ))}
            </ol>
          )}
        </Tarjeta>
      </>
    );
  }

  return (
    <Pagina
      figura="Fig. 11"
      titulo="Historial global"
      descripcion="Estadísticas de todas las búsquedas del sistema."
      acciones={
        <Boton icono="recargar" onClick={est.recargar}>
          Actualizar
        </Boton>
      }
    >
      {cuerpo}
    </Pagina>
  );
}
