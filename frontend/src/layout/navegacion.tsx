// Entradas de menu por rol (RN-1). Para agregar una pantalla: agrega su ruta en App.tsx y una entrada aqui.
import type { ReactNode } from 'react';

export interface Entrada {
  a: string;
  texto: string;
  corto: string;
  icono: ReactNode;
}

const trazo = { fill: 'none', stroke: 'currentColor', strokeWidth: 2, strokeLinecap: 'round', strokeLinejoin: 'round' } as const;
const Icono = ({ children }: { children: ReactNode }) => (
  <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true" {...trazo}>
    {children}
  </svg>
);

export const ENTRADAS_PASAJERO: Entrada[] = [
  {
    a: '/buscar',
    texto: 'Buscar ruta',
    corto: 'Buscar',
    icono: (
      <Icono>
        <circle cx="11" cy="11" r="7" />
        <path d="m20 20-3.5-3.5" />
      </Icono>
    ),
  },
  {
    a: '/ruta',
    texto: 'Resultado de ruta',
    corto: 'Ruta',
    icono: (
      <Icono>
        <circle cx="6" cy="19" r="2" />
        <circle cx="18" cy="5" r="2" />
        <path d="M8 19h8a3 3 0 0 0 0-6H8a3 3 0 0 1 0-6h8" />
      </Icono>
    ),
  },
  {
    a: '/historial',
    texto: 'Mi historial',
    corto: 'Historial',
    icono: (
      <Icono>
        <path d="M3 12a9 9 0 1 0 3-6.7L3 8" />
        <path d="M3 3v5h5M12 7v5l3 2" />
      </Icono>
    ),
  },
];

export const ENTRADAS_ADMIN: Entrada[] = [
  {
    a: '/admin/red',
    texto: 'Red vial',
    corto: 'Red',
    icono: (
      <Icono>
        <circle cx="5" cy="6" r="2" />
        <circle cx="19" cy="6" r="2" />
        <circle cx="12" cy="18" r="2" />
        <path d="M7 6h10M6 8l5 8.5M18 8l-5 8.5" />
      </Icono>
    ),
  },
  {
    a: '/admin/paradas',
    texto: 'Registro de paradas',
    corto: 'Paradas',
    icono: (
      <Icono>
        <path d="M12 21s-7-6.2-7-11.5A7 7 0 0 1 19 9.5C19 14.8 12 21 12 21z" />
        <circle cx="12" cy="9.5" r="2.5" />
      </Icono>
    ),
  },
  {
    a: '/admin/despacho',
    texto: 'Despacho',
    corto: 'Despacho',
    icono: (
      <Icono>
        <rect x="4" y="3" width="16" height="14" rx="3" />
        <path d="M4 11h16M8 21l1-4M16 21l-1-4" />
        <circle cx="8" cy="14" r=".6" />
        <circle cx="16" cy="14" r=".6" />
      </Icono>
    ),
  },
  {
    a: '/admin/historial-global',
    texto: 'Historial global',
    corto: 'Global',
    icono: (
      <Icono>
        <path d="M4 20V10M10 20V4M16 20v-7M22 20H2" />
      </Icono>
    ),
  },
];
