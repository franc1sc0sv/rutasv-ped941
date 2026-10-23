// Estados de carga.
// - <Cargando texto="Calculando ruta…" />: indicador con texto (acciones cortas, pantalla completa).
// - <Esqueleto lineas={3} />: bloques grises con la forma del contenido (listas, tablas, tarjetas).
//   Usa esqueleto cuando sabes que forma tendra el contenido; evita saltos de layout.

export function Cargando({ texto = 'Cargando…', pantallaCompleta = false }: { texto?: string; pantallaCompleta?: boolean }) {
  return (
    <div className={pantallaCompleta ? 'cargando cargando--pantalla' : 'cargando'} role="status" aria-live="polite">
      <span className="cargando__giro" aria-hidden="true" />
      <span>{texto}</span>
    </div>
  );
}

interface PropsEsqueleto {
  /** Numero de lineas de texto. */
  lineas?: number;
  /** Bloque alto (mapa, grafico) en px. */
  alto?: number;
  /** Texto para lectores de pantalla. */
  etiqueta?: string;
}

export function Esqueleto({ lineas = 3, alto, etiqueta = 'Cargando…' }: PropsEsqueleto) {
  return (
    <div className="esqueleto" role="status" aria-live="polite">
      <span className="oculto-visual">{etiqueta}</span>
      {alto ? (
        <span className="esqueleto__bloque" style={{ height: alto }} aria-hidden="true" />
      ) : (
        Array.from({ length: lineas }, (_, i) => <span key={i} className="esqueleto__linea" aria-hidden="true" />)
      )}
    </div>
  );
}
