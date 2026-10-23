# API HTTP (para el frontend React)

La capa `api/` es delgada: cada endpoint llama a un metodo de `SistemaTransporte` y nada mas.
La logica vive en `core`. Si tu modulo aun no esta conectado, su endpoint responde `501` con `"pendiente": true`.

Base: `http://localhost:7070/api`. En desarrollo, Vite reenvia `/api` a ese puerto (ver `frontend/vite.config.ts`).

## Sesion

`POST /auth/login` o `POST /auth/registro` devuelven `{ "token", "usuario", "rol" }` (`rol`: `PASAJERO` o `ADMIN`).
Todos los demas pedidos llevan el encabezado `Authorization: Bearer <token>`.

## Errores

Siempre `{ "error": "mensaje", "pendiente": false }`.

| Codigo | Cuando |
|---|---|
| 400 | Datos invalidos (`ValidacionException`): minutos <= 0, codigo inexistente, JSON mal formado |
| 401 | Sin sesion, sesion invalida o credenciales incorrectas |
| 403 | Falta rol ADMIN (RN-1) |
| 404 | No hay ruta (RN-6) o no existe la estacion buscada |
| 409 | Usuario repetido, o cola vacia al despachar o atender |
| 501 | Modulo pendiente (`"pendiente": true`) |

## Endpoints

| Metodo y ruta | Rol | Cuerpo / parametros | Respuesta | Fachada | Modulo |
|---|---|---|---|---|---|
| `POST /auth/registro` | - | `{usuario, contrasena}` | sesion (201) | `registrar` | auth |
| `POST /auth/login` | - | `{usuario, contrasena}` | sesion | `iniciarSesion` | auth |
| `POST /auth/logout` | sesion | - | 204 | - | api |
| `GET /auth/yo` | sesion | - | sesion | - | api |
| `GET /estaciones/sugerencias?prefijo=ter` | sesion | - | `["Terminal Occidente", ...]` | `sugerirEstaciones` | Busqueda |
| `GET /busquedas/recientes` | sesion | - | `["ter", ...]` | `busquedasRecientes` | Busqueda |
| `GET /rutas?origen=E01&destino=E04` | sesion | - | ruta (abajo) | `calcularRuta` | Ruta |
| `GET /red` | sesion | - | `{estaciones: [Estacion], tramos: [Tramo]}` | `verRed` | Ruta |
| `POST /red/tramos` | ADMIN | `{origen, destino, minutos, km}` | 201 | `agregarTramo` | Ruta |
| `GET /historial` | sesion | - | `[RutaCalculada]` | `miHistorial` | Historial (pendiente) |
| `GET /estadisticas` | ADMIN | - | `{totalBusquedas, rutaMasConsultada, tiempoPromedioNs, top5}` | `estadisticas` | Historial (pendiente) |
| `GET /estaciones` | sesion | - | `[Estacion]` | `verRed` | Paradas |
| `GET /estaciones/buscar?texto=Metrocentro` | sesion | - | `Estacion` o 404 | `buscarEstacion` | Paradas |
| `POST /estaciones` | ADMIN | `Estacion` | 201 | `crearEstacion` | Paradas |
| `PUT /estaciones/{codigo}` | ADMIN | `Estacion` | 204 | `editarEstacion` | Paradas (pendiente) |
| `PATCH /estaciones/{codigo}/estado` | ADMIN | `{activa}` | 204 | `activarDesactivarEstacion` | Paradas (pendiente) |
| `POST /terminal/unidades` | ADMIN | `{id, tipoRuta, horaProgramada: "07:30"}` | 201 | `registrarUnidad` | Terminal |
| `GET /terminal/orden` | ADMIN | - | `[Unidad]` en orden de salida | `verOrdenDespacho` | Terminal |
| `POST /terminal/despachar` | ADMIN | - | `Unidad` despachada | `despacharSiguiente` | Terminal |
| `POST /terminal/filas/{NORMAL o EXPRES}/pasajeros` | ADMIN | `{id, nombre}` | 201 | `encolarPasajero` | Terminal |
| `POST /terminal/filas/{NORMAL o EXPRES}/atender` | ADMIN | - | `Pasajero` | `atenderPasajero` | Terminal |

## Formas JSON

```jsonc
// Estacion
{ "codigo": "E01", "nombre": "Terminal Occidente", "zona": "Occidente", "x": 10, "y": 50, "activa": true }
// Tramo
{ "origen": "E01", "destino": "E02", "minutos": 8, "km": 4.0 }
// Ruta (GET /rutas): RutaCalculada + detalle para la pantalla Resultado de ruta (Fig. 6)
{
  "origen": "E01", "destino": "E04", "minutos": 18, "km": 9.5,
  "fecha": "2026-10-24T10:15:00", "tiempoCalculoNs": 773753, "usuario": "ana_01",
  "estaciones": [ /* Estacion en orden de origen a destino */ ],
  "tramos": [ /* Tramo por cada par consecutivo, con sus minutos y km */ ]
}
// Unidad
{ "id": "U-014", "tipoRuta": "EXPRES", "horaProgramada": "07:00:00" }
// Pasajero
{ "id": "P1", "nombre": "Ana" }
```

## Agregar un endpoint

1. Si el metodo ya existe en `SistemaTransporte`, solo agrega la linea en `api/.../Servidor.java`.
2. Si no existe, primero cambia el contrato en `core` (PR aparte, avisa al equipo) y luego agrega el endpoint.
3. Agrega su caso en `ServidorTest` y actualiza esta tabla.
