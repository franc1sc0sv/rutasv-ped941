# Entrega de la Fase 2

Cierre segun el plan de trabajo: **25 de octubre de 2026**. Este archivo junta lo que falta, como levantar el sistema para grabar y el guion del video.

## 1. Estado por rol

Leyenda: ✅ hecho y conectado · 🟡 hecho pero falta conectar o probar · ❌ falta.

| Rol | Estructura propia | Servicio | Conectado en la fachada | Pantalla (React) |
|---|---|---|---|---|
| Ruta | ✅ Grafo, ✅ Pila | ✅ Dijkstra + RutaService | ✅ `calcularRuta`, `verRed`, `agregarTramo` | Resultado de ruta (Fig. 6) |
| Busqueda | ✅ Arbol AVL | ✅ Normalizador, BusquedaService | ✅ `sugerirEstaciones`, `busquedasRecientes` | Buscar ruta (Fig. 5), ventana base |
| Terminal | ✅ Monticulo, ✅ Cola FIFO | ✅ ReglaDespacho, ✅ Terminal | ✅ despacho y filas | Despacho (Fig. 10) |
| Paradas | ❌ TablaHash | 🟡 Directorio (falta editar y activar/desactivar), ✅ CargadorCSV | 🟡 `buscarEstacion`, `crearEstacion`; ❌ `editarEstacion`, `activarDesactivarEstacion` | Registro de paradas (Fig. 9) |
| Historial | ✅ ListaDinamica | ✅ historial y estadisticas | ✅ `miHistorial`, `estadisticas` | Mi historial, Red vial, Historial global (Fig. 7, 8, 11) |

### Lo que falta, en orden

1. **Paradas:** `TablaHash` (quita el `@Disabled` de `TablaHashContratoTest`) y cambiar `MapaTemporal` por `TablaHash` en `Aplicacion` (login, Dijkstra y directorio). Agregar `editar` y `activar/desactivar` a `DirectorioEstaciones` y conectarlos; al desactivar, actualizar tambien la red (`red.agregarEstacion` con la estacion inactiva) para que Dijkstra la salte.
2. **Historial:** `ListaDinamica`, `HistorialRepositorioArchivo`, guardar en `calcularRuta` (RN-7, nunca si no hay ruta, RN-6), y estadisticas (conteo por par con tabla hash, top 5 con cola de prioridad, tiempo promedio con `tiempoCalculoNs`). Conectar `miHistorial` y `estadisticas`.
3. **Terminal:** decidir un solo dueno de la fila RN-5 (seccion 8). La prueba de la Fig. 10 y `verOrdenDespacho` sobre una copia ya estan.
4. **Todos:** conectar su pantalla en `frontend/src/modulos/<rol>/` (ver `frontend/README.md`) y revisar sus pendientes de la seccion 8.
5. **Cuando lleguen `TablaHash` y `ListaDinamica`:** cambiar las clases Temporal en todos los lugares de la seccion 9.

## 2. Levantar el sistema para grabar

```bash
# 1. Backend (desde la raiz)
mvn -B package -DskipTests
RUTASV_ADMIN_PASSWORD=una-clave-de-demo java -jar api/target/rutasv-api.jar

# 2. Frontend (otra terminal)
cd frontend && npm install && npm run dev      # http://localhost:5173
```

- Admin: usuario `admin` con la clave de `RUTASV_ADMIN_PASSWORD`. Pasajero: registrate desde la pantalla de inicio.
- Datos de ejemplo: `data/estaciones.csv` y `data/tramos.csv` (8 estaciones de la Fig. 8).
- Caso de demo de Ruta: Terminal Occidente -> Metrocentro = **18 min** (8 + 8 + 2).
- Si algo de tu modulo dice "Pendiente", es que aun no esta conectado en la fachada.

## 3. Video del avance

La rubrica pide (nivel Estrategico): **narracion con contexto, demo, retos y proximos pasos, clara para no tecnicos**. Cada integrante graba su parte; Historial edita.

