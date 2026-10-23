# RutaSV · frontend (React)

Interfaz web de RutaSV hecha con Vite 5 + React 18 + TypeScript + react-router 6.
No usa librerías de componentes ni frameworks de CSS: los estilos son CSS plano con tokens en `src/styles/tokens.css`.

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
| `src/layout/` | Ventana base: menú lateral (admin) o barra inferior (pasajero) | todos |
| `src/comun/` | `Pendiente`, `Cargando`, `MensajeError`, `MapaRed`, `usePedido`, `useEstaciones`, formatos | todos |
| `src/modulos/busqueda/` | Buscar ruta (Fig. 5) y el autocompletado | Búsqueda |
| `src/modulos/ruta/` | Resultado de ruta (Fig. 6) | Ruta |
| `src/modulos/historial/` | Mi historial (Fig. 7), Red vial (Fig. 8), Historial global (Fig. 11) | Historial |
| `src/modulos/paradas/` | Registro de paradas (Fig. 9) | Paradas |
| `src/modulos/terminal/` | Despacho (Fig. 10) | Terminal |
| `src/styles/` | `tokens.css` (colores, espacios, radios, tipografía, modo oscuro) y `global.css` | todos |

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
4. Mientras el backend responda 501, la pantalla muestra `<Pendiente rol="..." />`. Cuando tu módulo esté listo, esa tarjeta deja de salir sola, porque solo se pinta si `error.pendiente` es `true`. Revisa entonces que la vista con datos se vea bien y borra lo que ya no haga falta.

## Limitaciones conocidas

- **Filas de pasajeros (Despacho):** la API no tiene un `GET` para ver el contenido de las filas. La pantalla guarda una copia local de lo que se encola y atiende desde ella; si recargas la página, esa copia se pierde aunque el servidor conserve la fila.
- **Tramos:** el backend guarda tramos dirigidos. La opción "Doble sentido" de Red vial envía dos `POST` (ida y vuelta).
