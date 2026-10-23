// Tarjeta para pantallas cuyo endpoint aun responde 501 { pendiente: true }.
// Cuando tu modulo este listo en el backend, esta tarjeta deja de mostrarse sola
// (la pantalla solo la pinta si el error tiene pendiente=true).

export type RolEquipo = 'Ruta' | 'Busqueda' | 'Terminal' | 'Historial' | 'Paradas';

interface Props {
  rol: RolEquipo;
  /** Mensaje que mando el backend, p. ej. "Pendiente: modulo Historial". */
  mensaje?: string;
  endpoint?: string;
  /** Variante compacta para usar dentro de una fila o formulario. */
  enLinea?: boolean;
}

export function Pendiente({ rol, mensaje, endpoint, enLinea = false }: Props) {
  if (enLinea) {
    return (
      <div className="pendiente pendiente--linea" role="status">
        <span className="pendiente__chip">Pendiente</span>
        <span>
          Lo conecta el rol <strong>{rol}</strong>
          {mensaje ? <span className="texto-suave"> · {mensaje}</span> : null}
        </span>
      </div>
    );
  }
  return (
    <section className="pendiente tarjeta" role="status" aria-label={`Pantalla pendiente del rol ${rol}`}>
      <div className="pendiente__icono" aria-hidden="true">
        <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M12 2v4M12 18v4M4.9 4.9l2.8 2.8M16.3 16.3l2.8 2.8M2 12h4M18 12h4M4.9 19.1l2.8-2.8M16.3 7.7l2.8-2.8" />
        </svg>
      </div>
      <div>
        <span className="pendiente__chip">Pendiente</span>
        <h2 className="pendiente__titulo">Esta pantalla la conecta el rol {rol}</h2>
        <p className="texto-suave">
          La interfaz ya está lista; falta que el módulo <strong>{rol}</strong> implemente su parte en el backend.
        </p>
        {(mensaje || endpoint) && (
          <dl className="pendiente__detalle">
            {endpoint && (
              <>
                <dt>Endpoint</dt>
                <dd>
                  <code>{endpoint}</code>
                </dd>
              </>
            )}
            {mensaje && (
              <>
                <dt>Respuesta del servidor</dt>
                <dd>{mensaje}</dd>
              </>
            )}
          </dl>
        )}
      </div>
    </section>
  );
}
