// Registro de paradas (Fig. 9). Rol dueno: Paradas. Solo ADMIN.
// Endpoints: GET /estaciones, GET /estaciones/buscar?texto=, POST /estaciones,
//            PUT /estaciones/{codigo} (pendiente, 501), PATCH /estaciones/{codigo}/estado (pendiente, 501).
// Editar y Activar/Desactivar ya llaman a su endpoint: cuando respondan 204 la tabla se recarga sola.
import { useState, type FormEvent } from 'react';
import { api, ApiError, mensajeDe } from '../../api/cliente';
import type { Estacion } from '../../api/tipos';
import { normalizar, useEstaciones } from '../../comun/useEstaciones';
import {
  Aviso,
  Boton,
  Campo,
  Casilla,
  CodigoEstacion,
  Entrada,
  Esqueleto,
  FilaVacia,
  Insignia,
  Pagina,
  Pendiente,
  Tabla,
  Tarjeta,
} from '../../comun/ui';
import './paradas.css';

type Formulario = { codigo: string; nombre: string; zona: string; x: string; y: string; activa: boolean };
const VACIO: Formulario = { codigo: '', nombre: '', zona: '', x: '', y: '', activa: true };

/** Resultado de una accion por fila: mensaje normal o pendiente (501). */
type AvisoFila = { codigo: string; texto: string; pendiente: boolean; tono: 'error' | 'exito' };