**Duracion sugerida:** intro 1 min + 2 a 3 min por rol + cierre 1 min (unos 12 a 15 min).

### Guion de cada parte (2 a 3 min)

1. **Contexto (20 s):** que problema resuelve tu modulo para el pasajero o el administrador, en palabras simples.
2. **Estructura (40 s):** cual estructura programaste, por que esa y su costo (Big-O). Muestra el codigo 10 segundos, no mas.
3. **Demo (60 s):** tu pantalla funcionando con el caso del documento.
4. **Prueba (20 s):** corre tu prueba en verde (`mvn -B test -pl core -Dtest=<TuPrueba>`).
5. **Reto y proximo paso (20 s):** un problema real que tuviste y que harias en la Fase 3.

### Demo por rol

| Rol | Pantalla | Que mostrar | Prueba a correr |
|---|---|---|---|
| Ruta | Resultado de ruta | Occidente -> Metrocentro = 18 min; ruta inexistente (RN-6); estacion inactiva | `DijkstraTest`, `GrafoListaAdyacenciaContratoTest`, `PilaEnlazadaContratoTest` |
| Busqueda | Buscar ruta | Escribir "ter" y "metro" (sin tildes); intercambiar; busquedas recientes | `ArbolAvlTest`, `BusquedaServiceTest` |
| Terminal | Despacho | Registrar las 5 unidades de la Fig. 10 y despachar en orden RN-4; fila FIFO | `DespachoFigura10Test`, `ReglaDespachoTest`, `MonticuloBinarioTest` |
| Paradas | Registro de paradas | Buscar por codigo y nombre, crear parada, codigo repetido, CSV con error | `TablaHashContratoTest`, `DirectorioEstacionesTest`, `CargadorCSVTest` |
| Historial | Mi historial, Red vial, Historial global | Agregar tramo (peso 0 rechazado, RN-2); historial en orden; top 5 | `ListaDinamicaContratoTest` y las de historial |

**Intro y cierre** (puede grabarlos quien integra): el problema y las 7 estructuras (Tabla 1 del documento), la arquitectura (`docs/arquitectura.md`), y los proximos pasos de la Fase 3.

### Consejos de grabacion

- Graba en 1080p, con el navegador al 110 % de zoom y sin pestanas ni notificaciones.
- Usa datos sinteticos: nada de correos ni contrasenas reales en pantalla.
- Ensaya una vez con el sistema recien levantado; graba la pantalla y la voz por separado si te trabas.

## 4. Bitacora (cada uno la suya; Busqueda la une)

Una entrada por sesion de trabajo:

```markdown
### AAAA-MM-DD — <tu nombre> (<rol>)
- **Que hice:** ...
- **Problema:** ...
- **Decision y por que:** ...
- **Que aprendi / que cambiaria:** ...
```

Cierre del equipo: 3 problemas que afectaron a todos, como se resolvieron y que se propone para la Fase 3.

## 5. Documento actualizado (cada uno su seccion; Terminal integra)

El documento de la Fase 1 debe quedar coherente con el codigo (rubrica). Correcciones detectadas en la revision del 23/10:

