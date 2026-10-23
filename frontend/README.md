# RutaSV · frontend (React)

Interfaz web de RutaSV hecha con Vite 5 + React 18 + TypeScript + react-router 6.
No usa librerías de componentes ni frameworks de CSS. Los estilos son CSS plano con tokens en `src/styles/tokens.css` y componentes propios en `src/comun/ui/`. La dirección visual, los tokens y las reglas están en [`DESIGN.md`](DESIGN.md).

Habla con la API Java de `api/` según el contrato de [`docs/api.md`](../docs/api.md).

## Requisitos

- Node 20 o superior (`node -v`)
- La API corriendo en `http://localhost:7070` (ver `api/README.md`)

## Comandos

```bash
cd frontend
npm install          # una sola vez
npm run dev          # http://localhost:5173 (con la API corriendo)
npm run build        # revisa tipos (tsc --noEmit) y genera dist/
npm run typecheck    # solo revisa tipos
npm run preview      # sirve dist/ en http://localhost:4173
```

En desarrollo (y en `preview`), Vite reenvía `/api` a `http://localhost:7070`, así que no hace falta CORS.

Para levantar la API con datos de prueba sin tocar `data/`:

```bash
mkdir -p /tmp/rutasv-datos && cp data/*.csv /tmp/rutasv-datos/
RUTASV_ADMIN_PASSWORD=admin-demo-123 RUTASV_DATA_DIR=/tmp/rutasv-datos java -jar api/target/rutasv-api.jar
```

Usuario admin: `admin` / `admin-demo-123`. Los pasajeros se crean desde la pantalla "Crear cuenta".

## Mapa de carpetas

| Carpeta | Qué tiene | Dueño |
|---|---|---|
| `src/api/` | `cliente.ts` (fetch + token + errores) y `tipos.ts` (formas JSON de `docs/api.md`) | todos (cambios con aviso) |
| `src/sesion/` | `SesionContext`, Login/Registro, guardas de ruta (sesión y admin) | Auth |
| `src/layout/` | Ventana base: menú lateral de señal (admin) o barra inferior (pasajero), `Marca` | todos |
| `src/comun/ui/` | Componentes base: `Boton`, `Campo`/`Entrada`/`Selector`, `Combobox`, `Tabla`, `Tarjeta`, `Aviso`, `Insignia`, `EstadoVacio`, `Cargando`/`Esqueleto`, `Pendiente`, `Pagina`, `Icono` (+ `ui.css`) | todos (cambios con aviso) |
| `src/comun/` | `MapaRed`, `usePedido`, `useEstaciones`, formatos | todos |
| `src/modulos/busqueda/` | Buscar ruta (Fig. 5) y el autocompletado | Búsqueda |
| `src/modulos/ruta/` | Resultado de ruta (Fig. 6) | Ruta |
| `src/modulos/historial/` | Mi historial (Fig. 7), Red vial (Fig. 8), Historial global (Fig. 11) | Historial |
| `src/modulos/paradas/` | Registro de paradas (Fig. 9) | Paradas |
| `src/modulos/terminal/` | Despacho (Fig. 10) | Terminal |
| `src/styles/` | `tokens.css` (única fuente de colores, tipografía, espacios, radios, elevación y movimiento, con modo oscuro) y `global.css` (base y rejillas) | todos |

Cada pantalla dice al inicio del archivo qué rol la mantiene y qué endpoints usa.

## Pantallas y rutas

| Ruta | Pantalla | Quién la ve | Estado del backend |
|---|---|---|---|
| `/login` | Login / Registro | todos | listo |
| `/buscar` | Buscar ruta | pasajero y admin | listo |
| `/ruta?origen=E01&destino=E04` | Resultado de ruta | pasajero y admin | listo |
| `/historial` | Mi historial | pasajero y admin | **pendiente** (501) |
| `/admin/red` | Red vial | admin | listo |
| `/admin/paradas` | Registro de paradas | admin | alta y búsqueda listas; editar y activar/desactivar **pendientes** |
| `/admin/despacho` | Despacho | admin | listo |
| `/admin/historial-global` | Historial global | admin | **pendiente** (501) |

