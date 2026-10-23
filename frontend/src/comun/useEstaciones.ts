import { useMemo } from 'react';
import { api } from '../api/cliente';
import type { Estacion } from '../api/tipos';
import { usePedido } from './usePedido';

/** Carga GET /estaciones una vez y expone busquedas por codigo y por nombre. */
export function useEstaciones() {
  const pedido = usePedido(() => api.get<Estacion[]>('/estaciones'), []);
  const indices = useMemo(() => {
    const lista = pedido.datos ?? [];
    const porCodigo = new Map(lista.map((e) => [e.codigo, e]));
    const porNombre = new Map(lista.map((e) => [normalizar(e.nombre), e]));
    return { lista, porCodigo, porNombre };
  }, [pedido.datos]);

  /** Resuelve lo escrito (nombre exacto sin importar mayusculas/acentos, o codigo) a una estacion. */
  function resolver(texto: string): Estacion | undefined {
    const t = texto.trim();
    if (!t) return undefined;
    return indices.porNombre.get(normalizar(t)) ?? indices.porCodigo.get(t.toUpperCase());
  }

  return { ...pedido, ...indices, resolver };
}

export function normalizar(s: string): string {
  return s.normalize('NFD').replace(/[̀-ͯ]/g, '').trim().toLowerCase();
}
