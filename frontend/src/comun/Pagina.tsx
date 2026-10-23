import type { ReactNode } from 'react';

/** Encabezado comun de cada pantalla: titulo, figura del documento y acciones opcionales. */
export function Pagina({
  titulo,
  figura,
  descripcion,
  acciones,
  children,
}: {
  titulo: string;
  figura?: string;
  descripcion?: ReactNode;
  acciones?: ReactNode;
  children: ReactNode;
}) {
  return (
    <div className="pagina">
      <header className="pagina__encabezado">
        <div>
          {figura && <span className="pagina__figura">{figura}</span>}
          <h1 className="pagina__titulo">{titulo}</h1>
          {descripcion && <p className="pagina__descripcion texto-suave">{descripcion}</p>}
        </div>
        {acciones && <div className="pagina__acciones">{acciones}</div>}
      </header>
      {children}
    </div>
  );
}