Después de entrar: el pasajero va a Buscar ruta y el admin a Red vial.

## Cómo conectar tu pantalla

1. Busca tu endpoint en [`docs/api.md`](../docs/api.md). Si devuelve una forma nueva, agrégala a `src/api/tipos.ts`.
2. Pide los datos con `src/api/cliente.ts`: `api.get<Tipo>('/ruta', { parametro })`, `api.post('/ruta', cuerpo)`, `api.put(...)` o `api.patch(...)`. El token se agrega solo. Cualquier error llega como `ApiError` con `status`, `message` (el texto del backend) y `pendiente`.
3. Para cargar al abrir la pantalla usa `usePedido(() => api.get<Tipo>('/ruta'), [])`, que te da `datos`, `error`, `cargando` y `recargar`.
4. Mientras el backend responda 501, la pantalla muestra `<Pendiente rol="..." />` (de `src/comun/ui`). Cuando tu módulo esté listo, esa tarjeta deja de salir sola, porque solo se pinta si `error.pendiente` es `true`. Revisa entonces que la vista con datos se vea bien y borra lo que ya no haga falta.

## Cómo usar el sistema de diseño

Importa todo desde `src/comun/ui` y no escribas estilos que ya existen:

```tsx
import { Aviso, Boton, Campo, Entrada, Pagina, Pendiente, Tarjeta } from '../../comun/ui';

<Pagina titulo="Mi pantalla" figura="Fig. 7" descripcion="Una frase que diga para qué sirve.">
  <Tarjeta titulo="Agregar algo">
    <form className="pila" onSubmit={guardar} noValidate>
      <Campo etiqueta="Minutos" ayuda="Mayor que 0" error={errorMinutos}>
        <Entrada type="number" value={min} onChange={(e) => setMin(e.target.value)} />
      </Campo>
      {aviso && <Aviso tono={aviso.tono}>{aviso.texto}</Aviso>}
      <Boton variante="primario" type="submit" icono="mas" cargando={enviando}>Agregar</Boton>
    </form>
  </Tarjeta>
</Pagina>
```

Al conectar una pantalla, revisa estos puntos:

- **Estados.** Mientras carga, usa `<Esqueleto />` si conoces la forma del contenido y `<Cargando />` si no. Si falla, usa `<Aviso tono="error" titulo="No se pudo …">{error.message}</Aviso>`. Si no hay datos, usa `<EstadoVacio titulo="…" accion={…}>` con el siguiente paso. Mientras el endpoint responda 501, usa `<Pendiente rol="…" />`.
- **Errores de campo.** Pásalos en `<Campo error="…">`. El campo queda marcado y el lector de pantalla lo anuncia.
- **Tablas.** Envuelve con `<Tabla etiqueta="…">`, marca las cifras con `className="num"` y muestra la tabla vacía con `<FilaVacia>`.
- **Tipo de ruta.** Usa `<InsigniaTipo tipo="EXPRES" />`. Para los códigos de estación, usa `<CodigoEstacion codigo="E01" />`.
- **Rejillas.** Usa `rejilla-2`, `rejilla-2--invertida` o `rejilla-2--pareja`. Se adaptan al ancho del contenido.
- **CSS de tu módulo.** Solo usa `var(--token)`. Si te falta un color o una medida, agrégalo en `tokens.css` con su valor oscuro y documéntalo en `DESIGN.md`.
- **Movimiento.** Usa `--dur-*` y `--curva-*`, anima solo `transform` y `opacity`, y mantente por debajo de 300 ms. El modo de movimiento reducido ya está cubierto en `global.css`.
- **Texto.** Escribe en español, tuteando y en minúscula de oración. Los botones empiezan con un verbo ("Agregar tramo", no "Enviar"). Cada error dice qué pasó y cómo arreglarlo.

## Limitaciones conocidas

- **Filas de pasajeros (Despacho):** la API no tiene un `GET` para ver el contenido de las filas. La pantalla guarda una copia local de lo que se encola y atiende desde ella; si recargas la página, esa copia se pierde aunque el servidor conserve la fila.
- **Tramos:** el backend guarda tramos dirigidos. La opción "Doble sentido" de Red vial envía dos `POST` (ida y vuelta).
