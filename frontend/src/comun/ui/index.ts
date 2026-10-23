// Componentes base de RutaSV. Importa desde aqui:
//   import { Boton, Campo, Entrada, Tarjeta, Aviso } from '../../comun/ui';
// Estilos: ui.css (se carga una vez en main.tsx). Guia: frontend/DESIGN.md.
export { Aviso, type TonoAviso } from './Aviso';
export { Boton, BotonEnlace, clasesBoton, type VarianteBoton, type TamanoBoton } from './Boton';
export { Campo, Casilla, Entrada, Segmentado, Selector } from './Campo';
export { Cargando, Esqueleto } from './Cargando';
export { Combobox } from './Combobox';
export { EstadoVacio } from './EstadoVacio';
export { Icono, type NombreIcono } from './Icono';
export { CodigoEstacion, Insignia, InsigniaTipo, type TonoInsignia } from './Insignia';
export { Pagina } from './Pagina';
export { Pendiente, type RolEquipo } from './Pendiente';
export { FilaVacia, Tabla } from './Tabla';
export { Tarjeta } from './Tarjeta';
