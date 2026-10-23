// Campos de formulario: Campo (etiqueta + ayuda + error) envuelve a Entrada o Selector.
// Campo conecta solo el id, aria-describedby y aria-invalid; no hace falta pasarlos a mano.
//
//   <Campo etiqueta="Minutos" ayuda="Tiempo del tramo, mayor que 0" error={errores.minutos}>
//     <Entrada type="number" value={min} onChange={...} />
//   </Campo>
//   <Campo etiqueta="Origen"><Selector value={o} onChange={...}>{opciones}</Selector></Campo>
import {
  createContext,
  forwardRef,
  useContext,
  useId,
  type InputHTMLAttributes,
  type ReactNode,
  type SelectHTMLAttributes,
} from 'react';
import { Icono } from './Icono';

interface ValorCampo {
  id: string;
  descritoPor?: string;
  invalido: boolean;
}
const ContextoCampo = createContext<ValorCampo | null>(null);

interface PropsCampo {
  etiqueta: ReactNode;
  ayuda?: ReactNode;
  /** Texto de error. Si existe, el control queda aria-invalid y el texto se lee con el campo. */
  error?: ReactNode;
  /** Oculta la etiqueta a la vista (sigue para lectores de pantalla). */
  etiquetaOculta?: boolean;
  className?: string;
  children: ReactNode;
}

export function Campo({ etiqueta, ayuda, error, etiquetaOculta = false, className, children }: PropsCampo) {
  const id = useId();
  const idAyuda = `${id}-ayuda`;
  const idError = `${id}-error`;
  const descritoPor = [error ? idError : null, ayuda ? idAyuda : null].filter(Boolean).join(' ') || undefined;
  return (
    <ContextoCampo.Provider value={{ id, descritoPor, invalido: !!error }}>
      <div className={className ? `campo ${className}` : 'campo'}>
        <label htmlFor={id} className={etiquetaOculta ? 'oculto-visual' : 'campo__etiqueta'}>
          {etiqueta}
        </label>
        {children}
        {error && (
          <p id={idError} className="campo__error" role="alert">
            <Icono nombre="error" tamano={16} />
            {error}
          </p>
        )}
        {ayuda && (
          <p id={idAyuda} className="campo__ayuda">
            {ayuda}
          </p>
        )}
      </div>
    </ContextoCampo.Provider>
  );
}

function useCampo(idPropio?: string, descritoPropio?: string) {
  const c = useContext(ContextoCampo);
  return {
    id: idPropio ?? c?.id,
    'aria-describedby': [descritoPropio, c?.descritoPor].filter(Boolean).join(' ') || undefined,
    'aria-invalid': c?.invalido || undefined,
  };
}

export const Entrada = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement>>(function Entrada(
  { className, id, 'aria-describedby': descrito, ...resto },
  ref,
) {
  const a11y = useCampo(id, descrito);
  return <input ref={ref} className={className ? `entrada ${className}` : 'entrada'} {...a11y} {...resto} />;
});

export const Selector = forwardRef<HTMLSelectElement, SelectHTMLAttributes<HTMLSelectElement>>(function Selector(
  { className, id, children, 'aria-describedby': descrito, ...resto },
  ref,
) {
  const a11y = useCampo(id, descrito);
  return (
    <span className="selector">
      <select ref={ref} className={className ? `entrada selector__control ${className}` : 'entrada selector__control'} {...a11y} {...resto}>
        {children}
      </select>
      <svg className="selector__flecha" viewBox="0 0 24 24" width="16" height="16" aria-hidden="true" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <path d="m6 9 6 6 6-6" />
      </svg>
    </span>
  );
});

/** Casilla de verificacion con su texto. */
export function Casilla({ children, ...resto }: InputHTMLAttributes<HTMLInputElement> & { children: ReactNode }) {
  return (
    <label className="casilla">
      <input type="checkbox" className="casilla__control" {...resto} />
      <span className="casilla__marca" aria-hidden="true">
        <Icono nombre="check" tamano={14} />
      </span>
      <span>{children}</span>
    </label>
  );
}

/**
 * Control segmentado: un grupo de radios con aspecto de botones.
 * Con flechas se cambia de opcion (comportamiento nativo de radio).
 */
export function Segmentado<T extends string>({
  nombre,
  etiqueta,
  opciones,
  valor,
  alCambiar,
  etiquetaOculta = false,
}: {
  nombre: string;
  etiqueta: string;
  etiquetaOculta?: boolean;
  opciones: { valor: T; texto: ReactNode }[];
  valor: T;
  alCambiar: (v: T) => void;
}) {
  return (
    <fieldset className="segmentado">
      <legend className={etiquetaOculta ? 'oculto-visual' : 'campo__etiqueta'}>{etiqueta}</legend>
      <div className="segmentado__opciones">
        {opciones.map((o) => (
          <label key={o.valor} className="segmentado__opcion">
            <input type="radio" name={nombre} value={o.valor} checked={valor === o.valor} onChange={() => alCambiar(o.valor)} />
            <span>{o.texto}</span>
          </label>
        ))}
      </div>
    </fieldset>
  );
}
