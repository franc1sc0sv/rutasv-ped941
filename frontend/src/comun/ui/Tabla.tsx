// Tabla densa para pantallas de administracion. Desplaza en horizontal dentro de su caja
// (nunca la pagina). Usa <th scope="col">, clase "num" para cifras alineadas a la derecha.
//   <Tabla etiqueta="Estaciones de la red">
//     <thead>…</thead>
//     <tbody>{filas.length ? filas.map(...) : <FilaVacia columnas={5}>Sin estaciones.</FilaVacia>}</tbody>
//   </Tabla>
import type { ReactNode } from 'react';

interface Props {
  /** Nombre accesible de la tabla (se lee como caption oculto). */
  etiqueta: string;
  /** Muestra la etiqueta como caption visible. */
  captionVisible?: boolean;
  className?: string;
  children: ReactNode;
}

export function Tabla({ etiqueta, captionVisible = false, className, children }: Props) {
  return (
    <div className={className ? `tabla-caja ${className}` : 'tabla-caja'} role="region" aria-label={etiqueta} tabIndex={0}>
      <table className="tabla">
        <caption className={captionVisible ? 'tabla__caption' : 'oculto-visual'}>{etiqueta}</caption>
        {children}
      </table>
    </div>
  );
}

export function FilaVacia({ columnas, children }: { columnas: number; children: ReactNode }) {
  return (
    <tr className="tabla__vacia">
      <td colSpan={columnas}>{children}</td>
    </tr>
  );
}