| Seccion | Que corregir | Quien |
|---|---|---|
| Tabla 1 (las 7 filas) | Cambiar las clases de `java.util` por las propias y su interfaz de `nucleo`: `GrafoListaAdyacencia` (`RedVial`), `TablaHash` (`Mapa`), `MonticuloBinario` (`ColaPrioridad`), `ListaDinamica` (`Lista`), `PilaEnlazada` (`Pila`), `ColaEnlazada` (`Cola`), `ArbolAvl` (`IndiceNombres`) | Cada rol su fila |
| 2.1, 2.5, Tabla 1 filas 1 y 5 | El grafo se indexa por codigo (`String`), no por `Estacion`; la pila guarda codigos; agregar un tramo es O(grado) porque reemplaza el anterior (RN-3). Texto listo en `docs/documento-ruta.md` | Ruta |
| 1.3, 2.2, Tabla 1 fila 2 | No hay "baja" que borre: RN-1 desactiva (`activa`). Hay dos indices (codigo y nombre sin mayusculas). La clave de ejemplo es `E01`, no `EST-001` | Paradas |
| 2.3, 5.2, Fig. 13, Tabla 5 | `MonticuloBinario.insertar/extraer` en lugar de `offer/poll`. RN-4 tiene un tercer desempate por id (orden siempre igual). Agregar `verOrdenDespacho` y su costo O(n log n) a la Tabla 5 | Terminal |
| 2.6 y RN-5 (3.5) | Hoy hay una fila por tipo de ruta (NORMAL y EXPRES). Elegir el modelo final con Terminal y reescribir RN-5 (A3, M4) | Terminal |
| 2.7 | TreeMap -> Arbol AVL propio con rotaciones; costo O(log n + k) por prefijo (M5) | Busqueda |
| 3.3, Fig. 3 | `SistemaTransporte` es una interfaz; `SistemaTransporteImpl` usa `Modulos` (un servicio por rol). Agregar `Usuario`, `Sesion`, `Rol` y los campos actuales de `RutaCalculada` y `Estacion` | Lead |
| 3.4, Fig. 4 | La operacion se llama `calcularRuta` y recibe codigos. Secuencia: Fachada -> Autorizador -> RutaServiceImpl -> Dijkstra (ColaPrioridad, Mapa, RedVial) -> PilaEnlazada. El texto -> codigo es una llamada anterior (`sugerirEstaciones`/`buscarEstacion`). El guardado RN-7 queda en la fachada | Ruta |
| 3.6, Fig. 6 | La nota dice **6.4 km**; con `data/tramos.csv` la ruta suma **9.5 km** (4.0 + 4.5 + 1.0). Cambiar la figura o el CSV (M7) | Ruta e Historial |
| 3.6, Fig. 7, 8, 11 | Recalcular los datos con el programa (M7) | Historial |
| 4.2 y 4.3 | A*: el modelo ya guarda coordenadas, pasa de "no se puede" a "mejora de la Fase 3". Tabla de complejidad en `docs/documento-ruta.md` (M1) | Ruta |
| 5.1, Fig. 12, Tabla 4 | Pseudocodigo con la pila (A1) y Tabla 4 actualizada: texto en `docs/documento-ruta.md` | Ruta |
| Nueva seccion | Decisiones de implementacion: interfaz React + API, login y roles, persistencia en archivos, busquedas recientes, desempates (ver `docs/decisiones.md`) | Lead |

## 6. Autocritica (Terminal redacta, todos aportan)

- Que quedo completo y que no (usa la tabla de la seccion 1 el dia de la entrega).
- Por que: dependencias entre roles y tiempo.
- Plan concreto para la Fase 3: Big-O con mejor, promedio y peor caso; A* con coordenadas; metricas por modulo; casos borde en todas las pantallas.

## 7. Tabla de casos de prueba (Paradas la une)

