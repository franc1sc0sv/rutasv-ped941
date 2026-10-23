// Insignias: estado corto y tipo de ruta.
//   <Insignia tono="exito">Activa</Insignia>
//   <InsigniaTipo tipo="EXPRES" />   -> rombo amarillo (como las lineas exprés del metro)
//   <InsigniaTipo tipo="NORMAL" />   -> circulo gris pizarra
//   <CodigoEstacion codigo="E01" />  -> placa del codigo de estacion
import type { ReactNode } from 'react';
import type { TipoRuta } from '../../api/tipos';

export type TonoInsignia = 'neutro' | 'primario' | 'exito' | 'peligro' | 'aviso' | 'info';

export function Insignia({ tono = 'neutro', children }: { tono?: TonoInsignia; children: ReactNode }) {
  return <span className={`insignia insignia--${tono}`}>{children}</span>;
}

export function InsigniaTipo({ tipo }: { tipo: TipoRuta }) {
  const expres = tipo === 'EXPRES';
  return (
    <span className={`insignia-tipo insignia-tipo--${tipo}`}>
      <svg viewBox="0 0 12 12" width="10" height="10" aria-hidden="true">
        {expres ? <path d="M6 0.6 11.4 6 6 11.4 0.6 6z" fill="currentColor" /> : <circle cx="6" cy="6" r="5" fill="currentColor" />}
      </svg>
      {expres ? 'Exprés' : 'Normal'}
    </span>
  );
}

export function CodigoEstacion({ codigo }: { codigo: string }) {
  return <span className="codigo-estacion">{codigo}</span>;
}
