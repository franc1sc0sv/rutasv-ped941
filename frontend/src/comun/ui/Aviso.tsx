// Aviso en linea: resultado de una accion o error de carga, junto a lo que lo provoco.
// tono "error" usa role="alert" (se anuncia de inmediato); los demas, role="status".
//   <Aviso tono="exito">Tramo agregado.</Aviso>
//   <Aviso tono="error" titulo="No se pudo guardar">{mensaje}</Aviso>
import type { ReactNode } from 'react';
import { Icono, type NombreIcono } from './Icono';

export type TonoAviso = 'error' | 'aviso' | 'exito' | 'info';

const ICONO: Record<TonoAviso, NombreIcono> = { error: 'error', aviso: 'alerta', exito: 'check', info: 'info' };

interface Props {
  tono?: TonoAviso;
  titulo?: ReactNode;
  children?: ReactNode;
  /** Accion opcional a la derecha (p. ej. "Reintentar"). */
  accion?: ReactNode;
  className?: string;
}

export function Aviso({ tono = 'error', titulo, children, accion, className }: Props) {
  return (
    <div className={`aviso aviso--${tono}${className ? ` ${className}` : ''}`} role={tono === 'error' ? 'alert' : 'status'}>
      <Icono nombre={ICONO[tono]} tamano={20} className="aviso__icono" />
      <div className="aviso__texto">
        {titulo && <p className="aviso__titulo">{titulo}</p>}
        {children && <div>{children}</div>}
      </div>
      {accion && <div className="aviso__accion">{accion}</div>}
    </div>
  );
}
