// Aviso de "en obra" para endpoints que aun responden 501 { pendiente: true }.
// Cuando tu modulo este listo en el backend, deja de mostrarse solo: la pantalla
// solo lo pinta si el error tiene pendiente=true.
//   <Pendiente rol="Historial" endpoint="GET /api/historial" mensaje={error.message} />
//   <Pendiente enLinea rol="Paradas" mensaje={...} />   (dentro de un formulario o una fila)

import { Icono } from './Icono';

export type RolEquipo = 'Ruta' | 'Busqueda' | 'Terminal' | 'Historial' | 'Paradas';

const NOMBRE_ROL: Record<RolEquipo, string> = {
  Ruta: 'Ruta',
  Busqueda: 'Búsqueda',
  Terminal: 'Terminal',
  Historial: 'Historial',
  Paradas: 'Paradas',
};

interface Props {
  rol: RolEquipo;
  /** Mensaje que mando el backend, p. ej. "Pendiente: modulo Historial". */
  mensaje?: string;
  endpoint?: string;
  /** Variante compacta para usar dentro de una fila o formulario. */
  enLinea?: boolean;
}

export function Pendiente({ rol, mensaje, endpoint, enLinea = false }: Props) {
  const equipo = NOMBRE_ROL[rol];
  if (enLinea) {
    return (
      <div className="pendiente pendiente--linea" role="status">
        <span className="pendiente__chip">
          <Icono nombre="obra" tamano={14} />
          Pendiente
        </span>
        <span>
          Esta acción aún no está disponible: la conecta el equipo de <strong>{equipo}</strong>.
          {mensaje ? <span className="texto-suave"> Servidor: {mensaje}</span> : null}
        </span>
      </div>
    );
  }
  return (
    <section className="pendiente" role="status" aria-label={`Pantalla pendiente del equipo ${equipo}`}>
      <span className="pendiente__icono" aria-hidden="true">
        <Icono nombre="obra" tamano={28} />
      </span>
      <div className="pendiente__cuerpo">
        <span className="pendiente__chip">Pendiente</span>
        <h2 className="pendiente__titulo">Esta pantalla aún no tiene datos</h2>
        <p className="texto-suave">
          La interfaz ya está lista. Falta que el equipo de <strong>{equipo}</strong> conecte su módulo en el backend; cuando lo haga, los
          datos aparecerán aquí sin cambiar nada más.
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
