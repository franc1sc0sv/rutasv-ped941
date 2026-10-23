// Tipos que reflejan las formas JSON de docs/api.md (fuente de verdad).
// Si el backend cambia una forma, actualiza docs/api.md y este archivo en el mismo PR.

export type Rol = 'PASAJERO' | 'ADMIN';
export type TipoRuta = 'NORMAL' | 'EXPRES';

/** Respuesta de POST /auth/login, POST /auth/registro y GET /auth/yo. */
export interface Sesion {
  token: string;
  usuario: string;
  rol: Rol;
}

export interface Credenciales {
  usuario: string;
  contrasena: string;
}

/** Cuerpo de todo error: { error, pendiente }. */
export interface ErrorApi {
  error: string;
  pendiente: boolean;
}

export interface Estacion {
  codigo: string;
  nombre: string;
  zona: string;
  x: number;
  y: number;
  activa: boolean;
}

export interface Tramo {
  origen: string;
  destino: string;
  minutos: number;
  km: number;
}

/** GET /red */
export interface Red {
  estaciones: Estacion[];
  tramos: Tramo[];
}

/**
 * RutaCalculada tal como la guarda el core (GET /historial devuelve una lista de estas).
 * `tramos` aqui son los codigos de estacion en orden, de origen a destino.
 */
export interface RutaCalculada {
  origen: string;
  destino: string;
  tramos: string[];
  minutos: number;
  km: number;
  /** LocalDateTime ISO sin zona, p. ej. "2026-10-24T10:15:00". */
  fecha: string;
  tiempoCalculoNs: number;
  usuario: string;
}

/** GET /rutas: RutaCalculada + detalle de estaciones y tramos para la Fig. 6. */
export interface Ruta {
  origen: string;
  destino: string;
  minutos: number;
  km: number;
  fecha: string;
  tiempoCalculoNs: number;
  usuario: string;
  estaciones: Estacion[];
  tramos: Tramo[];
}

/** GET /estadisticas (ADMIN). */
export interface Estadisticas {
  totalBusquedas: number;
  rutaMasConsultada: string | null;
  tiempoPromedioNs: number;
  top5: string[];
}

export interface Unidad {
  id: string;
  tipoRuta: TipoRuta;
  /** El backend responde "07:00:00"; al registrar se envia "07:00". */
  horaProgramada: string;
}

export interface Pasajero {
  id: string;
  nombre: string;
}

export interface NuevoTramo {
  origen: string;
  destino: string;
  minutos: number;
  km: number;
}
