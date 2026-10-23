// Como llega una ruta a la pantalla Resultado de ruta sin volver a calcularla.
// GET /rutas ejecuta calcularRuta, y con RN-7 cada calculo queda en el historial: solo debe
// llamarse cuando el usuario pide una ruta (Buscar ruta o el boton "Calcular esta ruta").
import type { Red, Ruta, RutaCalculada } from '../../api/tipos';

/** `state` de la navegacion hacia /ruta. */
export interface EstadoRuta {
  /** Ruta recien calculada en Buscar ruta. */
  ruta?: Ruta;
  /** Entrada de Mi historial: se pinta sin recalcular. */
  entrada?: RutaCalculada;
}

export function urlRuta(origen: string, destino: string): string {
  return `/ruta?origen=${encodeURIComponent(origen)}&destino=${encodeURIComponent(destino)}`;
}

/**
 * Arma el detalle de una entrada del historial con los datos de GET /red (que no registra nada):
 * los nombres de las estaciones y los minutos y km de cada tramo. Devuelve null si la red ya no
 * tiene alguna estacion o tramo de esa ruta.
 */
export function rutaDesdeEntrada(entrada: RutaCalculada, red: Red): Ruta | null {
  const porCodigo = new Map(red.estaciones.map((e) => [e.codigo, e]));
  const estaciones = entrada.tramos.map((c) => porCodigo.get(c));
  if (estaciones.some((e) => !e)) return null;
  const tramos = [];
  for (let i = 1; i < entrada.tramos.length; i++) {
    const t = red.tramos.find((x) => x.origen === entrada.tramos[i - 1] && x.destino === entrada.tramos[i]);
    if (!t) return null;
    tramos.push(t);
  }
  return { ...entrada, estaciones: estaciones as NonNullable<(typeof estaciones)[number]>[], tramos };
}
