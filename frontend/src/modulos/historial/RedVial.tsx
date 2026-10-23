// Red vial (Fig. 8). Rol dueno: Historial (pantalla) sobre el grafo del rol Ruta. Solo ADMIN.
// Endpoints: GET /red (estaciones + tramos), POST /red/tramos {origen, destino, minutos, km}.
// Nota: el backend guarda tramos dirigidos; "doble sentido" envia dos POST (ida y vuelta).
import { useMemo, useState, type FormEvent } from 'react';
import { api, mensajeDe } from '../../api/cliente';
import type { Red } from '../../api/tipos';
import { km as fmtKm } from '../../comun/formato';
import { MapaRed } from '../../comun/MapaRed';
import { Aviso, Boton, Campo, Casilla, CodigoEstacion, Entrada, Esqueleto, FilaVacia, Pagina, Selector, Tabla, Tarjeta } from '../../comun/ui';
import { usePedido } from '../../comun/usePedido';
import './historial.css';

type Resultado = { tono: 'error' | 'exito'; texto: string; campo?: 'minutos' } | null;

export function RedVial() {
  const red = usePedido(() => api.get<Red>('/red'), []);
  const [origen, setOrigen] = useState('');
  const [destino, setDestino] = useState('');
  const [minutos, setMinutos] = useState('');
  const [km, setKm] = useState('');
  const [doble, setDoble] = useState(true);
  const [enviando, setEnviando] = useState(false);
  const [aviso, setAviso] = useState<Resultado>(null);

  const estaciones = red.datos?.estaciones ?? [];
  const nombre = (c: string) => estaciones.find((e) => e.codigo === c)?.nombre ?? c;
  const activas = estaciones.filter((e) => e.activa).length;

  // Un renglon por par de estaciones (ida y vuelta juntas).
  const pares = useMemo(() => {
    const m = new Map<string, { a: string; b: string; minutos: number; km: number; vuelta?: number }>();
    for (const t of red.datos?.tramos ?? []) {
      const clave = [t.origen, t.destino].sort().join('|');
      const p = m.get(clave);
      if (!p) m.set(clave, { a: t.origen, b: t.destino, minutos: t.minutos, km: t.km });
      else if (p.a !== t.origen) p.vuelta = t.minutos;
    }
    return [...m.values()].sort((x, y) => x.a.localeCompare(y.a) || x.b.localeCompare(y.b));
  }, [red.datos]);

  async function agregar(ev: FormEvent) {
    ev.preventDefault();
    setAviso(null);
    if (!origen || !destino) {
      setAviso({ tono: 'error', texto: 'Elige las dos estaciones del tramo: Desde y Hasta.' });
      return;
    }
    // Se envian tal cual: el backend valida (minutos <= 0 -> 400 RN-2) y la pantalla muestra su mensaje.
    const cuerpo = { origen, destino, minutos: Number(minutos || 0), km: Number(km || 0) };
    setEnviando(true);
    try {
      await api.post('/red/tramos', cuerpo);
      if (doble) await api.post('/red/tramos', { ...cuerpo, origen: destino, destino: origen });
      setAviso({
        tono: 'exito',
        texto: `Tramo ${nombre(origen)} ${doble ? '↔' : '→'} ${nombre(destino)} guardado: ${cuerpo.minutos} min, ${fmtKm(cuerpo.km)}.`,
      });
      setMinutos('');
      setKm('');
      red.recargar();
    } catch (e) {
      const texto = mensajeDe(e);
      setAviso({ tono: 'error', texto, campo: /minuto/i.test(texto) ? 'minutos' : undefined });
    } finally {
      setEnviando(false);
    }
  }

  const opciones = estaciones.map((e) => (
    <option key={e.codigo} value={e.codigo}>
      {e.codigo} · {e.nombre}
      {e.activa ? '' : ' (inactiva)'}
    </option>
  ));

  return (
    <Pagina
      figura="Fig. 8"
      titulo="Red vial"
      descripcion="Estaciones y tramos con su tiempo en minutos. Las estaciones inactivas aparecen punteadas."
      acciones={
        <Boton icono="recargar" onClick={red.recargar}>
          Actualizar
        </Boton>
      }
    >
      <div className="rejilla-2">
        <div className="pila pila--grande">
          <Tarjeta
            titulo="Mapa de la red"
            detalle={red.datos ? `${estaciones.length} estaciones · ${activas} activas · ${pares.length} tramos` : undefined}
          >
            {red.cargando && !red.datos ? (
              <Esqueleto alto={320} etiqueta="Cargando la red…" />
            ) : red.error ? (
              <Aviso tono="error" titulo="No se pudo cargar la red">
                {red.error.message}
              </Aviso>
            ) : red.datos ? (
              <MapaRed estaciones={red.datos.estaciones} tramos={red.datos.tramos} titulo="Red vial completa" />
            ) : null}
          </Tarjeta>

          {red.datos && (
            <Tarjeta titulo="Tramos" detalle="ida y vuelta en un renglón" sinRelleno>
              <Tabla etiqueta="Tramos de la red vial" className="red__tabla">
                <thead>
                  <tr>
                    <th scope="col">Desde</th>
                    <th scope="col">Hasta</th>
                    <th scope="col" className="num">
                      Minutos
                    </th>
                    <th scope="col" className="num">
                      Distancia
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {pares.length === 0 ? (
                    <FilaVacia columnas={4}>La red no tiene tramos todavía. Agrega el primero con el formulario.</FilaVacia>
                  ) : (
                    pares.map((p) => (
                      <tr key={`${p.a}-${p.b}`}>
                        <td>
                          <span className="red__celda-estacion">
                            <CodigoEstacion codigo={p.a} /> {nombre(p.a)}
                          </span>
                        </td>
                        <td>
                          <span className="red__celda-estacion">
                            <CodigoEstacion codigo={p.b} /> {nombre(p.b)}
                          </span>
                        </td>
                        <td className="num cifra">{p.vuelta !== undefined && p.vuelta !== p.minutos ? `${p.minutos} / ${p.vuelta}` : p.minutos}</td>
                        <td className="num">{fmtKm(p.km)}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </Tabla>
            </Tarjeta>
          )}
        </div>

        <Tarjeta titulo="Agregar tramo" className="red__formulario">
          <form className="pila" onSubmit={agregar} noValidate>
            <Campo etiqueta="Desde">
              <Selector value={origen} onChange={(e) => setOrigen(e.target.value)} required>
                <option value="">Elige una estación…</option>
                {opciones}
              </Selector>
            </Campo>
            <Campo etiqueta="Hasta">
              <Selector value={destino} onChange={(e) => setDestino(e.target.value)} required>
                <option value="">Elige una estación…</option>
                {opciones}
              </Selector>
            </Campo>
            <div className="red__medidas">
              <Campo etiqueta="Minutos" error={aviso?.campo === 'minutos' ? aviso.texto : undefined}>
                <Entrada type="number" inputMode="numeric" step="1" min="1" value={minutos} onChange={(e) => setMinutos(e.target.value)} placeholder="Ej. 6" />
              </Campo>
              <Campo etiqueta="Kilómetros">
                <Entrada type="number" inputMode="decimal" step="0.1" min="0" value={km} onChange={(e) => setKm(e.target.value)} placeholder="Ej. 3.2" />
              </Campo>
            </div>
            <Casilla checked={doble} onChange={(e) => setDoble(e.target.checked)}>
              Doble sentido (agrega también la vuelta)
            </Casilla>
            {aviso && !aviso.campo && <Aviso tono={aviso.tono}>{aviso.texto}</Aviso>}
            <Boton variante="primario" type="submit" icono="mas" cargando={enviando} disabled={!red.datos}>
              {enviando ? 'Guardando…' : 'Agregar tramo'}
            </Boton>
            <p className="campo__ayuda">Si el tramo ya existe, se reemplaza con los nuevos minutos y kilómetros.</p>
          </form>
        </Tarjeta>
      </div>
    </Pagina>
  );
}
