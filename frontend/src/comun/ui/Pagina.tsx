// Encabezado comun de cada pantalla: titulo (h1 unico), referencia a la figura del documento,
// una frase de contexto y acciones opcionales a la derecha.
//   <Pagina titulo="Red vial" figura="Fig. 8" descripcion="..." acciones={<Boton>…</Boton>}>…</Pagina>
import type { ReactNode } from 'react';

interface Props {
  titulo: ReactNode;
  /** Referencia al documento del proyecto, p. ej. "Fig. 8". */
  figura?: string;
  descripcion?: ReactNode;
  acciones?: ReactNode;
  /** Ancho de lectura comodo (pantallas de pasajero). */
  angosta?: boolean;
  children: ReactNode;
}

export function Pagina({ titulo, figura, descripcion, acciones, angosta = false, children }: Props) {
  return (
    <div className={angosta ? 'pagina pagina--angosta' : 'pagina'}>
      <header className="pagina__encabezado">
        <div className="pagina__titulos">
          <h1 className="pagina__titulo">{titulo}</h1>
          {descripcion && <p className="pagina__descripcion">{descripcion}</p>}
        </div>
        {(acciones || figura) && (
          <div className="pagina__acciones">
            {acciones}
            {figura && (
              <span className="pagina__figura" title="Figura del documento del proyecto">
                {figura}
              </span>
            )}
          </div>
        )}
      </header>
      {children}
    </div>
  );
}
