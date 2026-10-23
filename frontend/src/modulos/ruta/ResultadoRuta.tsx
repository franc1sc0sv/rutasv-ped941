// Resultado de ruta (Fig. 6). Rol dueno: Ruta.
// Endpoints: GET /red (mapa y detalle; no registra nada). GET /rutas solo si el usuario lo pide.
// Esta pantalla nunca recalcula por su cuenta: con RN-7 cada calculo queda en el historial, y ver
// una ruta no es buscarla otra vez. La ruta llega en el state de la navegacion (ver estadoRuta.ts):
//   - desde Buscar ruta: la ruta recien calculada;
//   - desde Mi historial: la entrada guardada, completada con GET /red;
//   - URL pegada a mano: boton "Calcular esta ruta";
//   - sin parametros (pestana Ruta): la ultima entrada de GET /historial del usuario, o el estado vacio.
// Jerarquia: el tiempo total manda; luego el itinerario como diagrama de linea; luego el mapa.
import { useState } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { api, ApiError } from '../../api/cliente';
import type { Red, Ruta, RutaCalculada } from '../../api/tipos';
import { km, nsAMs } from '../../comun/formato';
import { MapaRed } from '../../comun/MapaRed';
import { Aviso, Boton, BotonEnlace, CodigoEstacion, Esqueleto, EstadoVacio, Pagina, Tarjeta } from '../../comun/ui';
import { usePedido, type EstadoPedido } from '../../comun/usePedido';
import { rutaDesdeEntrada, urlRuta, type EstadoRuta } from './estadoRuta';
import './ruta.css';

export function ResultadoRuta() {
  const [params] = useSearchParams();
  const estado = (useLocation().state ?? {}) as EstadoRuta;
  const origen = params.get('origen');
  const destino = params.get('destino');
  const red = usePedido(() => api.get<Red>('/red'), []);

  if (!origen || !destino) return <UltimaDelHistorial red={red} />;
  const coincide = (r?: { origen: string; destino: string }) => r?.origen === origen && r?.destino === destino;
  if (coincide(estado.ruta)) return <Detalle ruta={estado.ruta!} red={red} />;
  if (coincide(estado.entrada)) return <DesdeEntrada entrada={estado.entrada!} red={red} />;
  return <CalcularBajoPedido origen={origen} destino={destino} />;
}

const volver = (
  <BotonEnlace a="/buscar" icono="atras" tamano="sm">
    Nueva búsqueda
  </BotonEnlace>
);

function SinRuta() {
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

function CargandoRuta({ texto }: { texto: string }) {
  return (
    <Pagina figura="Fig. 6" titulo="Resultado de ruta" acciones={volver}>
      <div className="tarjeta">
        <Esqueleto lineas={4} etiqueta={texto} />
      </div>
    </Pagina>
  );
}

/** Pestana Ruta sin parametros: la ruta mas reciente del historial de este usuario (lo guarda el servidor). */
function UltimaDelHistorial({ red }: { red: EstadoPedido<Red> }) {
  const historial = usePedido(() => api.get<RutaCalculada[]>('/historial'), []);
  if (historial.cargando) return <CargandoRuta texto="Buscando tu última ruta…" />;
  const ultima = [...(historial.datos ?? [])].sort((a, b) => b.fecha.localeCompare(a.fecha))[0];
  // Historial pendiente (501), vacio o con error: no hay una ruta que mostrar.
  if (!ultima) return <SinRuta />;
  return <DesdeEntrada entrada={ultima} red={red} />;
}

/** Entrada del historial: se completa con GET /red, sin volver a calcular. */
function DesdeEntrada({ entrada, red }: { entrada: RutaCalculada; red: EstadoPedido<Red> }) {
  if (red.cargando) return <CargandoRuta texto="Cargando la ruta…" />;
  const ruta = red.datos ? rutaDesdeEntrada(entrada, red.datos) : null;
  if (!ruta) {
    return (
      <Pagina figura="Fig. 6" titulo="Resultado de ruta" acciones={volver}>
        <Aviso tono="aviso" titulo="Esta ruta ya no coincide con la red">
          <p>Alguna estación o tramo de este viaje cambió desde que lo calculaste. Vuelve a buscarlo para ver la ruta actual.</p>
        </Aviso>
      </Pagina>
    );
  }
  return <Detalle ruta={ruta} red={red} />;
}

/** URL pegada a mano o recargada sin state: calcular es una busqueda nueva, asi que la pide el usuario. */
function CalcularBajoPedido({ origen, destino }: { origen: string; destino: string }) {
  const navegar = useNavigate();
  const [calculando, setCalculando] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);

  async function calcular() {
    setCalculando(true);
    setError(null);
    try {
      const ruta = await api.get<Ruta>('/rutas', { origen, destino });
      const estado: EstadoRuta = { ruta };
      navegar(urlRuta(origen, destino), { replace: true, state: estado });
    } catch (e) {
      setError(e instanceof ApiError ? e : new ApiError(0, 'No se pudo calcular la ruta.'));
      setCalculando(false);
    }
  }

  const sinRuta = error?.status === 404;
  return (
    <Pagina angosta figura="Fig. 6" titulo="Resultado de ruta" acciones={volver}>
      <div className="tarjeta ruta__pedir">
        <p>
          Ruta de <CodigoEstacion codigo={origen} /> a <CodigoEstacion codigo={destino} />. Calcularla cuenta como una búsqueda nueva en tu
          historial.
        </p>
        {error && (
          <Aviso tono={sinRuta ? 'aviso' : 'error'} titulo={sinRuta ? 'No hay ruta entre estas estaciones' : 'No se pudo calcular la ruta'}>
            {sinRuta ? 'Ningún tramo activo conecta el origen con el destino (RN-6). Prueba con otra estación cercana.' : error.message}
          </Aviso>
        )}
        <Boton variante="primario" icono="ruta" cargando={calculando} onClick={calcular}>
          Calcular esta ruta
        </Boton>
      </div>
    </Pagina>
  );
}

function Detalle({ ruta: r, red }: { ruta: Ruta; red: EstadoPedido<Red> }) {
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
