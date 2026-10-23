// Registro de paradas (Fig. 9). Rol dueno: Paradas. Solo ADMIN.
// Endpoints: GET /estaciones, GET /estaciones/buscar?texto=, POST /estaciones,
//            PUT /estaciones/{codigo} (pendiente, 501), PATCH /estaciones/{codigo}/estado (pendiente, 501).
// Editar y Activar/Desactivar ya llaman a su endpoint: cuando respondan 204 la tabla se recarga sola.
import { useState, type FormEvent } from 'react';
import { api, ApiError, mensajeDe } from '../../api/cliente';
import type { Estacion } from '../../api/tipos';
import { Cargando } from '../../comun/Cargando';
import { MensajeError } from '../../comun/MensajeError';
import { Pagina } from '../../comun/Pagina';
import { Pendiente } from '../../comun/Pendiente';
import { normalizar, useEstaciones } from '../../comun/useEstaciones';
import './paradas.css';

type Formulario = { codigo: string; nombre: string; zona: string; x: string; y: string; activa: boolean };
const VACIO: Formulario = { codigo: '', nombre: '', zona: '', x: '', y: '', activa: true };

/** Resultado de una accion por fila: mensaje normal o pendiente (501). */
type AvisoFila = { codigo: string; texto: string; pendiente: boolean; tono: 'error' | 'exito' };

