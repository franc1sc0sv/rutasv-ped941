// Estado vacio: dice que no hay nada todavia y cual es el siguiente paso.
//   <EstadoVacio icono="ruta" titulo="Aún no calculas rutas" accion={<BotonEnlace a="/buscar">Buscar ruta</BotonEnlace>}>
//     Tus rutas aparecerán aquí.
//   </EstadoVacio>
import type { ReactNode } from 'react';
import { Icono, type NombreIcono } from './Icono';

interface Props {
  icono?: NombreIcono;
  titulo: ReactNode;
  children?: ReactNode;
  accion?: ReactNode;
  /** Variante sin borde para dentro de una tarjeta o tabla. */
  compacto?: boolean;
}

export function EstadoVacio({ icono = 'info', titulo, children, accion, compacto = false }: Props) {
  return (
    <div className={compacto ? 'vacio vacio--compacto' : 'vacio'}>
      <span className="vacio__icono" aria-hidden="true">
        <Icono nombre={icono} tamano={compacto ? 20 : 24} />
      </span>
      <div className="vacio__texto">
        <p className="vacio__titulo">{titulo}</p>
        {children && <p className="texto-suave">{children}</p>}
      </div>
      {accion && <div className="vacio__accion">{accion}</div>}
    </div>
  );
}
