// Set de iconos SVG en linea (trazo 2 px, rejilla de 24). Sin emoji ni librerias.
// Uso: <Icono nombre="buscar" />  ·  tamano por defecto 20 px, hereda el color del texto.
// Para agregar uno: suma su nombre a NombreIcono y su trazo en TRAZOS (24x24, sin relleno).
import type { ReactNode } from 'react';

export type NombreIcono =
  | 'buscar'
  | 'ruta'
  | 'historial'
  | 'red'
  | 'parada'
  | 'bus'
  | 'estadisticas'
  | 'intercambiar'
  | 'menu'
  | 'cerrar'
  | 'salir'
  | 'check'
  | 'alerta'
  | 'info'
  | 'error'
  | 'recargar'
  | 'atras'
  | 'mas'
  | 'reloj'
  | 'obra'
  | 'editar'
  | 'encendido'
  | 'persona'
  | 'despachar';

const TRAZOS: Record<NombreIcono, ReactNode> = {
  buscar: (
    <>
      <circle cx="11" cy="11" r="6.5" />
      <path d="m20 20-4.4-4.4" />
    </>
  ),
  ruta: (
    <>
      <circle cx="6" cy="18" r="2.2" />
      <circle cx="18" cy="6" r="2.2" />
      <path d="M8.2 18H15a3 3 0 0 0 0-6H9a3 3 0 0 1 0-6h6.8" />
    </>
  ),
  historial: (
    <>
      <path d="M3.5 12a8.5 8.5 0 1 0 2.6-6.1L3.5 8.5" />
      <path d="M3.5 3.5v5h5M12 7.5V12l3 2" />
    </>
  ),
  red: (
    <>
      <circle cx="5" cy="6" r="2" />
      <circle cx="19" cy="6" r="2" />
      <circle cx="12" cy="18" r="2" />
      <path d="M7 6h10M6 7.8l5 8.4M18 7.8l-5 8.4" />
    </>
  ),
  parada: (
    <>
      <path d="M8 21h8M12 21V10" />
      <rect x="6" y="3" width="12" height="7" rx="1" />
    </>
  ),
  bus: (
    <>
      <rect x="4" y="3" width="16" height="14" rx="2" />
      <path d="M4 11h16M7 20v-3M17 20v-3" />
      <path d="M8 14h.01M16 14h.01" />
    </>
  ),
  estadisticas: <path d="M4 20V11M10 20V4M16 20v-6M2.5 20h19" />,
  intercambiar: <path d="M7 4v16m0 0-3-3m3 3 3-3M17 20V4m0 0-3 3m3-3 3 3" />,
  menu: <path d="M4 6h16M4 12h16M4 18h16" />,
  cerrar: <path d="M6 6l12 12M18 6 6 18" />,
  salir: (
    <>
      <path d="M14 4h4a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-4" />
      <path d="M10 16l-4-4 4-4M6 12h10" />
    </>
  ),
  check: <path d="m5 12.5 4.5 4.5L19 7.5" />,
  alerta: (
    <>
      <path d="M12 3.5 2.5 20h19L12 3.5z" />
      <path d="M12 10v4.5M12 17.2v.01" />
    </>
  ),
  info: (
    <>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 11v5.5M12 7.8v.01" />
    </>
  ),
  error: (
    <>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7.5V13M12 16.2v.01" />
    </>
  ),
  recargar: (
    <>
      <path d="M20 12a8 8 0 1 1-2.4-5.7L20 8.5" />
      <path d="M20 3.5v5h-5" />
    </>
  ),
  atras: <path d="M19 12H5m0 0 6-6m-6 6 6 6" />,
  mas: <path d="M12 5v14M5 12h14" />,
  reloj: (
    <>
      <circle cx="12" cy="12" r="8.5" />
      <path d="M12 7.5V12l3 2" />
    </>
  ),
  obra: (
    <>
      <path d="M3 20h18M5 20l3.5-14h7L19 20" />
      <path d="M7 14h10M6.2 17h11.6M8 10h8" />
    </>
  ),
  editar: (
    <>
      <path d="M4 20h4L19 9a2.8 2.8 0 0 0-4-4L4 16v4z" />
      <path d="m13.5 6.5 4 4" />
    </>
  ),
  encendido: (
    <>
      <path d="M12 3v8" />
      <path d="M6.3 6.8a8 8 0 1 0 11.4 0" />
    </>
  ),
  persona: (
    <>
      <circle cx="12" cy="8" r="3.8" />
      <path d="M4.5 20.5a7.5 7.5 0 0 1 15 0" />
    </>
  ),
  despachar: (
    <>
      <path d="M3 12h13m0 0-5-5m5 5-5 5" />
      <path d="M20 5v14" />
    </>
  ),
};

interface Props {
  nombre: NombreIcono;
  tamano?: number;
  /** Si el icono es el unico contenido de un control, pasa la etiqueta en el control (aria-label), no aqui. */
  className?: string;
}

export function Icono({ nombre, tamano = 20, className }: Props) {
  return (
    <svg
      className={className ? `icono ${className}` : 'icono'}
      viewBox="0 0 24 24"
      width={tamano}
      height={tamano}
      fill="none"
      stroke="currentColor"
      strokeWidth={2}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      focusable="false"
    >
      {TRAZOS[nombre]}
    </svg>
  );
}