| Modulo | Caso | Entrada | Esperado | Prueba |
|---|---|---|---|---|
| Ruta | Ruta del ejemplo | E01 -> E04 | 18 min, E01-E02-E03-E04 | `DijkstraTest.occidenteAMetrocentroEn18Minutos` |
| Ruta | Origen = destino | E03 -> E03 | 0 min, una estacion | `DijkstraTest.origenIgualADestino` |
| Ruta | Sin ruta (RN-6) | E01 -> estacion aislada | vacio | `DijkstraTest.sinRutaDevuelveVacioRn6` |
| Ruta | Estacion inactiva | E03 inactiva | busca desvio o no hay ruta | `DijkstraTest.saltaEstacionesInactivas` |
| Ruta | Peso invalido (RN-2) | minutos 0 o -3 | `ValidacionException` | `GrafoListaAdyacenciaContratoTest.minutosNoPositivosSeRechazan` |
| Ruta | Un tramo por sentido (RN-3) | A->B dos veces | se reemplaza | `GrafoListaAdyacenciaTest.repetirUnTramoLoReemplazaRn3` |
| Ruta | Tramo de mas de 24 h o km no finitos | 1441 min, NaN | `ValidacionException` | `GrafoListaAdyacenciaTest.rechazaTramosDeMasDeUnDiaYKmNoFinitos` |
| Ruta | Dijkstra con el monticulo propio | red de 500 estaciones | 499 min, atajos descartados | `DijkstraConMonticuloTest` |
| API | Sin ruta por HTTP (RN-6) | E01 -> estacion aislada | 404 | `ServidorTest.sinRutaDevuelve404Rn6` |
| Terminal | Orden de la Fig. 10 (RN-4) | U-009 N 07:05, U-031 X 07:30, U-005 N 07:40, U-014 X 07:15, U-022 N 07:20 | U-014, U-031, U-009, U-022, U-005 | `DespachoFigura10Test` |
| Terminal | Terminal vacia | despachar sin unidades | `ColeccionVaciaException` | `DespachoFigura10Test.despacharSacaLasUnidadesEnElOrdenDeLaFigura` |
| ... | (cada rol agrega sus filas) | | | |

## 8. Pendientes por rol que dejo la revision del 23/10

Revision automatica de arquitectura, Java, reglas de negocio, pruebas, documentacion y API (68 hallazgos verificados). Lo de Ruta, `app`, `api` y docs ya se corrigio. Esto queda para cada rol:

| Rol | Pendiente | Archivo |
|---|---|---|
| Terminal | Un solo dueno de la fila RN-5 (hoy `Modulos.filas` por tipo; `Terminal.filaPasajeros` no se usa) | `Terminal.java`, `Aplicacion.java` |
| Terminal y Busqueda | Usar `ValidacionException` en lugar de `IllegalArgumentException` | `Terminal.java`, `BusquedaService.java` |
| Busqueda | `ArbolAvl` (estructura) depende de `servicios.Normalizador`: capa invertida | `ArbolAvl.java` |
| Busqueda | Javadoc de `IndiceNombres` con costos de trie; con AVL es O(log n + k) (PR de contrato) | `IndiceNombres.java` |
| Paradas | Unicidad de nombres sin quitar tildes (choca con el AVL) y `toLowerCase()` sin `Locale.ROOT` | `DirectorioEstaciones.java` |
| Paradas | Columna `activa` invalida en el CSV: reportar error de linea en vez de desactivar en silencio | `CargadorCSV.java` |
| Paradas | Minutos del CSV como `int` (hoy `double`) | `CargadorCSV.java` |
| Auth (lead) | Un pasajero puede registrarse como `admin` antes de que se cree el admin y bloquear la siembra. Reservar ese nombre | `AuthServiceImpl.java`, `SeedAdmin.java` |
| Auth (lead) | Sin limite de intentos en login y registro (mejora para la Fase 3) | `Servidor.java` |

## 9. Clases Temporal que se reemplazan

Cuando una estructura propia pase su contrato, cambia la Temporal en **todos** estos lugares (no solo en `Aplicacion`):

| Temporal | Donde |
|---|---|
| `MapaTemporal` -> `TablaHash` | `Aplicacion` (usuarios, `MapaTemporal::new` de Dijkstra y de `EstadisticasService`, `filas`, mapa de `BusquedaService`), constructores sin argumentos de `GrafoListaAdyacencia` y `DirectorioEstaciones`, `Terminal.unidadesEnEspera` |
| `ListaTemporal` -> `ListaDinamica` | `GrafoListaAdyacencia.nuevaLista`, `Dijkstra.reconstruir`, `Terminal` (`verOrdenDespacho`, `verFila`), `ArbolAvl.conPrefijo`, `BusquedaService.recientes`, `UsuarioRepositorioArchivo`, `AuthServiceImpl`, `SeedAdmin` |
| `RedVialTemporal` | `app/servicios/RedSoloLectura` (copia de la red para `verRed`); puede quedarse o pasar a `GrafoListaAdyacencia` |
