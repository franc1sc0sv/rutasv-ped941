// Combobox con sugerencias (patron ARIA combobox + listbox), generico.
// Quien lo usa decide de donde salen las sugerencias con `buscar(prefijo)`.
//   <Combobox etiqueta="Origen" valor={v} alCambiar={setV}
//             buscar={(p) => api.get<string[]>('/estaciones/sugerencias', { prefijo: p })} />
// Teclado: flechas para moverse, Enter elige, Escape cierra, Tab sale.
import { forwardRef, useEffect, useId, useRef, useState, type KeyboardEvent, type ReactNode } from 'react';
import { mensajeDe } from '../../api/cliente';

interface Props {
  etiqueta: string;
  valor: string;
  alCambiar: (v: string) => void;
  buscar: (prefijo: string) => Promise<string[]>;
  alEnfocar?: () => void;
  placeholder?: string;
  /** Texto bajo el campo, p. ej. el codigo resuelto. */
  ayuda?: ReactNode;
  invalido?: boolean;
  /** Elemento visual antes de la etiqueta (p. ej. el punto de origen o destino). */
  marcador?: ReactNode;
  /** Espera entre teclas antes de pedir sugerencias. */
  esperaMs?: number;
  className?: string;
}

export const Combobox = forwardRef<HTMLInputElement, Props>(function Combobox(
  { etiqueta, valor, alCambiar, buscar, alEnfocar, placeholder, ayuda, invalido, marcador, esperaMs = 200, className },
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
  const buscarRef = useRef(buscar);
  buscarRef.current = buscar;

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
        const lista = await buscarRef.current(prefijo);
        if (!vigente) return;
        setErrorRed(null);
        setSugerencias(lista);
        setActivo(-1);
        setAbierto(tieneFoco.current);
      } catch (e) {
        if (vigente) {
          setSugerencias([]);
          setErrorRed(`No se pudieron cargar sugerencias. ${mensajeDe(e)}`);
        }
      }
    }, esperaMs);
    return () => {
      vigente = false;
      window.clearTimeout(t);
    };
  }, [valor, esperaMs]);

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
    <div className={className ? `combobox ${className}` : 'combobox'}>
      <label className="campo__etiqueta combobox__etiqueta" htmlFor={id}>
        {marcador}
        {etiqueta}
      </label>
      <div className="combobox__caja">
        <input
          ref={ref}
          id={id}
          className="entrada combobox__entrada"
          type="text"
          role="combobox"
          aria-autocomplete="list"
          aria-expanded={mostrarLista}
          aria-controls={idLista}
          aria-activedescendant={mostrarLista && activo >= 0 ? `${id}-op-${activo}` : undefined}
          aria-describedby={ayuda || errorRed ? idAyuda : undefined}
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
        <ul id={idLista} role="listbox" aria-label={`Sugerencias para ${etiqueta}`} className="combobox__lista" hidden={!mostrarLista}>
          {sugerencias.map((s, i) => (
            <li
              key={s}
              id={`${id}-op-${i}`}
              role="option"
              aria-selected={i === activo}
              className={`combobox__opcion ${i === activo ? 'es-activa' : ''}`}
              onMouseDown={(e) => e.preventDefault()}
              onMouseEnter={() => setActivo(i)}
              onClick={() => elegir(s)}
            >
              <span className="combobox__punto" aria-hidden="true" />
              <Resaltar texto={s} prefijo={valor.trim()} />
            </li>
          ))}
        </ul>
        {sinResultados && (
          <div className="combobox__vacio" role="status">
            Ninguna estación empieza con “{valor.trim()}”.
          </div>
        )}
      </div>
      {(ayuda || errorRed) && (
        <p id={idAyuda} className={errorRed ? 'campo__error' : 'campo__ayuda'}>
          {errorRed ?? ayuda}
        </p>
      )}
    </div>
  );
});

function Resaltar({ texto, prefijo }: { texto: string; prefijo: string }) {
  if (prefijo && texto.toLowerCase().startsWith(prefijo.toLowerCase())) {
    return (
      <span>
        <strong>{texto.slice(0, prefijo.length)}</strong>
        {texto.slice(prefijo.length)}
      </span>
    );
  }
  return <span>{texto}</span>;
}
