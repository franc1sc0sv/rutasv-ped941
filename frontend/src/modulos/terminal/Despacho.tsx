// Despacho (Fig. 10). Rol dueno: Terminal. Solo ADMIN.
// Endpoints: POST /terminal/unidades, GET /terminal/orden, POST /terminal/despachar,
//            POST /terminal/filas/{NORMAL|EXPRES}/pasajeros, POST /terminal/filas/{NORMAL|EXPRES}/atender.
//
// LIMITACION: la API no tiene un GET para ver el contenido de las filas de pasajeros.
// Esta pantalla lleva una copia local (FIFO) de lo que se encola/atiende desde AQUI;
// si otro cliente usa las filas, o se recarga la pagina, la copia local no coincide con el servidor.
// Cuando exista GET /terminal/filas/{tipo}, reemplaza `filas` por ese pedido.
import { useState, type FormEvent } from 'react';
import { api, ApiError, mensajeDe } from '../../api/cliente';
import type { Pasajero, TipoRuta, Unidad } from '../../api/tipos';
import { Cargando } from '../../comun/Cargando';
import { hora } from '../../comun/formato';
import { MensajeError } from '../../comun/MensajeError';
import { Pagina } from '../../comun/Pagina';
import { usePedido } from '../../comun/usePedido';
import './terminal.css';

const TIPOS: TipoRuta[] = ['NORMAL', 'EXPRES'];
type Aviso = { tono: 'error' | 'exito' | 'aviso' | 'info'; texto: string } | null;

