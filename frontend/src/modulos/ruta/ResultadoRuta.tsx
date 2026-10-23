// Resultado de ruta (Fig. 6). Rol dueno: Ruta.
// Endpoints: GET /rutas?origen=E01&destino=E04 (ruta con estaciones y tramos), GET /red (mapa de fondo).
// Lee origen y destino de la URL; si faltan, vuelve a la ultima ruta vista o invita a buscar una.
// Jerarquia: el tiempo total manda; luego el itinerario como diagrama de linea; luego el mapa.
import { Navigate, useSearchParams } from 'react-router-dom';
import { api, ApiError } from '../../api/cliente';
import type { Red, Ruta } from '../../api/tipos';
import { km, nsAMs } from '../../comun/formato';
import { MapaRed } from '../../comun/MapaRed';
import { Aviso, BotonEnlace, CodigoEstacion, Esqueleto, EstadoVacio, Pagina, Tarjeta } from '../../comun/ui';
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
      <Pagina angosta figura="Fig. 6" titulo="Resultado de ruta">
        <EstadoVacio
          icono="ruta"
          titulo="Aún no calculas una ruta"
          accion={
            <BotonEnlace a="/buscar" variante="primario" icono="buscar">
              Buscar una ruta
            </BotonEnlace>
          }
        >
          Elige un origen y un destino; aquí verás el tiempo de viaje y cada parada del recorrido.
        </EstadoVacio>
      </Pagina>
    );
  }
  ultimaConsulta = params.toString();
  return <Detalle origen={origen} destino={destino} />;
}

const volver = (
  <BotonEnlace a="/buscar" icono="atras" tamano="sm">
    Nueva búsqueda
  </BotonEnlace>
);

function Detalle({ origen, destino }: { origen: string; destino: string }) {
  const ruta = usePedido(() => api.get<Ruta>('/rutas', { origen, destino }), [origen, destino]);
  const red = usePedido(() => api.get<Red>('/red'), []);

  if (ruta.cargando) {
    return (
      <Pagina figura="Fig. 6" titulo="Calculando tu ruta…" acciones={volver}>
        <div className="tarjeta">
          <Esqueleto lineas={4} etiqueta="Calculando la ruta más corta…" />
        </div>
      </Pagina>
    );
  }

  if (ruta.error || !ruta.datos) {
    const e = ruta.error;
    const sinRuta = e instanceof ApiError && e.status === 404;
    return (
      <Pagina figura="Fig. 6" titulo="Resultado de ruta" acciones={volver}>
        <Aviso tono={sinRuta ? 'aviso' : 'error'} titulo={sinRuta ? 'No hay ruta entre estas estaciones' : 'No se pudo calcular la ruta'}>
          <p>
            {sinRuta
              ? 'Ningún tramo activo conecta el origen con el destino (RN-6). Prueba con otra estación cercana.'
              : e?.message ?? 'Inténtalo de nuevo en un momento.'}
          </p>
          <p className="ruta__error-detalle">
            Origen <CodigoEstacion codigo={origen} /> · destino <CodigoEstacion codigo={destino} />
          </p>
        </Aviso>
      </Pagina>
    );
  }

  const r = ruta.datos;
  const primera = r.estaciones[0];
  const ultima = r.estaciones[r.estaciones.length - 1];
  const nombreOrigen = primera?.nombre ?? r.origen;
  const nombreDestino = ultima?.nombre ?? r.destino;
  const paradasIntermedias = Math.max(r.estaciones.length - 2, 0);

  // Minuto acumulado al llegar a cada parada (como en un horario de linea).
  const acumulado: number[] = [0];
  r.tramos.forEach((t, i) => acumulado.push(acumulado[i] + t.minutos));

  return (
    <div className="pagina">
      <div className="ruta__barra">
        {volver}
        <span className="pagina__figura" title="Figura del documento del proyecto">
          Fig. 6
        </span>
      </div>

      <section className="tablero" aria-labelledby="ruta-titulo">
        <p className="tablero__tiempo" data-prueba="minutos">
          <span className="tablero__cifra">{r.minutos}</span>
          <span className="tablero__unidad">min</span>
        </p>
        <div className="tablero__trayecto">
          <h1 id="ruta-titulo" className="tablero__ruta">
            <span className="tablero__estacion">{nombreOrigen}</span>
            <span className="oculto-visual"> a </span>
            <span className="tablero__estacion tablero__estacion--destino">{nombreDestino}</span>
          </h1>
          <ul className="tablero__datos lista-limpia" aria-label="Resumen del viaje">
            <li>
              <span className="cifra">{km(r.km)}</span>
            </li>
            <li>
              <span className="cifra">{r.tramos.length}</span> {r.tramos.length === 1 ? 'tramo' : 'tramos'}
            </li>
            <li>
              <span className="cifra">{paradasIntermedias}</span> {paradasIntermedias === 1 ? 'parada intermedia' : 'paradas intermedias'}
            </li>
          </ul>
        </div>
        <p className="tablero__nota">
          Ruta más corta por tiempo de viaje (Dijkstra) · calculada en <span className="cifra">{nsAMs(r.tiempoCalculoNs)}</span>
        </p>
      </section>

      <div className="rejilla-2 rejilla-2--invertida ruta__cuerpo">
        <Tarjeta titulo="Itinerario" detalle={`${r.estaciones.length} paradas`}>
          <ol className="linea">
            {r.estaciones.map((e, i) => {
              const t = r.tramos[i];
              const esPrimera = i === 0;
              const esUltima = i === r.estaciones.length - 1;
              return (
                <li
                  key={`${e.codigo}-${i}`}
                  className={`linea__parada${esPrimera || esUltima ? ' es-extremo' : ''}`}
                  style={{ ['--i' as string]: i }}
                >
                  <span className="linea__punto" aria-hidden="true" />
                  <div className="linea__estacion">
                    <span className="linea__nombre">{e.nombre}</span>
                    <span className="linea__meta">
                      <CodigoEstacion codigo={e.codigo} /> {e.zona}
                      {esPrimera && <span className="linea__rotulo">Salida</span>}
                      {esUltima && <span className="linea__rotulo">Llegada</span>}
                    </span>
                  </div>
                  <span className="linea__acumulado" aria-label={esPrimera ? 'minuto 0' : `minuto ${acumulado[i]} del viaje`}>
                    {esPrimera ? '0' : `+${acumulado[i]}`}
                    <small>min</small>
                  </span>
                  {t && !esUltima && (
                    <p className="linea__tramo">
                      <span className="linea__tramo-min">{t.minutos} min</span>
                      <span className="linea__tramo-km">{km(t.km)}</span>
                    </p>
                  )}
                </li>
              );
            })}
          </ol>
        </Tarjeta>

        <Tarjeta titulo="Mapa">
          {red.datos ? (
            <MapaRed
              estaciones={red.datos.estaciones}
              tramos={red.datos.tramos}
              ruta={r.estaciones.map((e) => e.codigo)}
              atenuarRed
              titulo={`Ruta de ${nombreOrigen} a ${nombreDestino} sobre la red vial`}
            />
          ) : red.error ? (
            <Aviso tono="error">No se pudo cargar el mapa. {red.error.message}</Aviso>
          ) : (
            <Esqueleto alto={260} etiqueta="Cargando mapa…" />
          )}
        </Tarjeta>
      </div>
    </div>
  );
}
