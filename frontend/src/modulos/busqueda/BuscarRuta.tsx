// Buscar ruta (Fig. 5). Rol dueno: Busqueda.
// Endpoints: GET /estaciones/sugerencias?prefijo=, GET /busquedas/recientes, GET /estaciones (nombre -> codigo).
// Al calcular navega a /ruta?origen=E01&destino=E04 (pantalla del rol Ruta).
import { useRef, useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../../api/cliente';
import { MensajeError } from '../../comun/MensajeError';
import { Pagina } from '../../comun/Pagina';
import { usePedido } from '../../comun/usePedido';
import { useEstaciones } from '../../comun/useEstaciones';
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

  function calcular(ev: FormEvent) {
    ev.preventDefault();
    setError(null);
    if (!eOrigen || !eDestino) {
      const falta = !eOrigen ? 'origen' : 'destino';
      setError(`Elige una estación de ${falta} de la lista de sugerencias.`);
      (falta === 'origen' ? refOrigen : refDestino).current?.focus();
      return;
    }
    if (eOrigen.codigo === eDestino.codigo) {
      setError('El origen y el destino deben ser estaciones distintas.');
      return;
    }
    navegar(`/ruta?origen=${encodeURIComponent(eOrigen.codigo)}&destino=${encodeURIComponent(eDestino.codigo)}`);
  }

  const ayuda = (texto: string, e: typeof eOrigen) =>
    e ? (
      <>
        <span className="codigo-chip">{e.codigo}</span> {e.zona}
        {!e.activa && ' · estación inactiva'}
      </>
    ) : texto.trim() ? (
      'Elige una opción de la lista'
    ) : undefined;

  return (
    <Pagina figura="Fig. 5 · Búsqueda" titulo="¿A dónde vas?" descripcion="Escribe el inicio del nombre de una estación y elige una sugerencia.">
      <form className="tarjeta tarjeta--relleno buscar" onSubmit={calcular} noValidate>
        <div className="buscar__campos">
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
          <button type="button" className="boton boton--icono buscar__intercambiar" onClick={intercambiar} aria-label="Intercambiar origen y destino" title="Intercambiar origen y destino">
            <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M7 4v16M7 20l-3-3M7 20l3-3M17 20V4M17 4l-3 3M17 4l3 3" />
            </svg>
          </button>
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
        </div>
        {estaciones.error && <MensajeError mensaje={`No se pudieron cargar las estaciones: ${estaciones.error.message}`} />}
        {error && <MensajeError mensaje={error} />}
        <button type="submit" className="boton boton--primario boton--ancho buscar__calcular">
          Calcular ruta
        </button>
      </form>

      <section className="tarjeta tarjeta--relleno" aria-labelledby="recientes-titulo">
        <div className="tarjeta__titulo">
          <h2 id="recientes-titulo">Búsquedas recientes</h2>
          <button type="button" className="boton boton--chico boton--fantasma" onClick={recientes.recargar}>
            Actualizar
          </button>
        </div>
        <p className="texto-suave buscar__nota">
          Toca una para llenar el campo <strong>{enfocado === 'origen' ? 'Origen' : 'Destino'}</strong>.
        </p>
        {recientes.cargando && !recientes.datos ? (
          <p className="texto-suave">Cargando…</p>
        ) : recientes.error ? (
          <MensajeError mensaje={recientes.error.message} />
        ) : recientes.datos && recientes.datos.length > 0 ? (
          <ul className="lista-limpia recientes">
            {recientes.datos.map((r, i) => (
              <li key={`${r}-${i}`}>
                <button type="button" className="reciente" onMouseDown={(e) => e.preventDefault()} onClick={() => usarReciente(r)}>
                  <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                    <path d="M3 12a9 9 0 1 0 3-6.7L3 8M3 3v5h5M12 7v5l3 2" />
                  </svg>
                  {r}
                </button>
              </li>
            ))}
          </ul>
        ) : (
          <p className="texto-suave">Aún no tienes búsquedas. Empieza escribiendo una estación.</p>
        )}
      </section>
    </Pagina>
  );
}
