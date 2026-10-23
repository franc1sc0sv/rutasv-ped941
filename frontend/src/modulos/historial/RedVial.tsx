// Red vial (Fig. 8). Rol dueno: Historial (pantalla) sobre el grafo del rol Ruta. Solo ADMIN.
// Endpoints: GET /red (estaciones + tramos), POST /red/tramos {origen, destino, minutos, km}.
// Nota: el backend guarda tramos dirigidos; "doble sentido" envia dos POST (ida y vuelta).
import { useState, type FormEvent } from 'react';
import { api, mensajeDe } from '../../api/cliente';
import type { Red } from '../../api/tipos';
import { Cargando } from '../../comun/Cargando';
import { km as fmtKm } from '../../comun/formato';
import { MapaRed } from '../../comun/MapaRed';
import { MensajeError } from '../../comun/MensajeError';
import { Pagina } from '../../comun/Pagina';
import { usePedido } from '../../comun/usePedido';
import './historial.css';

export function RedVial() {
  const red = usePedido(() => api.get<Red>('/red'), []);
  const [origen, setOrigen] = useState('');
  const [destino, setDestino] = useState('');
  const [minutos, setMinutos] = useState('');
  const [km, setKm] = useState('');
  const [doble, setDoble] = useState(true);
  const [enviando, setEnviando] = useState(false);
  const [aviso, setAviso] = useState<{ tono: 'error' | 'exito'; texto: string } | null>(null);

  const estaciones = red.datos?.estaciones ?? [];
  const nombre = (c: string) => estaciones.find((e) => e.codigo === c)?.nombre ?? c;
  const activas = estaciones.filter((e) => e.activa).length;
  const tramosUnicos = new Set((red.datos?.tramos ?? []).map((t) => [t.origen, t.destino].sort().join('|'))).size;

  async function agregar(ev: FormEvent) {
    ev.preventDefault();
    setAviso(null);
    if (!origen || !destino) {
      setAviso({ tono: 'error', texto: 'Elige la estación de origen y la de destino.' });
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
        texto: `Tramo ${nombre(origen)} ${doble ? '↔' : '→'} ${nombre(destino)} agregado (${cuerpo.minutos} min, ${fmtKm(cuerpo.km)}).`,
      });
      setMinutos('');
      setKm('');
      red.recargar();
    } catch (e) {
      setAviso({ tono: 'error', texto: mensajeDe(e) });
    } finally {
      setEnviando(false);
    }
  }

  return (
    <Pagina
      figura="Fig. 8 · Historial"
      titulo="Red vial"
      descripcion="Estaciones y tramos con su tiempo en minutos. Las estaciones inactivas aparecen en gris."
      acciones={
        <button type="button" className="boton" onClick={red.recargar}>
          Actualizar
        </button>
      }
    >
      <div className="rejilla-2">
        <section className="tarjeta tarjeta--relleno" aria-labelledby="red-mapa">
          <div className="tarjeta__titulo">
            <h2 id="red-mapa">Mapa de la red</h2>
            {red.datos && (
              <span className="texto-suave red__resumen">
                {estaciones.length} estaciones ({activas} activas) · {tramosUnicos} tramos
              </span>
            )}
          </div>
          {red.cargando && !red.datos ? (
            <Cargando texto="Cargando red…" />
          ) : red.error ? (
            <MensajeError mensaje={red.error.message} />
          ) : red.datos ? (
            <>
              <MapaRed estaciones={red.datos.estaciones} tramos={red.datos.tramos} titulo="Red vial completa" className="mapa--grande" />
              <ul className="leyenda lista-limpia" aria-label="Leyenda">
                <li>
                  <span className="leyenda__punto" /> Estación activa
                </li>
                <li>
                  <span className="leyenda__punto leyenda__punto--inactiva" /> Estación inactiva
                </li>
                <li>
                  <span className="leyenda__linea" /> Tramo (minutos)
                </li>
              </ul>
            </>
          ) : null}
        </section>

        <section className="tarjeta tarjeta--relleno" aria-labelledby="red-nuevo">
          <h2 id="red-nuevo">Agregar tramo</h2>
          <form className="formulario" onSubmit={agregar} noValidate>
            <label className="campo">
              <span>Origen</span>
              <select value={origen} onChange={(e) => setOrigen(e.target.value)} required>
                <option value="">Elige una estación…</option>
                {estaciones.map((e) => (
                  <option key={e.codigo} value={e.codigo}>
                    {e.codigo} · {e.nombre}
                  </option>
                ))}
              </select>
            </label>
            <label className="campo">
              <span>Destino</span>
              <select value={destino} onChange={(e) => setDestino(e.target.value)} required>
                <option value="">Elige una estación…</option>
                {estaciones.map((e) => (
                  <option key={e.codigo} value={e.codigo}>
                    {e.codigo} · {e.nombre}
                  </option>
                ))}
              </select>
            </label>
            <div className="formulario__fila">
              <label className="campo">
                <span>Minutos</span>
                <input type="number" inputMode="numeric" step="1" value={minutos} onChange={(e) => setMinutos(e.target.value)} placeholder="Ej. 6" />
              </label>
              <label className="campo">
                <span>Kilómetros</span>
                <input type="number" inputMode="decimal" step="0.1" value={km} onChange={(e) => setKm(e.target.value)} placeholder="Ej. 3.2" />
              </label>
            </div>
            <label className="casilla">
              <input type="checkbox" checked={doble} onChange={(e) => setDoble(e.target.checked)} />
              Doble sentido (agrega también la vuelta)
            </label>
            {aviso && <MensajeError tono={aviso.tono} mensaje={aviso.texto} />}
            <button className="boton boton--primario" type="submit" disabled={enviando || !red.datos}>
              {enviando ? 'Guardando…' : 'Agregar tramo'}
            </button>
            <p className="texto-suave red__nota">Si el tramo ya existe, se reemplaza con los nuevos minutos y km.</p>
          </form>
        </section>
      </div>
    </Pagina>
  );
}
