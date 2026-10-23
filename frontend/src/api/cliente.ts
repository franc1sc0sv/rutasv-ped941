// Envoltorio de fetch para la API de RutaSV (docs/api.md).
// - Antepone /api (Vite lo reenvia al backend en el puerto 7070).
// - Agrega Authorization: Bearer <token> si hay sesion.
// - Convierte cualquier respuesta no 2xx en ApiError(status, mensaje, pendiente).
//
// Uso tipico en una pantalla:
//   const rutas = await api.get<Ruta>('/rutas', { origen: 'E01', destino: 'E04' });
//   await api.post('/red/tramos', { origen, destino, minutos, km });

export class ApiError extends Error {
  readonly status: number;
  readonly pendiente: boolean;

  constructor(status: number, mensaje: string, pendiente = false) {
    super(mensaje);
    this.name = 'ApiError';
    this.status = status;
    this.pendiente = pendiente;
  }
}

const BASE = '/api';

let tokenActual: string | null = null;
let alExpirarSesion: (() => void) | null = null;

/** Lo llama SesionContext; las pantallas no necesitan tocarlo. */
export function configurarToken(token: string | null): void {
  tokenActual = token;
}

/** SesionContext registra aqui que hacer ante un 401 con sesion abierta. */
export function alPerderSesion(fn: (() => void) | null): void {
  alExpirarSesion = fn;
}

type Parametros = Record<string, string | number | boolean | undefined | null>;

function armarUrl(ruta: string, parametros?: Parametros): string {
  let url = BASE + ruta;
  if (parametros) {
    const q = new URLSearchParams();
    for (const [k, v] of Object.entries(parametros)) {
      if (v !== undefined && v !== null) q.set(k, String(v));
    }
    const s = q.toString();
    if (s) url += (url.includes('?') ? '&' : '?') + s;
  }
  return url;
}

async function pedir<T>(metodo: string, ruta: string, cuerpo?: unknown, parametros?: Parametros): Promise<T> {
  const encabezados: Record<string, string> = { Accept: 'application/json' };
  if (cuerpo !== undefined) encabezados['Content-Type'] = 'application/json';
  if (tokenActual) encabezados.Authorization = `Bearer ${tokenActual}`;

  let resp: Response;
  try {
    resp = await fetch(armarUrl(ruta, parametros), {
      method: metodo,
      headers: encabezados,
      body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
    });
  } catch {
    throw new ApiError(0, 'No se pudo conectar con el servidor. Revisa que la API esté corriendo en el puerto 7070.');
  }

  const texto = await resp.text();
  let datos: unknown = undefined;
  if (texto) {
    try {
      datos = JSON.parse(texto);
    } catch {
      datos = texto;
    }
  }

  if (!resp.ok) {
    let mensaje = `Error ${resp.status}`;
    let pendiente = false;
    if (datos && typeof datos === 'object' && 'error' in datos) {
      const e = datos as { error?: unknown; pendiente?: unknown };
      if (typeof e.error === 'string' && e.error) mensaje = e.error;
      pendiente = e.pendiente === true;
    } else if (resp.status === 502 || resp.status === 504) {
      mensaje = 'El servidor de la API no responde (puerto 7070). Inténtalo de nuevo en un momento.';
    }
    if (resp.status === 401 && tokenActual && alExpirarSesion) alExpirarSesion();
    throw new ApiError(resp.status, mensaje, pendiente);
  }

  return datos as T;
}

export const api = {
  get: <T>(ruta: string, parametros?: Parametros) => pedir<T>('GET', ruta, undefined, parametros),
  post: <T = void>(ruta: string, cuerpo?: unknown) => pedir<T>('POST', ruta, cuerpo),
  put: <T = void>(ruta: string, cuerpo?: unknown) => pedir<T>('PUT', ruta, cuerpo),
  patch: <T = void>(ruta: string, cuerpo?: unknown) => pedir<T>('PATCH', ruta, cuerpo),
};

/** Mensaje legible para cualquier error atrapado en un catch. */
export function mensajeDe(e: unknown): string {
  if (e instanceof ApiError) return e.message;
  if (e instanceof Error) return e.message;
  return 'Ocurrió un error inesperado. Inténtalo de nuevo.';
}
