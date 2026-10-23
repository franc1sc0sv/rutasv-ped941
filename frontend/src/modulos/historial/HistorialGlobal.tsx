// Historial global (Fig. 11). Rol dueno: Historial. Solo ADMIN.
// Endpoint: GET /estadisticas -> { totalBusquedas, rutaMasConsultada, tiempoPromedioNs, top5 } (hoy 501 pendiente).
import type { ReactNode } from 'react';
import { api } from '../../api/cliente';
import type { Estadisticas } from '../../api/tipos';
import { Cargando } from '../../comun/Cargando';
import { nsAMs } from '../../comun/formato';
import { MensajeError } from '../../comun/MensajeError';
import { Pagina } from '../../comun/Pagina';
import { Pendiente } from '../../comun/Pendiente';
import { usePedido } from '../../comun/usePedido';
import './historial.css';

export function HistorialGlobal() {
  const est = usePedido(() => api.get<Estadisticas>('/estadisticas'), []);

  let cuerpo: ReactNode;
  if (est.cargando) cuerpo = <Cargando />;
  else if (est.error?.pendiente) cuerpo = <Pendiente rol="Historial" mensaje={est.error.message} endpoint="GET /api/estadisticas" />;
  else if (est.error || !est.datos) cuerpo = <MensajeError mensaje={est.error?.message ?? 'Sin datos'} />;
  else {
    const d = est.datos;
    cuerpo = (
      <>
        <section className="kpis" aria-label="Resumen">
          <div className="kpi kpi--acento">
            <div className="kpi__etiqueta">Total de búsquedas</div>
            <div className="kpi__valor">{d.totalBusquedas}</div>
          </div>
          <div className="kpi">
            <div className="kpi__etiqueta">Ruta más consultada</div>
            <div className="kpi__valor kpi__valor--texto">{d.rutaMasConsultada || '—'}</div>
          </div>
          <div className="kpi">
            <div className="kpi__etiqueta">Tiempo promedio de cálculo</div>
            <div className="kpi__valor">{nsAMs(d.tiempoPromedioNs)}</div>
          </div>
        </section>
        <section className="tarjeta tarjeta--relleno" aria-labelledby="top5-titulo">
          <h2 id="top5-titulo">Top 5 de rutas</h2>
          {d.top5.length === 0 ? (
            <p className="texto-suave">Todavía no hay rutas consultadas.</p>
          ) : (
            <ol className="top5">
              {d.top5.map((r, i) => (
                <li key={`${r}-${i}`}>
                  <span className="top5__pos">{i + 1}</span>
                  <span>{r}</span>
                </li>
              ))}
            </ol>
          )}
        </section>
      </>
    );
  }

  return (
    <Pagina
      figura="Fig. 11 · Historial"
      titulo="Historial global"
      descripcion="Estadísticas de todas las búsquedas del sistema."
      acciones={
        <button type="button" className="boton" onClick={est.recargar}>
          Actualizar
        </button>
      }
    >
      {cuerpo}
    </Pagina>
  );
}
