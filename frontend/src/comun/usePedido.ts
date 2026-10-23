import { useCallback, useEffect, useRef, useState } from 'react';
import { ApiError } from '../api/cliente';

export interface EstadoPedido<T> {
  datos: T | null;
  error: ApiError | null;
  cargando: boolean;
  recargar: () => void;
}

/**
 * Ejecuta un pedido al montar (y cada vez que cambian `deps`) y expone datos/error/cargando.
 * Ejemplo: const { datos, error, cargando } = usePedido(() => api.get<Estacion[]>('/estaciones'), []);
 */
export function usePedido<T>(fn: () => Promise<T>, deps: unknown[]): EstadoPedido<T> {
  const [datos, setDatos] = useState<T | null>(null);
  const [error, setError] = useState<ApiError | null>(null);
  const [cargando, setCargando] = useState(true);
  const [vuelta, setVuelta] = useState(0);
  const fnRef = useRef(fn);
  fnRef.current = fn;

  useEffect(() => {
    let vivo = true;
    setCargando(true);
    setError(null);
    fnRef
      .current()
      .then((d) => {
        if (vivo) setDatos(d);
      })
      .catch((e: unknown) => {
        if (!vivo) return;
        setDatos(null);
        setError(e instanceof ApiError ? e : new ApiError(0, e instanceof Error ? e.message : 'Error inesperado'));
      })
      .finally(() => {
        if (vivo) setCargando(false);
      });
    return () => {
      vivo = false;
    };
    // deps los decide quien llama.
  }, [...deps, vuelta]);

  const recargar = useCallback(() => setVuelta((v) => v + 1), []);
  return { datos, error, cargando, recargar };
}
