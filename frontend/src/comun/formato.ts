/** Nanosegundos -> "0.774 ms" (3 decimales). */
export function nsAMs(ns: number): string {
  return `${(ns / 1_000_000).toFixed(3)} ms`;
}

export function km(valor: number): string {
  return `${valor.toLocaleString('es-SV', { minimumFractionDigits: 1, maximumFractionDigits: 2 })} km`;
}

/** "2026-10-24T10:15:00.123" (LocalDateTime sin zona) -> "24 oct 2026, 10:15". */
export function fecha(iso: string): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  return d.toLocaleString('es-SV', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

/** "07:00:00" -> "07:00". */
export function hora(h: string): string {
  return h.length >= 5 ? h.slice(0, 5) : h;
}
