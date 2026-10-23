export function Cargando({ texto = 'Cargando…', pantallaCompleta = false }: { texto?: string; pantallaCompleta?: boolean }) {
  return (
    <div className={pantallaCompleta ? 'cargando cargando--pantalla' : 'cargando'} role="status" aria-live="polite">
      <span className="cargando__giro" aria-hidden="true" />
      <span>{texto}</span>
    </div>
  );
}