const COLUMNAS = 6;

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
      setExacta({
        estado: 'error',
        texto: err instanceof ApiError && err.status === 404 ? `No hay ninguna estación llamada “${filtro.trim()}”.` : mensajeDe(err),
      });
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
    const destino = document.getElementById('form-parada');
    destino?.scrollIntoView({ behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth', block: 'start' });
    destino?.querySelector<HTMLInputElement>('input:not(:disabled)')?.focus({ preventScroll: true });
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
    <Pagina figura="Fig. 9" titulo="Registro de paradas" descripcion="Alta, edición y estado de las estaciones de la red.">
      <div className="pila pila--grande">
        <Tarjeta id="form-parada" titulo={editando ? `Editar parada ${editando}` : 'Nueva parada'} className="paradas__formulario">
          <form className="paradas__form" onSubmit={guardar} noValidate>
            <Campo etiqueta="Código" ayuda={editando ? 'No se puede cambiar.' : undefined} className="paradas__codigo">
              <Entrada value={form.codigo} onChange={(e) => campo('codigo', e.target.value)} placeholder="E09" disabled={!!editando} />
            </Campo>
            <Campo etiqueta="Nombre" className="paradas__campo-nombre">
              <Entrada value={form.nombre} onChange={(e) => campo('nombre', e.target.value)} placeholder="Ej. Plaza Mundo" />
            </Campo>
            <Campo etiqueta="Zona">
              <Entrada value={form.zona} onChange={(e) => campo('zona', e.target.value)} placeholder="Centro" />
            </Campo>
            <Campo etiqueta="Coordenada x">
              <Entrada type="number" step="any" value={form.x} onChange={(e) => campo('x', e.target.value)} placeholder="0 a 100" />
            </Campo>
            <Campo etiqueta="Coordenada y">
              <Entrada type="number" step="any" value={form.y} onChange={(e) => campo('y', e.target.value)} placeholder="0 a 100" />
            </Campo>
            <div className="paradas__cierre">
              <Casilla checked={form.activa} onChange={(e) => campo('activa', e.target.checked)}>
                Activa (en servicio)
              </Casilla>
              <div className="fila-flex">
                {editando && <Boton onClick={cancelarEdicion}>Cancelar</Boton>}
                <Boton variante="primario" type="submit" icono={editando ? 'check' : 'mas'} cargando={enviando}>
                  {enviando ? 'Guardando…' : editando ? 'Guardar cambios' : 'Registrar parada'}
                </Boton>
              </div>
            </div>
            {avisoForm && (
              <div className="paradas__resultado">
                {avisoForm.pendiente ? <Pendiente enLinea rol="Paradas" mensaje={avisoForm.texto} /> : <Aviso tono={avisoForm.tono}>{avisoForm.texto}</Aviso>}
              </div>
            )}
          </form>
        </Tarjeta>
        <Tarjeta titulo="Estaciones" detalle={`${filas.length} de ${estaciones.lista.length}`} sinRelleno>
          <div className="paradas__herramientas">
            <form
              className="paradas__buscar"
              role="search"
              aria-label="Buscar estaciones"
              onSubmit={(ev) => {
                ev.preventDefault();
                buscarExacto();
              }}
            >
              <Campo etiqueta="Filtrar por código o nombre" etiquetaOculta className="paradas__filtro">
                <Entrada
                  type="search"
                  placeholder="Filtrar por código o nombre…"
                  value={filtro}
                  onChange={(e) => {
                    setFiltro(e.target.value);
                    setExacta(null);
                  }}
                />
              </Campo>
              <Boton type="submit" icono="buscar" cargando={buscando} disabled={!filtro.trim()} title="Busca en el servidor por nombre exacto o código">
                Buscar exacto
              </Boton>
            </form>
            {exacta?.estado === 'ok' && (
              <Aviso tono="info">
                Encontrada en el servidor:{' '}
                <strong>
                  {exacta.e.codigo} · {exacta.e.nombre}
                </strong>{' '}
                ({exacta.e.zona}, x={exacta.e.x}, y={exacta.e.y}, {exacta.e.activa ? 'activa' : 'inactiva'})
              </Aviso>
            )}
            {exacta?.estado === 'error' && <Aviso tono="aviso">{exacta.texto}</Aviso>}
          </div>

          {estaciones.cargando && !estaciones.datos ? (
            <div className="paradas__cargando">
              <Esqueleto lineas={5} etiqueta="Cargando estaciones…" />
            </div>
          ) : estaciones.error ? (
            <div className="paradas__cargando">
              <Aviso tono="error" titulo="No se pudieron cargar las estaciones">
                {estaciones.error.message}
              </Aviso>
            </div>
          ) : (
            <Tabla etiqueta="Estaciones registradas" className="paradas__tabla">
              <thead>
                <tr>
                  <th scope="col">Código</th>
                  <th scope="col">Nombre</th>
                  <th scope="col" className="col-zona">
                    Zona
                  </th>
                  <th scope="col" className="num col-xy">
                    x, y
                  </th>
                  <th scope="col">Estado</th>
                  <th scope="col">
                    <span className="oculto-visual">Acciones</span>
                  </th>
                </tr>
              </thead>
              <tbody>
                {filas.length === 0 && <FilaVacia columnas={COLUMNAS}>Ninguna estación coincide con “{filtro}”.</FilaVacia>}
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
            </Tabla>
          )}
        </Tarjeta>

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
        <td>
          <CodigoEstacion codigo={e.codigo} />
        </td>
        <td className="paradas__nombre">{e.nombre}</td>
        <td className="col-zona">{e.zona}</td>
        <td className="num col-xy">
          {e.x}, {e.y}
        </td>
        <td>
          <Insignia tono={e.activa ? 'exito' : 'neutro'}>{e.activa ? 'Activa' : 'Inactiva'}</Insignia>
        </td>
        <td>
          <div className="paradas__acciones">
            <Boton tamano="sm" variante="fantasma" icono="editar" onClick={alEditar} aria-label={`Editar ${e.nombre}`}>
              Editar
            </Boton>
            <Boton
              tamano="sm"
              variante={e.activa ? 'peligro' : 'secundario'}
              icono="encendido"
              onClick={alAlternar}
              aria-label={`${e.activa ? 'Desactivar' : 'Activar'} ${e.nombre}`}
            >
              {e.activa ? 'Desactivar' : 'Activar'}
            </Boton>
          </div>
        </td>
      </tr>
      {aviso && (
        <tr className="paradas__aviso">
          <td colSpan={COLUMNAS}>
            {aviso.pendiente ? <Pendiente enLinea rol="Paradas" mensaje={aviso.texto} /> : <Aviso tono={aviso.tono}>{aviso.texto}</Aviso>}
          </td>
        </tr>
      )}
    </>
  );
}
