// Boton y BotonEnlace. Una sola accion primaria por zona de la pantalla.
// Variantes: primario (la accion principal) · secundario (por defecto) · fantasma (acciones menores) · peligro.
// Uso: <Boton variante="primario" icono="mas" cargando={enviando}>Agregar tramo</Boton>
//      <BotonEnlace a="/buscar" icono="atras">Nueva búsqueda</BotonEnlace>
import { forwardRef, type ButtonHTMLAttributes, type ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { Icono, type NombreIcono } from './Icono';

export type VarianteBoton = 'primario' | 'secundario' | 'fantasma' | 'peligro';
export type TamanoBoton = 'md' | 'sm' | 'lg';

interface Comunes {
  variante?: VarianteBoton;
  tamano?: TamanoBoton;
  /** Ocupa todo el ancho disponible. */
  ancho?: boolean;
  icono?: NombreIcono;
  /** Icono a la derecha del texto (p. ej. avanzar). */
  iconoFinal?: NombreIcono;
  /** Solo icono: exige aria-label. */
  soloIcono?: boolean;
}

export function clasesBoton({ variante = 'secundario', tamano = 'md', ancho, soloIcono }: Comunes, extra?: string): string {
  return ['boton', `boton--${variante}`, tamano !== 'md' && `boton--${tamano}`, ancho && 'boton--ancho', soloIcono && 'boton--icono', extra]
    .filter(Boolean)
    .join(' ');
}

type PropsBoton = Comunes &
  ButtonHTMLAttributes<HTMLButtonElement> & {
    /** Muestra un indicador y deshabilita el boton mientras se envia. */
    cargando?: boolean;
  };

export const Boton = forwardRef<HTMLButtonElement, PropsBoton>(function Boton(
  { variante, tamano, ancho, icono, iconoFinal, soloIcono, cargando = false, className, children, disabled, type = 'button', ...resto },
  ref,
) {
  return (
    <button
      ref={ref}
      type={type}
      className={clasesBoton({ variante, tamano, ancho, soloIcono }, className)}
      disabled={disabled || cargando}
      aria-busy={cargando || undefined}
      {...resto}
    >
      {cargando ? <span className="boton__giro" aria-hidden="true" /> : icono && <Icono nombre={icono} tamano={tamano === 'sm' ? 16 : 18} />}
      {children}
      {iconoFinal && <Icono nombre={iconoFinal} tamano={tamano === 'sm' ? 16 : 18} />}
    </button>
  );
});

type PropsEnlace = Comunes & { a: string; children: ReactNode; className?: string; 'aria-label'?: string };

export function BotonEnlace({ a, variante, tamano, ancho, icono, iconoFinal, soloIcono, className, children, ...resto }: PropsEnlace) {
  return (
    <Link to={a} className={clasesBoton({ variante, tamano, ancho, soloIcono }, className)} {...resto}>
      {icono && <Icono nombre={icono} tamano={tamano === 'sm' ? 16 : 18} />}
      {children}
      {iconoFinal && <Icono nombre={iconoFinal} tamano={tamano === 'sm' ? 16 : 18} />}
    </Link>
  );
}