export function Despacho() {
  const orden = usePedido(() => api.get<Unidad[]>('/terminal/orden'), []);

  // ---- Unidades ----
  const [idUnidad, setIdUnidad] = useState('');
  const [tipo, setTipo] = useState<TipoRuta>('NORMAL');
  const [horaProg, setHoraProg] = useState('07:00');
  const [avisoUnidad, setAvisoUnidad] = useState<Aviso>(null);
  const [despachada, setDespachada] = useState<Unidad | null>(null);
  const [avisoDespacho, setAvisoDespacho] = useState<Aviso>(null);
  const [ocupado, setOcupado] = useState(false);

  async function registrar(ev: FormEvent) {
    ev.preventDefault();
    setAvisoUnidad(null);
    if (!idUnidad.trim() || !horaProg) {
      setAvisoUnidad({ tono: 'error', texto: 'Escribe el id de la unidad y su hora programada.' });
      return;
    }
    setOcupado(true);
    try {
      await api.post('/terminal/unidades', { id: idUnidad.trim(), tipoRuta: tipo, horaProgramada: horaProg });
      setAvisoUnidad({ tono: 'exito', texto: `Unidad ${idUnidad.trim()} (${tipo}) registrada para las ${horaProg}.` });
      setIdUnidad('');
      orden.recargar();
    } catch (e) {
      setAvisoUnidad({ tono: 'error', texto: mensajeDe(e) });
    } finally {
      setOcupado(false);
    }
  }

  async function despachar() {
    setAvisoDespacho(null);
    setOcupado(true);
    try {
      const u = await api.post<Unidad>('/terminal/despachar');
      setDespachada(u);
      orden.recargar();
    } catch (e) {
      setDespachada(null);
      setAvisoDespacho(
        e instanceof ApiError && e.status === 409
          ? { tono: 'aviso', texto: 'No hay unidades en espera. Registra una unidad primero.' }
          : { tono: 'error', texto: mensajeDe(e) },
      );
    } finally {
      setOcupado(false);
    }
  }

  // ---- Filas de pasajeros (copia local, ver LIMITACION arriba) ----
  const [filas, setFilas] = useState<Record<TipoRuta, Pasajero[]>>({ NORMAL: [], EXPRES: [] });
  const [contador, setContador] = useState(1);

  return (
    <Pagina figura="Fig. 10 · Terminal" titulo="Despacho" descripcion="Las unidades EXPRES salen primero; entre iguales, la de hora más próxima (RN-4).">
      <div className="rejilla-2">
        <section className="tarjeta tarjeta--relleno" aria-labelledby="orden-titulo">
          <div className="tarjeta__titulo">
            <h2 id="orden-titulo">Orden de salida</h2>
            <button type="button" className="boton boton--primario" onClick={despachar} disabled={ocupado}>
              Despachar siguiente
            </button>
          </div>

          {despachada && (
            <div className="despachada" role="status" aria-live="polite">
              <span className="despachada__etiqueta">Despachada</span>
              <strong className="despachada__id">{despachada.id}</strong>
              <span className={`insignia insignia--${despachada.tipoRuta}`}>{despachada.tipoRuta}</span>
              <span className="texto-suave">{hora(despachada.horaProgramada)}</span>
            </div>
          )}
          {avisoDespacho && <MensajeError tono={avisoDespacho.tono} mensaje={avisoDespacho.texto} />}

          {orden.cargando && !orden.datos ? (
            <Cargando />
          ) : orden.error ? (
            <MensajeError mensaje={orden.error.message} />
          ) : (
            <div className="tabla-contenedor terminal__tabla">
              <table className="tabla">
                <thead>
                  <tr>
                    <th scope="col" className="num">#</th>
                    <th scope="col">Unidad</th>
                    <th scope="col">Tipo</th>
                    <th scope="col" className="num">Hora</th>
                  </tr>
                </thead>
                <tbody>
                  {(orden.datos ?? []).length === 0 ? (
                    <tr>
                      <td colSpan={4} className="vacio">
                        No hay unidades en espera.
                      </td>
                    </tr>
                  ) : (
                    orden.datos!.map((u, i) => (
                      <tr key={u.id} className={i === 0 ? 'es-siguiente' : ''}>
                        <td className="num">{i + 1}</td>
                        <td className="mono">
                          {u.id}
                          {i === 0 && <span className="terminal__siguiente">siguiente</span>}
                        </td>
                        <td>
                          <span className={`insignia insignia--${u.tipoRuta}`}>{u.tipoRuta}</span>
                        </td>
                        <td className="num">{hora(u.horaProgramada)}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          )}
        </section>

        <section className="tarjeta tarjeta--relleno" aria-labelledby="unidad-titulo">
          <h2 id="unidad-titulo">Registrar unidad</h2>
          <form className="formulario" onSubmit={registrar} noValidate>
            <label className="campo">
              <span>Id de la unidad</span>
              <input value={idUnidad} onChange={(e) => setIdUnidad(e.target.value)} placeholder="Ej. U-014" />
            </label>
            <fieldset className="campo terminal__tipos">
              <legend>Tipo de ruta</legend>
              <div className="segmentado" role="radiogroup" aria-label="Tipo de ruta">
                {TIPOS.map((t) => (
                  <button key={t} type="button" role="radio" aria-checked={tipo === t} onClick={() => setTipo(t)}>
                    {t === 'EXPRES' ? 'Exprés' : 'Normal'}
                  </button>
                ))}
              </div>
            </fieldset>
            <label className="campo">
              <span>Hora programada</span>
              <input type="time" value={horaProg} onChange={(e) => setHoraProg(e.target.value)} />
            </label>
            {avisoUnidad && <MensajeError tono={avisoUnidad.tono} mensaje={avisoUnidad.texto} />}
            <button className="boton boton--primario" type="submit" disabled={ocupado}>
              Registrar unidad
            </button>
          </form>
        </section>
      </div>

      <section aria-labelledby="filas-titulo" className="terminal__filas">
        <h2 id="filas-titulo">Filas de pasajeros</h2>
        <p className="texto-suave terminal__nota">
          Primero en llegar, primero en ser atendido (FIFO). La lista muestra lo encolado desde esta pantalla.
        </p>
        <div className="rejilla-2 rejilla-2--pareja">
          {TIPOS.map((t) => (
            <Fila
              key={t}
              tipo={t}
              pasajeros={filas[t]}
              sugerido={`P${contador}`}
              alEncolar={(p) => {
                setFilas((f) => ({ ...f, [t]: [...f[t], p] }));
                setContador((c) => c + 1);
              }}
              alAtender={(p) =>
                setFilas((f) => {
                  // Quita al atendido de la copia local (normalmente es el primero).
                  const i = f[t].findIndex((x) => x.id === p.id);
                  return { ...f, [t]: i >= 0 ? f[t].filter((_, j) => j !== i) : f[t].slice(1) };
                })
              }
              alVaciar={() => setFilas((f) => ({ ...f, [t]: [] }))}
            />
          ))}
        </div>
      </section>
    </Pagina>
  );
}

function Fila({
  tipo,
  pasajeros,
  sugerido,
  alEncolar,
  alAtender,
  alVaciar,
}: {
  tipo: TipoRuta;
  pasajeros: Pasajero[];
  sugerido: string;
  alEncolar: (p: Pasajero) => void;
  alAtender: (p: Pasajero) => void;
  alVaciar: () => void;
}) {
  const [nombre, setNombre] = useState('');
  const [id, setId] = useState('');
  const [aviso, setAviso] = useState<Aviso>(null);
  const [atendido, setAtendido] = useState<Pasajero | null>(null);
  const [ocupado, setOcupado] = useState(false);

  async function encolar(ev: FormEvent) {
    ev.preventDefault();
    setAviso(null);
    const p = { id: id.trim() || sugerido, nombre: nombre.trim() };
    if (!p.nombre) {
      setAviso({ tono: 'error', texto: 'Escribe el nombre del pasajero.' });
      return;
    }
    setOcupado(true);
    try {
      await api.post(`/terminal/filas/${tipo}/pasajeros`, p);
      alEncolar(p);
      setNombre('');
      setId('');
    } catch (e) {
      setAviso({ tono: 'error', texto: mensajeDe(e) });
    } finally {
      setOcupado(false);
    }
  }

  async function atender() {
    setAviso(null);
    setOcupado(true);
    try {
      const p = await api.post<Pasajero>(`/terminal/filas/${tipo}/atender`);
      setAtendido(p);
      alAtender(p);
    } catch (e) {
      setAtendido(null);
      if (e instanceof ApiError && e.status === 409) {
        setAviso({ tono: 'aviso', texto: `La fila ${tipo === 'EXPRES' ? 'exprés' : 'normal'} está vacía: no hay a quién atender.` });
        alVaciar();
      } else {
        setAviso({ tono: 'error', texto: mensajeDe(e) });
      }
    } finally {
      setOcupado(false);
    }
  }

  return (
    <section className={`tarjeta tarjeta--relleno fila fila--${tipo}`} aria-labelledby={`fila-${tipo}`}>
      <div className="tarjeta__titulo">
        <h3 id={`fila-${tipo}`} className="fila__titulo">
          <span className={`insignia insignia--${tipo}`}>{tipo}</span> Fila {tipo === 'EXPRES' ? 'exprés' : 'normal'}
        </h3>
        <span className="texto-suave fila__conteo">{pasajeros.length} en espera</span>
      </div>

      <ol className="fila__lista" aria-label={`Pasajeros en la fila ${tipo}`}>
        {pasajeros.length === 0 ? (
          <li className="fila__vacia texto-suave">Sin pasajeros</li>
        ) : (
          pasajeros.map((p, i) => (
            <li key={`${p.id}-${i}`} className={`fila__pasajero ${i === 0 ? 'es-primero' : ''}`}>
              <span className="fila__pos">{i + 1}</span>
              <span>{p.nombre}</span>
              <span className="mono texto-suave">{p.id}</span>
            </li>
          ))
        )}
      </ol>

      {atendido && (
        <p className="fila__atendido" role="status">
          Atendido: <strong>{atendido.nombre}</strong> <span className="texto-suave">({atendido.id})</span>
        </p>
      )}
      {aviso && <MensajeError tono={aviso.tono} mensaje={aviso.texto} />}

      <form className="fila__form" onSubmit={encolar} noValidate>
        <label className="campo">
          <span>Nombre</span>
          <input value={nombre} onChange={(e) => setNombre(e.target.value)} placeholder="Ej. Ana" />
        </label>
        <label className="campo fila__id">
          <span>Id</span>
          <input value={id} onChange={(e) => setId(e.target.value)} placeholder={sugerido} />
        </label>
        <button className="boton" type="submit" disabled={ocupado}>
          Agregar
        </button>
      </form>
      <button type="button" className="boton boton--primario boton--ancho" onClick={atender} disabled={ocupado}>
        Atender siguiente
      </button>
    </section>
  );
}
