// Resultado de ruta (Fig. 6). Rol dueno: Ruta.
// Endpoints: GET /rutas?origen=E01&destino=E04 (ruta con estaciones y tramos), GET /red (mapa de fondo).
// Lee origen y destino de la URL; si faltan, vuelve a la ultima ruta vista o invita a buscar una.
import { Link, Navigate, useSearchParams } from 'react-router-dom';
import { api, ApiError } from '../../api/cliente';
import type { Red, Ruta } from '../../api/tipos';
import { Cargando } from '../../comun/Cargando';
import { km, nsAMs } from '../../comun/formato';
import { MapaRed } from '../../comun/MapaRed';
import { MensajeError } from '../../comun/MensajeError';
import { Pagina } from '../../comun/Pagina';
import { usePedido } from '../../comun/usePedido';
import './ruta.css';

// Ultima consulta vista en esta pestana: la pestana "Ruta" del menu vuelve a ella.
let ultimaConsulta: string | null = null;

export function ResultadoRuta() {
  const [params] = useSearchParams();
  const origen = params.get('origen');
  const destino = params.get('destino');

  if (!origen || !destino) {
    if (ultimaConsulta) return <Navigate to={`/ruta?${ultimaConsulta}`} replace />;
    return (
      <Pagina figura="Fig. 6 · Ruta" titulo="Resultado de ruta">
        <div className="tarjeta vacio">
          <p>Todavía no has calculado una ruta.</p>
          <Link className="boton boton--primario" to="/buscar">
            Buscar una ruta
          </Link>
        </div>
      </Pagina>
    );
  }
  ultimaConsulta = params.toString();
  return <Detalle origen={origen} destino={destino} />;
}

function Detalle({ origen, destino }: { origen: string; destino: string }) {
  const ruta = usePedido(() => api.get<Ruta>('/rutas', { origen, destino }), [origen, destino]);
  const red = usePedido(() => api.get<Red>('/red'), []);

  const volver = (
    <Link className="boton" to="/buscar">
      ← Nueva búsqueda
    </Link>
  );

  if (ruta.cargando) {
    return (
      <Pagina figura="Fig. 6 · Ruta" titulo="Resultado de ruta" acciones={volver}>
        <Cargando texto="Calculando la ruta más corta…" />
      </Pagina>
    );
  }

  if (ruta.error || !ruta.datos) {
    const e = ruta.error;
    const mensaje =
      e instanceof ApiError && e.status === 404 ? 'No existe ruta entre estas estaciones (RN-6).' : e?.message ?? 'No se pudo calcular la ruta.';
    return (
      <Pagina figura="Fig. 6 · Ruta" titulo="Resultado de ruta" acciones={volver}>
        <MensajeError mensaje={mensaje} tono={e?.status === 404 ? 'aviso' : 'error'}>
          <p className="ruta__error-detalle">
            Origen <code>{origen}</code> → destino <code>{destino}</code>.{' '}
            {e?.status === 404 && 'Las estaciones no están conectadas por ningún tramo activo.'}
          </p>
        </MensajeError>
      </Pagina>
    );
  }

  const r = ruta.datos;
  const primera = r.estaciones[0];
  const ultima = r.estaciones[r.estaciones.length - 1];

  return (
    <Pagina
      figura="Fig. 6 · Ruta"
      titulo={`${primera?.nombre ?? r.origen} → ${ultima?.nombre ?? r.destino}`}
      descripcion="Ruta más corta por tiempo de viaje (Dijkstra)."
      acciones={volver}
    >
      <section className="kpis" aria-label="Resumen de la ruta">
        <div className="kpi kpi--acento">
          <div className="kpi__etiqueta">Tiempo total</div>
          <div className="kpi__valor" data-prueba="minutos">
            {r.minutos}
            <small>min</small>
          </div>
        </div>
        <div className="kpi">
          <div className="kpi__etiqueta">Distancia</div>
          <div className="kpi__valor">{km(r.km)}</div>
        </div>
        <div className="kpi">
          <div className="kpi__etiqueta">Tramos</div>
          <div className="kpi__valor">{r.tramos.length}</div>
        </div>
        <div className="kpi">
          <div className="kpi__etiqueta">Tiempo de cálculo</div>
          <div className="kpi__valor kpi__valor--chico">{nsAMs(r.tiempoCalculoNs)}</div>
        </div>
      </section>

      <div className="rejilla-2">
        <section className="tarjeta tarjeta--relleno" aria-labelledby="mapa-titulo">
          <h2 id="mapa-titulo">Mapa</h2>
          {red.datos ? (
            <MapaRed
              estaciones={red.datos.estaciones}
              tramos={red.datos.tramos}
              ruta={r.estaciones.map((e) => e.codigo)}
              atenuarRed
              titulo={`Ruta de ${primera?.nombre} a ${ultima?.nombre} sobre la red vial`}
            />
          ) : red.error ? (
            <MensajeError mensaje={`No se pudo cargar la red: ${red.error.message}`} />
          ) : (
            <Cargando texto="Cargando mapa…" />
          )}
        </section>

        <section className="tarjeta tarjeta--relleno" aria-labelledby="itinerario-titulo">
          <h2 id="itinerario-titulo">Itinerario</h2>
          <ol className="itinerario">
            {r.estaciones.map((e, i) => {
              const t = r.tramos[i];
              const extremo = i === 0 || i === r.estaciones.length - 1;
              return (
                <li key={`${e.codigo}-${i}`} className={`itinerario__paso ${extremo ? 'es-extremo' : ''}`}>
                  <span className="itinerario__punto" aria-hidden="true" />
                  <div className="itinerario__estacion">
                    <strong>{e.nombre}</strong>
                    <span className="texto-suave">
                      {e.codigo} · {e.zona}
                      {i === 0 && ' · salida'}
                      {i === r.estaciones.length - 1 && ' · llegada'}
                    </span>
                  </div>
                  {t && i < r.estaciones.length - 1 && (
                    <div className="itinerario__tramo">
                      <span>{t.minutos} min</span>
                      <span className="texto-suave">{km(t.km)}</span>
                    </div>
                  )}
                </li>
              );
            })}
          </ol>
        </section>
      </div>
    </Pagina>
  );
}
