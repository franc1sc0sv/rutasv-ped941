# API HTTP (para el frontend React)

La capa `api/` es delgada: cada endpoint llama a un metodo de `SistemaTransporte` y nada mas.
La logica vive en `core`. Todos los modulos estan conectados; `501` queda solo como respuesta de seguridad si un modulo faltara en el cableado.

Base: `http://localhost:7070/api`. En desarrollo, Vite reenvia `/api` a ese puerto (ver `frontend/vite.config.ts`).

## Sesion

`POST /auth/login` o `POST /auth/registro` devuelven `{ "token", "usuario", "rol" }` (`rol`: `PASAJERO` o `ADMIN`).
Todos los demas pedidos llevan el encabezado `Authorization: Bearer <token>`.

## Errores

Siempre `{ "error": "mensaje", "pendiente": false }`.

| Codigo | Cuando |
|---|---|
| 400 | Datos invalidos (`ValidacionException`): minutos fuera de 1..1440 o con decimales, codigo inexistente, JSON mal formado, tipo de ruta u hora invalidos |
| 401 | Sin sesion, sesion invalida o credenciales incorrectas |
| 403 | Falta rol ADMIN (RN-1) |
| 404 | No hay ruta (RN-6) o no existe la estacion buscada |
| 409 | Usuario repetido, o cola vacia al despachar o atender |
| 500 | Error inesperado (`"Error interno del servidor"`); el detalle queda en el log |
| 501 | Modulo pendiente (`"pendiente": true`) |

## Endpoints

| Metodo y ruta | Rol | Cuerpo / parametros | Respuesta | Fachada | Modulo |
|---|---|---|---|---|---|
| `POST /auth/registro` | - | `{usuario, contrasena}` | sesion (201) | `registrar` | auth |
| `POST /auth/login` | - | `{usuario, contrasena}` | sesion | `iniciarSesion` | auth |
| `POST /auth/logout` | sesion | - | 204 | `cerrarSesion` | auth |
| `GET /auth/yo` | sesion | - | sesion | - | api |
| `GET /estaciones/sugerencias?prefijo=ter` | sesion | - | `["Terminal Occidente", ...]` | `sugerirEstaciones` | Busqueda |
| `GET /busquedas/recientes` | sesion | - | `["Terminal Occidente → Metrocentro", ...]` (ultimas 5 rutas calculadas) | `busquedasRecientes` | Busqueda |
| `GET /rutas?origen=E01&destino=E04` | sesion | - | ruta (abajo) | `calcularRuta` | Ruta |
| `GET /red` | sesion | - | `{estaciones: [Estacion], tramos: [Tramo]}` | `verRed` | Ruta |
| `POST /red/tramos` | ADMIN | `{origen, destino, minutos, km}` | 201 | `agregarTramo` | Ruta |
| `GET /historial` | sesion | - | `[RutaCalculada]` (abajo) | `miHistorial` | Historial |
| `GET /estadisticas` | ADMIN | - | `{totalBusquedas, rutaMasConsultada, tiempoPromedioNs, top5}` | `estadisticas` | Historial |
| `GET /estaciones` | sesion | - | `[Estacion]` | `verRed` | Paradas |
| `GET /estaciones/buscar?texto=Metrocentro` | sesion | - | `Estacion` o 404 | `buscarEstacion` | Paradas |
| `POST /estaciones` | ADMIN | `Estacion` | 201 | `crearEstacion` | Paradas |
| `PUT /estaciones/{codigo}` | ADMIN | `{nombre, zona, x, y, activa?}` | 204 | `editarEstacion` | Paradas |
| `PATCH /estaciones/{codigo}/estado` | ADMIN | `{activa}` (obligatorio) | 204 | `activarDesactivarEstacion` | Paradas |
| `POST /terminal/unidades` | ADMIN | `{id, tipoRuta, horaProgramada: "07:30"}` | 201 | `registrarUnidad` | Terminal |
| `GET /terminal/orden` | ADMIN | - | `[Unidad]` en orden de salida | `verOrdenDespacho` | Terminal |
| `POST /terminal/despachar` | ADMIN | - | `Unidad` despachada | `despacharSiguiente` | Terminal |
| `GET /terminal/filas/{NORMAL o EXPRES}` | ADMIN | - | `Pasajero[]` en orden de llegada | `verFila` | Terminal |
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
// RutaCalculada (GET /historial): el registro del core, sin detalle. "tramos" son codigos en orden
{ "origen": "E01", "destino": "E04", "tramos": ["E01", "E02", "E03", "E04"], "minutos": 18, "km": 9.5,
  "fecha": "2026-10-24T10:15:00", "tiempoCalculoNs": 773753, "usuario": "ana_01" }
// Estadisticas (GET /estadisticas): pares "ORIGEN -> DESTINO" con codigos; top5 de mas a menos consultado,
// empates en orden alfabetico. totalBusquedas cuenta solo las rutas encontradas (RN-6: las demas no se guardan).
// Sin historial: totalBusquedas 0, rutaMasConsultada null, tiempoPromedioNs 0, top5 []
{ "totalBusquedas": 7, "rutaMasConsultada": "E01 -> E04", "tiempoPromedioNs": 512340,
  "top5": ["E01 -> E04", "E02 -> E05", "E01 -> E03"] }
// Unidad
{ "id": "U-014", "tipoRuta": "EXPRES", "horaProgramada": "07:00:00" }
// Pasajero
{ "id": "P1", "nombre": "Ana" }
```

## Agregar un endpoint

1. Si el metodo ya existe en `SistemaTransporte`, solo agrega la linea en `api/.../Servidor.java`.
2. Si no existe, primero cambia el contrato en `core` (PR aparte, avisa al equipo) y luego agrega el endpoint.
3. Agrega su caso en `ServidorTest` y actualiza esta tabla.

## Validaciones (Fase 3)

- Un cuerpo JSON vacio o `null` responde 400, nunca 500.
- `POST /estaciones`: `x` e `y` son obligatorias y estan acotadas (±10 000). `activa` es opcional y por defecto es `true`. El codigo admite letras, numeros, guion y guion bajo (maximo 12) y se guarda en mayusculas. El nombre no admite `;` ni `>` y no puede repetir otro sin importar mayusculas ni tildes.
- `POST /red/tramos`: `minutos` (entero de 1 a 1440) y `km` son obligatorios.
- `GET /rutas`: los codigos aceptan minusculas y espacios. Mismo origen y destino, o una estacion inactiva, responde 400 con el motivo. Sin camino responde 404 (RN-6).
- `POST /terminal/unidades`: el id no puede estar en blanco ni repetido.
- `POST /terminal/filas/{tipo}/pasajeros`: un pasajero no puede estar en dos filas a la vez.
