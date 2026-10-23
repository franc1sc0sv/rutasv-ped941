// Marca de RutaSV: placa de senal azul con una linea y dos estaciones (origen lleno, destino hueco).
export function Marca({ tamano = 28, conTexto = true, className }: { tamano?: number; conTexto?: boolean; className?: string }) {
  return (
    <span className={className ? `marca ${className}` : 'marca'}>
      <svg className="marca__placa" viewBox="0 0 32 32" width={tamano} height={tamano} aria-hidden="true">
        <rect className="marca__fondo" width="32" height="32" rx="4" />
        <path className="marca__linea" d="M7 21H13.5L18.5 11H25" />
        <circle className="marca__origen" cx="7" cy="21" r="3.4" />
        <circle className="marca__destino" cx="25" cy="11" r="3" />
      </svg>
      {conTexto && <span className="marca__texto">RutaSV</span>}
    </span>
  );
}
