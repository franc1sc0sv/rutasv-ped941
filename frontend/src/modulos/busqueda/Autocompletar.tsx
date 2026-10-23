// Campo de estacion con autocompletado. Rol: Busqueda.
// Endpoint: GET /estaciones/sugerencias?prefijo=... (con espera de 200 ms entre teclas).
// La logica de teclado y ARIA vive en el componente base Combobox (src/comun/ui/Combobox.tsx);
// aqui solo se decide de donde salen las sugerencias y como se ve el marcador A/B.
import { forwardRef, type ReactNode } from 'react';
import { api } from '../../api/cliente';
import { Combobox } from '../../comun/ui';

interface Props {
  etiqueta: string;
  valor: string;
  alCambiar: (v: string) => void;
  alEnfocar?: () => void;
  placeholder?: string;
  /** Texto bajo el campo, p. ej. el codigo resuelto ("E01 · Occidente"). */
  ayuda?: ReactNode;
  invalido?: boolean;
  /** A = origen (punto hueco), B = destino (punto lleno). */
  marcador: 'A' | 'B';
}

const sugerir = (prefijo: string) => api.get<string[]>('/estaciones/sugerencias', { prefijo });

export const Autocompletar = forwardRef<HTMLInputElement, Props>(function Autocompletar({ marcador, ...resto }, ref) {
  return (
    <Combobox
      ref={ref}
      {...resto}
      buscar={sugerir}
      marcador={<span className={`marcador-parada marcador-parada--${marcador}`} aria-hidden="true" />}
    />
  );
});
