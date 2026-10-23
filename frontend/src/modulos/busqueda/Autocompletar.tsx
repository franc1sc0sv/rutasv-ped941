// Campo con autocompletado (patron ARIA combobox + listbox). Rol: Busqueda.
// Endpoint: GET /estaciones/sugerencias?prefijo=... (con espera de 200 ms entre teclas).
import { forwardRef, useEffect, useId, useRef, useState, type KeyboardEvent, type ReactNode } from 'react';
import { api } from '../../api/cliente';

interface Props {
  etiqueta: string;
  valor: string;
  alCambiar: (v: string) => void;
  alEnfocar?: () => void;
  placeholder?: string;
  /** Texto bajo el campo, p. ej. el codigo resuelto ("E01 · Occidente"). */
  ayuda?: ReactNode;
  invalido?: boolean;
  marcador: 'A' | 'B';
}

const ESPERA_MS = 200;

export const Autocompletar = forwardRef<HTMLInputElement, Props>(function Autocompletar(
  { etiqueta, valor, alCambiar, alEnfocar, placeholder, ayuda, invalido, marcador },
  ref,
) {
  const id = useId();
  const idLista = `${id}-lista`;
  const idAyuda = `${id}-ayuda`;
  const [sugerencias, setSugerencias] = useState<string[]>([]);
  const [abierto, setAbierto] = useState(false);
  const [activo, setActivo] = useState(-1);
  const [errorRed, setErrorRed] = useState<string | null>(null);
  // Evita volver a pedir sugerencias justo despues de elegir una.
  const omitirSiguiente = useRef(false);
  const tieneFoco = useRef(false);

  useEffect(() => {
    if (omitirSiguiente.current) {
      omitirSiguiente.current = false;
      return;
    }
    const prefijo = valor.trim();
    if (!prefijo) {
      setSugerencias([]);
      setAbierto(false);
      return;
    }
    let vigente = true;
    const t = window.setTimeout(async () => {
      try {
        const lista = await api.get<string[]>('/estaciones/sugerencias', { prefijo });
        if (!vigente) return;
        setErrorRed(null);
        setSugerencias(lista);
        setActivo(-1);
        setAbierto(tieneFoco.current);
      } catch (e) {
        if (vigente) {
          setSugerencias([]);
          setErrorRed(e instanceof Error ? e.message : 'No se pudieron cargar sugerencias');
        }
      }
    }, ESPERA_MS);
    return () => {
      vigente = false;
      window.clearTimeout(t);
    };
  }, [valor]);

  function elegir(nombre: string) {
    omitirSiguiente.current = true;
    alCambiar(nombre);
    setAbierto(false);
    setActivo(-1);
  }

  function alTeclear(ev: KeyboardEvent<HTMLInputElement>) {
    switch (ev.key) {
      case 'ArrowDown':
        ev.preventDefault();
        if (!abierto && sugerencias.length) setAbierto(true);
        setActivo((i) => (sugerencias.length ? (i + 1) % sugerencias.length : -1));
        break;
      case 'ArrowUp':
        ev.preventDefault();
        if (!abierto && sugerencias.length) setAbierto(true);
        setActivo((i) => (sugerencias.length ? (i <= 0 ? sugerencias.length - 1 : i - 1) : -1));
        break;
      case 'Enter':
        if (abierto && activo >= 0 && sugerencias[activo]) {
          ev.preventDefault();
          elegir(sugerencias[activo]);
        }
        break;
      case 'Escape':
        if (abierto) {
          ev.preventDefault();
          setAbierto(false);
          setActivo(-1);
        }
        break;
      case 'Tab':
        setAbierto(false);
        break;
    }
  }

  const mostrarLista = abierto && sugerencias.length > 0;
  const sinResultados = abierto && sugerencias.length === 0 && valor.trim() !== '' && !errorRed;

  return (
    <div className="auto">
      <label className="auto__etiqueta" htmlFor={id}>
        <span className={`auto__marcador auto__marcador--${marcador}`} aria-hidden="true">
          {marcador}
        </span>
        {etiqueta}
      </label>
      <div className="auto__caja">
        <input
          ref={ref}
          id={id}
          className="entrada auto__entrada"
          type="text"
          role="combobox"
          aria-autocomplete="list"
          aria-expanded={mostrarLista}
          aria-controls={idLista}
          aria-activedescendant={mostrarLista && activo >= 0 ? `${id}-op-${activo}` : undefined}
          aria-describedby={ayuda ? idAyuda : undefined}
          aria-invalid={invalido || undefined}
          autoComplete="off"
          spellCheck={false}
          placeholder={placeholder}
          value={valor}
          onChange={(e) => alCambiar(e.target.value)}
          onKeyDown={alTeclear}
          onFocus={() => {
            tieneFoco.current = true;
            alEnfocar?.();
            if (sugerencias.length && valor.trim()) setAbierto(true);
          }}
          onBlur={() => {
            tieneFoco.current = false;
            // Espera un instante para que el clic en una opcion alcance a registrarse.
            window.setTimeout(() => setAbierto(false), 120);
          }}
        />
        <ul id={idLista} role="listbox" aria-label={`Sugerencias para ${etiqueta}`} className="auto__lista" hidden={!mostrarLista}>
          {sugerencias.map((s, i) => (
            <li
              key={s}
              id={`${id}-op-${i}`}
              role="option"
              aria-selected={i === activo}
              className={`auto__opcion ${i === activo ? 'es-activa' : ''}`}
              onMouseDown={(e) => e.preventDefault()}
              onMouseEnter={() => setActivo(i)}
              onClick={() => elegir(s)}
            >
              <Resaltar texto={s} prefijo={valor.trim()} />
            </li>
          ))}
        </ul>
        {sinResultados && <div className="auto__vacio">Sin coincidencias para “{valor.trim()}”</div>}
      </div>
      {(ayuda || errorRed) && (
        <small id={idAyuda} className={errorRed ? 'auto__ayuda auto__ayuda--error' : 'auto__ayuda texto-suave'}>
          {errorRed ?? ayuda}
        </small>
      )}
    </div>
  );
});

function Resaltar({ texto, prefijo }: { texto: string; prefijo: string }) {
  if (prefijo && texto.toLowerCase().startsWith(prefijo.toLowerCase())) {
    return (
      <>
        <strong>{texto.slice(0, prefijo.length)}</strong>
        {texto.slice(prefijo.length)}
      </>
    );
  }
  return <>{texto}</>;
}