export function RegistroParadas() {
  const estaciones = useEstaciones();
  const [filtro, setFiltro] = useState('');
  const [exacta, setExacta] = useState<{ estado: 'ok'; e: Estacion } | { estado: 'error'; texto: string } | null>(null);
  const [buscando, setBuscando] = useState(false);

  const [form, setForm] = useState<Formulario>(VACIO);
  const [editando, setEditando] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [avisoForm, setAvisoForm] = useState<{ tono: 'error' | 'exito'; texto: string; pendiente?: boolean } | null>(null);
  const [avisoFila, setAvisoFila] = useState<AvisoFila | null>(null);

  const f = normalizar(filtro);
  const filas = estaciones.lista.filter((e) => !f || normalizar(e.codigo).includes(f) || normalizar(e.nombre).includes(f));

  async function buscarExacto() {
    if (!filtro.trim()) return;
    setBuscando(true);
    setExacta(null);
    try {
      const e = await api.get<Estacion>('/estaciones/buscar', { texto: filtro.trim() });
      setExacta({ estado: 'ok', e });
    } catch (err) {
      setExacta({ estado: 'error', texto: err instanceof ApiError && err.status === 404 ? `No existe una estación llamada “${filtro.trim()}”.` : mensajeDe(err) });
    } finally {
      setBuscando(false);
    }
  }

  function campo<K extends keyof Formulario>(k: K, v: Formulario[K]) {
    setForm((p) => ({ ...p, [k]: v }));
  }

  function empezarEdicion(e: Estacion) {
    setEditando(e.codigo);
    setForm({ codigo: e.codigo, nombre: e.nombre, zona: e.zona, x: String(e.x), y: String(e.y), activa: e.activa });
    setAvisoForm(null);
    document.getElementById('form-parada')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  function cancelarEdicion() {
    setEditando(null);
    setForm(VACIO);
    setAvisoForm(null);
  }

  async function guardar(ev: FormEvent) {
    ev.preventDefault();
    setAvisoForm(null);
    const cuerpo: Estacion = {
      codigo: form.codigo.trim().toUpperCase(),
      nombre: form.nombre.trim(),
      zona: form.zona.trim(),
      x: Number(form.x || 0),
      y: Number(form.y || 0),
      activa: form.activa,
    };
    setEnviando(true);
    try {
      if (editando) {
        await api.put(`/estaciones/${encodeURIComponent(editando)}`, cuerpo);
        setAvisoForm({ tono: 'exito', texto: `Parada ${editando} actualizada.` });
        setEditando(null);
      } else {
        await api.post('/estaciones', cuerpo);
        setAvisoForm({ tono: 'exito', texto: `Parada ${cuerpo.codigo} · ${cuerpo.nombre} registrada.` });
      }
      setForm(VACIO);
      estaciones.recargar();
    } catch (e) {
      setAvisoForm({ tono: 'error', texto: mensajeDe(e), pendiente: e instanceof ApiError && e.pendiente });
    } finally {
      setEnviando(false);
    }
  }

  async function alternarEstado(e: Estacion) {
    setAvisoFila(null);
    try {
      await api.patch(`/estaciones/${encodeURIComponent(e.codigo)}/estado`, { activa: !e.activa });
      setAvisoFila({ codigo: e.codigo, texto: `${e.nombre} ahora está ${e.activa ? 'inactiva' : 'activa'}.`, pendiente: false, tono: 'exito' });
      estaciones.recargar();
    } catch (err) {
      setAvisoFila({ codigo: e.codigo, texto: mensajeDe(err), pendiente: err instanceof ApiError && err.pendiente, tono: 'error' });
    }
  }

  return (
    <Pagina figura="Fig. 9 · Paradas" titulo="Registro de paradas" descripcion="Alta, edición y estado de las estaciones de la red.">
      <div className="paradas">
        <section id="form-parada" className="tarjeta tarjeta--relleno" aria-labelledby="paradas-form">
          <h2 id="paradas-form">{editando ? `Editar parada ${editando}` : 'Nueva parada'}</h2>
          <form className="formulario paradas__form" onSubmit={guardar} noValidate>
            <div className="formulario__fila">
              <label className="campo">
                <span>Código</span>
                <input value={form.codigo} onChange={(e) => campo('codigo', e.target.value)} placeholder="E09" disabled={!!editando} />
              </label>
              <label className="campo">
                <span>Zona</span>
                <input value={form.zona} onChange={(e) => campo('zona', e.target.value)} placeholder="Centro" />
              </label>
            </div>
            <label className="campo">
              <span>Nombre</span>
              <input value={form.nombre} onChange={(e) => campo('nombre', e.target.value)} placeholder="Ej. Plaza Mundo" />
            </label>
            <div className="formulario__fila">
              <label className="campo">
                <span>Coordenada x</span>
                <input type="number" step="any" value={form.x} onChange={(e) => campo('x', e.target.value)} placeholder="0 – 100" />
              </label>
              <label className="campo">
                <span>Coordenada y</span>
                <input type="number" step="any" value={form.y} onChange={(e) => campo('y', e.target.value)} placeholder="0 – 100" />
              </label>
            </div>
            <label className="casilla">
              <input type="checkbox" checked={form.activa} onChange={(e) => campo('activa', e.target.checked)} />
              Activa (en servicio)
            </label>
            {avisoForm &&
              (avisoForm.pendiente ? (
                <Pendiente enLinea rol="Paradas" mensaje={avisoForm.texto} />
              ) : (
                <MensajeError tono={avisoForm.tono} mensaje={avisoForm.texto} />
              ))}
            <div className="paradas__acciones">
              <button className="boton boton--primario" type="submit" disabled={enviando}>
                {enviando ? 'Guardando…' : editando ? 'Guardar cambios' : 'Registrar parada'}
              </button>
              {editando && (
                <button className="boton" type="button" onClick={cancelarEdicion}>
                  Cancelar
                </button>
              )}
            </div>
          </form>
        </section>
        <section className="tarjeta tarjeta--relleno" aria-labelledby="paradas-tabla">
          <div className="tarjeta__titulo">
            <h2 id="paradas-tabla">Estaciones</h2>
            <span className="texto-suave paradas__conteo">
              {filas.length} de {estaciones.lista.length}
            </span>
          </div>

          <form
            className="paradas__buscar"
            role="search"
            onSubmit={(ev) => {
              ev.preventDefault();
              buscarExacto();
            }}
          >
            <label className="oculto-visual" htmlFor="filtro-paradas">
              Filtrar por código o nombre
            </label>
            <input
              id="filtro-paradas"
              className="entrada"
              type="search"
              placeholder="Filtrar por código o nombre…"
              value={filtro}
              onChange={(e) => {
                setFiltro(e.target.value);
                setExacta(null);
              }}
            />
            <button type="submit" className="boton" disabled={!filtro.trim() || buscando} title="Busca en el servidor por nombre exacto o código">
              {buscando ? 'Buscando…' : 'Buscar exacto'}
            </button>
          </form>
          {exacta?.estado === 'ok' && (
            <MensajeError tono="info" mensaje={<>Encontrada en el servidor: <strong>{exacta.e.codigo} · {exacta.e.nombre}</strong> ({exacta.e.zona}, x={exacta.e.x}, y={exacta.e.y}, {exacta.e.activa ? 'activa' : 'inactiva'})</>} />
          )}
          {exacta?.estado === 'error' && <MensajeError tono="aviso" mensaje={exacta.texto} />}

          {estaciones.cargando && !estaciones.datos ? (
            <Cargando />
          ) : estaciones.error ? (
            <MensajeError mensaje={estaciones.error.message} />
          ) : (
            <div className="tabla-contenedor paradas__tabla">
              <table className="tabla">
                <thead>
                  <tr>
                    <th scope="col">Código</th>
                    <th scope="col">Nombre</th>
                    <th scope="col" className="col-zona">Zona</th>
                    <th scope="col" className="num col-xy">x, y</th>
                    <th scope="col">Estado</th>
                    <th scope="col">
                      <span className="oculto-visual">Acciones</span>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {filas.length === 0 && (
                    <tr>
                      <td colSpan={6} className="vacio">
                        Ninguna estación coincide con “{filtro}”.
                      </td>
                    </tr>
                  )}
                  {filas.map((e) => (
                    <FilaParada
                      key={e.codigo}
                      e={e}
                      resaltada={exacta?.estado === 'ok' && exacta.e.codigo === e.codigo}
                      editando={editando === e.codigo}
                      aviso={avisoFila?.codigo === e.codigo ? avisoFila : null}
                      alEditar={() => empezarEdicion(e)}
                      alAlternar={() => alternarEstado(e)}
                    />
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

      </div>
    </Pagina>
  );
}

function FilaParada({
  e,
  resaltada,
  editando,
  aviso,
  alEditar,
  alAlternar,
}: {
  e: Estacion;
  resaltada: boolean;
  editando: boolean;
  aviso: AvisoFila | null;
  alEditar: () => void;
  alAlternar: () => void;
}) {
  return (
    <>
      <tr className={`${e.activa ? '' : 'es-tenue'} ${resaltada || editando ? 'es-resaltada' : ''}`}>
        <td className="mono">{e.codigo}</td>
        <td>{e.nombre}</td>
        <td className="col-zona">{e.zona}</td>
        <td className="num col-xy">
          {e.x}, {e.y}
        </td>
        <td>
          <span className={`insignia insignia--${e.activa ? 'activa' : 'inactiva'}`}>{e.activa ? 'Activa' : 'Inactiva'}</span>
        </td>
        <td>
          <div className="paradas__fila-acciones">
            <button type="button" className="boton boton--chico" onClick={alEditar} aria-label={`Editar ${e.nombre}`}>
              Editar
            </button>
            <button
              type="button"
              className={`boton boton--chico ${e.activa ? 'boton--peligro' : ''}`}
              onClick={alAlternar}
              aria-label={`${e.activa ? 'Desactivar' : 'Activar'} ${e.nombre}`}
            >
              {e.activa ? 'Desactivar' : 'Activar'}
            </button>
          </div>
        </td>
      </tr>
      {aviso && (
        <tr className="paradas__aviso">
          <td colSpan={6}>
            {aviso.pendiente ? <Pendiente enLinea rol="Paradas" mensaje={aviso.texto} /> : <MensajeError tono={aviso.tono} mensaje={aviso.texto} />}
          </td>
        </tr>
      )}
    </>
  );
}
