// Buscar ruta (Fig. 5). Rol dueno: Busqueda.
// Endpoints: GET /estaciones/sugerencias?prefijo=, GET /busquedas/recientes, GET /estaciones (nombre -> codigo).
// Calcular ruta: GET /rutas (aqui y solo aqui se calcula; con RN-7 cada calculo queda en el historial).
// Con el resultado navega a /ruta?origen=E01&destino=E04 y le pasa la ruta en el state de la navegacion.
import { useRef, useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, ApiError } from '../../api/cliente';
import type { Ruta } from '../../api/tipos';
import { Aviso, Boton, CodigoEstacion, Esqueleto, Icono, Pagina } from '../../comun/ui';
import { usePedido } from '../../comun/usePedido';
import { useEstaciones } from '../../comun/useEstaciones';
import { urlRuta, type EstadoRuta } from '../ruta/estadoRuta';
import { Autocompletar } from './Autocompletar';
import './busqueda.css';

type Campo = 'origen' | 'destino';

export function BuscarRuta() {
  const navegar = useNavigate();
  const estaciones = useEstaciones();
  const recientes = usePedido(() => api.get<string[]>('/busquedas/recientes'), []);
  const [origen, setOrigen] = useState('');
  const [destino, setDestino] = useState('');
  const [enfocado, setEnfocado] = useState<Campo>('origen');
  const [error, setError] = useState<string | null>(null);
  const [calculando, setCalculando] = useState(false);
  const refOrigen = useRef<HTMLInputElement>(null);
  const refDestino = useRef<HTMLInputElement>(null);

  const eOrigen = estaciones.resolver(origen);
  const eDestino = estaciones.resolver(destino);

  function intercambiar() {
    setOrigen(destino);
    setDestino(origen);
    setError(null);
  }

  function usarReciente(texto: string) {
    if (enfocado === 'origen') {
      setOrigen(texto);
      refOrigen.current?.focus();
    } else {
      setDestino(texto);
      refDestino.current?.focus();
    }
  }

  async function calcular(ev: FormEvent) {
    ev.preventDefault();
    if (calculando) return;
    setError(null);
    if (!eOrigen || !eDestino) {
      const falta = !eOrigen ? 'origen' : 'destino';
      setError(`Elige la estación de ${falta} de la lista de sugerencias.`);
      (falta === 'origen' ? refOrigen : refDestino).current?.focus();
      return;
    }
    if (eOrigen.codigo === eDestino.codigo) {
      setError('El origen y el destino son la misma estación. Elige dos distintas.');
      return;
    }
    setCalculando(true);
    try {
      const ruta = await api.get<Ruta>('/rutas', { origen: eOrigen.codigo, destino: eDestino.codigo });
      const estado: EstadoRuta = { ruta };
      navegar(urlRuta(eOrigen.codigo, eDestino.codigo), { state: estado });
    } catch (e) {
      const sinRuta = e instanceof ApiError && e.status === 404;
      setError(
        sinRuta
          ? 'No hay ruta entre estas estaciones: ningún tramo activo las conecta (RN-6). Prueba con otra estación cercana.'
          : e instanceof Error
            ? e.message
            : 'No se pudo calcular la ruta. Inténtalo de nuevo.',
      );
      setCalculando(false);
    }
  }

  const ayuda = (texto: string, e: typeof eOrigen) =>
    e ? (
      <span className="buscar__resuelta">
        <CodigoEstacion codigo={e.codigo} /> {e.zona}
        {!e.activa && ' · estación inactiva'}
      </span>
    ) : texto.trim() ? (
      'Elige una opción de la lista'
    ) : undefined;

  return (
    <Pagina angosta figura="Fig. 5" titulo="¿A dónde vas?" descripcion="Escribe el inicio del nombre de una estación y elige una sugerencia.">
      <form className="tarjeta buscar" onSubmit={calcular} noValidate aria-label="Buscar ruta">
        <div className="buscar__campos">
          <div className="buscar__parada buscar__parada--origen">
            <Autocompletar
              ref={refOrigen}
              marcador="A"
              etiqueta="Origen"
              placeholder="Ej. Terminal Occidente"
              valor={origen}
              alCambiar={setOrigen}
              alEnfocar={() => setEnfocado('origen')}
              ayuda={ayuda(origen, eOrigen)}
              invalido={!!error && !eOrigen}
            />
          </div>
          <div className="buscar__parada buscar__parada--destino">
            <Autocompletar
              ref={refDestino}
              marcador="B"
              etiqueta="Destino"
              placeholder="Ej. Metrocentro"
              valor={destino}
              alCambiar={setDestino}
              alEnfocar={() => setEnfocado('destino')}
              ayuda={ayuda(destino, eDestino)}
              invalido={!!error && !eDestino}
            />
            <Boton tamano="sm" variante="fantasma" icono="intercambiar" className="buscar__intercambiar" onClick={intercambiar} title="Intercambiar origen y destino">
              Intercambiar
            </Boton>
          </div>
        </div>
        {estaciones.error && <Aviso tono="error">No se pudieron cargar las estaciones. {estaciones.error.message}</Aviso>}
        {error && <Aviso tono="error">{error}</Aviso>}
        <Boton type="submit" variante="primario" tamano="lg" ancho iconoFinal="ruta" cargando={calculando}>
          {calculando ? 'Calculando…' : 'Calcular ruta'}
        </Boton>
      </form>

      <section className="recientes" aria-labelledby="recientes-titulo">
        <div className="recientes__cabeza">
          <h2 id="recientes-titulo">Búsquedas recientes</h2>
          <Boton tamano="sm" variante="fantasma" icono="recargar" onClick={recientes.recargar}>
            Actualizar
          </Boton>
        </div>
        <p className="texto-suave recientes__nota">
          Toca una para escribirla en <strong>{enfocado === 'origen' ? 'Origen' : 'Destino'}</strong>.
        </p>
        {recientes.cargando && !recientes.datos ? (
          <Esqueleto lineas={2} etiqueta="Cargando búsquedas recientes…" />
        ) : recientes.error ? (
          <Aviso tono="error">{recientes.error.message}</Aviso>
        ) : recientes.datos && recientes.datos.length > 0 ? (
          <ul className="lista-limpia recientes__lista">
            {recientes.datos.map((r, i) => (
              <li key={`${r}-${i}`}>
                <button type="button" className="reciente" onMouseDown={(e) => e.preventDefault()} onClick={() => usarReciente(r)}>
                  <Icono nombre="historial" tamano={16} />
                  {r}
                </button>
              </li>
            ))}
          </ul>
        ) : (
          <p className="texto-suave">Aún no tienes búsquedas. Escribe una estación arriba para empezar.</p>
        )}
      </section>
    </Pagina>
  );
}
