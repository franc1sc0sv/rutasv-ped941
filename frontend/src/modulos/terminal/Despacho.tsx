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
import { hora } from '../../comun/formato';
import {
  Aviso,
  Boton,
  Campo,
  Entrada,
  Esqueleto,
  FilaVacia,
  Icono,
  InsigniaTipo,
  Pagina,
  Segmentado,
  Tabla,
  Tarjeta,
  type TonoAviso,
} from '../../comun/ui';
import { usePedido } from '../../comun/usePedido';
import './terminal.css';

const TIPOS: TipoRuta[] = ['EXPRES', 'NORMAL'];
type Resultado = { tono: TonoAviso; texto: string } | null;

/** Tiempo de la animacion de salida de una fila (debe coincidir con --dur-media). */
const SALIDA_MS = 220;
const sinMovimiento = () => window.matchMedia('(prefers-reduced-motion: reduce)').matches;
const nombreTipo = (t: TipoRuta) => (t === 'EXPRES' ? 'exprés' : 'normal');

export function Despacho() {
  const orden = usePedido(() => api.get<Unidad[]>('/terminal/orden'), []);

  // ---- Unidades ----
  const [idUnidad, setIdUnidad] = useState('');
  const [tipo, setTipo] = useState<TipoRuta>('NORMAL');
  const [horaProg, setHoraProg] = useState('07:00');
  const [avisoUnidad, setAvisoUnidad] = useState<Resultado>(null);
  const [despachada, setDespachada] = useState<Unidad | null>(null);
  const [avisoDespacho, setAvisoDespacho] = useState<Resultado>(null);
  const [ocupado, setOcupado] = useState(false);
  const [saliendo, setSaliendo] = useState<string | null>(null);

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
      setAvisoUnidad({ tono: 'exito', texto: `Unidad ${idUnidad.trim()} (${nombreTipo(tipo)}) registrada para las ${horaProg}.` });
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
      // La fila de la unidad sale de la tabla y luego se recarga el orden real del servidor.
      setSaliendo(u.id);
      window.setTimeout(
        () => {
          setSaliendo(null);
          orden.recargar();
        },
        sinMovimiento() ? 0 : SALIDA_MS,
      );
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
  const unidades = orden.datos ?? [];

  return (
    <Pagina figura="Fig. 10" titulo="Despacho" descripcion="Las unidades exprés salen primero; entre iguales, la de hora más próxima (RN-4).">
      <div className="rejilla-2">
        <Tarjeta
          titulo="Orden de salida"
          detalle={orden.datos ? `${unidades.length} en espera` : undefined}
          acciones={
            <Boton variante="primario" icono="despachar" onClick={despachar} disabled={ocupado}>
              Despachar siguiente
            </Boton>
          }
        >
          <div className="pila">
            {despachada && (
              <div className="salida" role="status" aria-live="polite" key={despachada.id}>
                <Icono nombre="check" tamano={20} className="salida__icono" />
                <span className="salida__texto">
                  Salió <strong className="salida__id">{despachada.id}</strong>
                </span>
                <InsigniaTipo tipo={despachada.tipoRuta} />
                <span className="salida__hora cifra">{hora(despachada.horaProgramada)}</span>
              </div>
            )}
            {avisoDespacho && <Aviso tono={avisoDespacho.tono}>{avisoDespacho.texto}</Aviso>}

            {orden.cargando && !orden.datos ? (
              <Esqueleto lineas={4} etiqueta="Cargando el orden de salida…" />
            ) : orden.error ? (
              <Aviso tono="error" titulo="No se pudo cargar el orden de salida">
                {orden.error.message}
              </Aviso>
            ) : (
              <Tabla etiqueta="Orden de salida de las unidades">
                <thead>
                  <tr>
                    <th scope="col" className="num despacho__col-turno">
                      Turno
                    </th>
                    <th scope="col">Unidad</th>
                    <th scope="col">Tipo</th>
                    <th scope="col" className="num">
                      Hora
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {unidades.length === 0 ? (
                    <FilaVacia columnas={4}>No hay unidades en espera. Registra una con el formulario.</FilaVacia>
                  ) : (
                    unidades.map((u, i) => (
                      <tr key={u.id} className={[i === 0 && 'es-siguiente', saliendo === u.id && 'es-saliendo'].filter(Boolean).join(' ')}>
                        <td className="num cifra despacho__col-turno">{i + 1}</td>
                        <td>
                          <span className="despacho__unidad cifra">{u.id}</span>
                          {i === 0 && <span className="despacho__siguiente">Siguiente</span>}
                        </td>
                        <td>
                          <InsigniaTipo tipo={u.tipoRuta} />
                        </td>
                        <td className="num cifra">{hora(u.horaProgramada)}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </Tabla>
            )}
          </div>
        </Tarjeta>

        <Tarjeta titulo="Registrar unidad">
          <form className="pila" onSubmit={registrar} noValidate>
            <Campo etiqueta="Id de la unidad">
              <Entrada value={idUnidad} onChange={(e) => setIdUnidad(e.target.value)} placeholder="Ej. U-014" autoComplete="off" />
            </Campo>
            <Segmentado
              nombre="tipo-ruta"
              etiqueta="Tipo de ruta"
              valor={tipo}
              alCambiar={setTipo}
              opciones={TIPOS.map((t) => ({ valor: t, texto: <InsigniaTipo tipo={t} /> }))}
            />
            <Campo etiqueta="Hora programada">
              <Entrada type="time" value={horaProg} onChange={(e) => setHoraProg(e.target.value)} />
            </Campo>
            {avisoUnidad && <Aviso tono={avisoUnidad.tono}>{avisoUnidad.texto}</Aviso>}
            <Boton variante="primario" type="submit" icono="mas" disabled={ocupado}>
              Registrar unidad
            </Boton>
          </form>
        </Tarjeta>
      </div>

      <section aria-labelledby="filas-titulo" className="pila">
        <div className="despacho__filas-cabeza">
          <h2 id="filas-titulo">Filas de pasajeros</h2>
          <p className="texto-suave">El primero en llegar es el primero en subir (FIFO). La lista muestra lo que se agregó desde esta pantalla.</p>
        </div>
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
  const [aviso, setAviso] = useState<Resultado>(null);
  const [atendido, setAtendido] = useState<Pasajero | null>(null);
  const [ocupado, setOcupado] = useState(false);
  const [saliendo, setSaliendo] = useState<string | null>(null);

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
      setSaliendo(p.id);
      window.setTimeout(
        () => {
          setSaliendo(null);
          alAtender(p);
        },
        sinMovimiento() ? 0 : SALIDA_MS,
      );
    } catch (e) {
      setAtendido(null);
      if (e instanceof ApiError && e.status === 409) {
        setAviso({ tono: 'aviso', texto: `La fila ${nombreTipo(tipo)} está vacía: no hay a quién atender.` });
        alVaciar();
      } else {
        setAviso({ tono: 'error', texto: mensajeDe(e) });
      }
    } finally {
      setOcupado(false);
    }
  }

  return (
    <Tarjeta
      nivel={3}
      acento={tipo === 'EXPRES' ? 'expres' : 'normal'}
      titulo={<>Fila {nombreTipo(tipo)}</>}
      detalle={`${pasajeros.length} en espera`}
      acciones={<InsigniaTipo tipo={tipo} />}
      className="fila"
    >
      <div className="pila">
        <ol className="fila__lista" aria-label={`Pasajeros en la fila ${nombreTipo(tipo)}`}>
          {pasajeros.length === 0 ? (
            <li className="fila__vacia">Nadie en espera.</li>
          ) : (
            pasajeros.map((p, i) => (
              <li key={`${p.id}-${i}`} className={['fila__pasajero', i === 0 && 'es-primero', saliendo === p.id && i === 0 && 'es-saliendo'].filter(Boolean).join(' ')}>
                <span className="fila__pos cifra">{i + 1}</span>
                <span className="fila__nombre">{p.nombre}</span>
                <span className="fila__id cifra">{p.id}</span>
                {i === 0 && <span className="fila__turno">Siguiente</span>}
              </li>
            ))
          )}
        </ol>

        {atendido && (
          <p className="fila__atendido" role="status">
            <Icono nombre="check" tamano={16} /> Atendido: <strong>{atendido.nombre}</strong> <span className="texto-suave">({atendido.id})</span>
          </p>
        )}
        {aviso && <Aviso tono={aviso.tono}>{aviso.texto}</Aviso>}

        <Boton variante="primario" ancho icono="persona" onClick={atender} disabled={ocupado}>
          Atender siguiente
        </Boton>

        <form className="fila__form" onSubmit={encolar} noValidate aria-label={`Agregar pasajero a la fila ${nombreTipo(tipo)}`}>
          <Campo etiqueta="Nombre">
            <Entrada value={nombre} onChange={(e) => setNombre(e.target.value)} placeholder="Ej. Ana" autoComplete="off" />
          </Campo>
          <Campo etiqueta="Id" className="fila__campo-id">
            <Entrada value={id} onChange={(e) => setId(e.target.value)} placeholder={sugerido} autoComplete="off" />
          </Campo>
          <Boton type="submit" icono="mas" disabled={ocupado}>
            Agregar
          </Boton>
        </form>
      </div>
    </Tarjeta>
  );
}
