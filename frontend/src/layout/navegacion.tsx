// Entradas de menu por rol (RN-1). Para agregar una pantalla: agrega su ruta en App.tsx y una entrada aqui.
import type { NombreIcono } from '../comun/ui';

export interface Entrada {
  a: string;
  texto: string;
  /** Texto corto para la barra inferior del pasajero. */
  corto: string;
  icono: NombreIcono;
}

export const ENTRADAS_PASAJERO: Entrada[] = [
  { a: '/buscar', texto: 'Buscar ruta', corto: 'Buscar', icono: 'buscar' },
  { a: '/ruta', texto: 'Resultado de ruta', corto: 'Ruta', icono: 'ruta' },
  { a: '/historial', texto: 'Mi historial', corto: 'Historial', icono: 'historial' },
];

export const ENTRADAS_ADMIN: Entrada[] = [
  { a: '/admin/red', texto: 'Red vial', corto: 'Red', icono: 'red' },
  { a: '/admin/paradas', texto: 'Registro de paradas', corto: 'Paradas', icono: 'parada' },
  { a: '/admin/despacho', texto: 'Despacho', corto: 'Despacho', icono: 'bus' },
  { a: '/admin/historial-global', texto: 'Historial global', corto: 'Global', icono: 'estadisticas' },
];
