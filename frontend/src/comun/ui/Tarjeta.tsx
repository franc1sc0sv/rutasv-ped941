// Tarjeta: panel con titulo (h2) y acciones. Conecta aria-labelledby solo.
//   <Tarjeta titulo="Agregar tramo" acciones={<Boton ...>}>...</Tarjeta>
// `nivel` cambia el titulo a h3 dentro de otra seccion. `sinRelleno` para tablas a sangre.
import { useId, type ReactNode } from 'react';

interface Props {
  titulo?: ReactNode;
  /** Texto corto a la derecha del titulo (conteos, resumen). */
  detalle?: ReactNode;
  acciones?: ReactNode;
  nivel?: 2 | 3;
  sinRelleno?: boolean;
  /** Acento de color en el borde superior (p. ej. filas EXPRES/NORMAL). */
  acento?: 'primario' | 'expres' | 'normal';
  id?: string;
  className?: string;
  children: ReactNode;
}

export function Tarjeta({ titulo, detalle, acciones, nivel = 2, sinRelleno = false, acento, id, className, children }: Props) {
  const idTitulo = useId();
  const Titulo = nivel === 2 ? 'h2' : 'h3';
  const clases = ['tarjeta', sinRelleno && 'tarjeta--sin-relleno', acento && `tarjeta--${acento}`, className].filter(Boolean).join(' ');
  return (
    <section id={id} className={clases} aria-labelledby={titulo ? idTitulo : undefined}>
      {(titulo || acciones) && (
        <header className="tarjeta__encabezado">
          <div className="tarjeta__titulos">
            {titulo && (
              <Titulo id={idTitulo} className="tarjeta__titulo">
                {titulo}
              </Titulo>
            )}
            {detalle && <span className="tarjeta__detalle">{detalle}</span>}
          </div>
          {acciones && <div className="tarjeta__acciones">{acciones}</div>}
        </header>
      )}
      {children}
    </section>
  );
}
