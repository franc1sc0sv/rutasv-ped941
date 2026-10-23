import type { ReactNode } from 'react';

type Tono = 'error' | 'aviso' | 'exito' | 'info';

/** Mensaje en linea. Por defecto es de error (role="alert" para lectores de pantalla). */
export function MensajeError({ mensaje, tono = 'error', children }: { mensaje: ReactNode; tono?: Tono; children?: ReactNode }) {
  const icono = tono === 'exito' ? '✓' : tono === 'info' ? 'i' : '!';
  return (
    <div className={`mensaje mensaje--${tono}`} role={tono === 'error' ? 'alert' : 'status'}>
      <span className="mensaje__icono" aria-hidden="true">
        {icono}
      </span>
      <div className="mensaje__texto">
        {mensaje}
        {children}
      </div>
    </div>
  );
}
