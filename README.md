# RutaSV

Planificador de rutas de transporte público para el proyecto PED941 (equipo de 5).
El núcleo está hecho en Java 21 con Maven (`core`), con una capa HTTP delgada (`api`) y una interfaz en React (`frontend`).

Todo funciona de punta a punta:
- login con roles;
- autocompletado de estaciones;
- ruta más rápida con Dijkstra;
- historial y estadísticas;
- red vial;
- alta, edición y activación de paradas;
- despacho de unidades y filas de pasajeros.

Todas las estructuras son propias: no hay colecciones de `java.util` guardando datos del dominio.

## Las 7 estructuras

| Estructura | Interfaz (`nucleo.estructuras`) | Implementación | Rol autor | Dónde se usa | Costo clave |
|---|---|---|---|---|---|
| Lista dinámica | `Lista<T>` | `nucleo.estructuras.impl.ListaDinamica` | Historial | Historial, vecinos del grafo, resultados | agregar O(1) amortizado |
| Pila | `Pila<T>` | `nucleo.estructuras.impl.PilaEnlazada` | Ruta | Reconstruir la ruta de Dijkstra | apilar/desapilar O(1) |
| Cola FIFO | `Cola<T>` | `nucleo.estructuras.impl.ColaEnlazada` | Terminal | Filas de pasajeros por tipo (RN-5) | encolar/desencolar O(1) |
| Cola de prioridad | `ColaPrioridad<T>` | `nucleo.estructuras.impl.MonticuloBinario` | Terminal | Despacho (RN-4), Dijkstra, top 5 | insertar/extraer O(log n) |
| Tabla hash | `Mapa<K,V>` | `nucleo.estructuras.impl.TablaHash` | Paradas | Estaciones, usuarios, sesiones, grafo, conteos | O(1) promedio |
| Árbol AVL | `IndiceNombres` | `busqueda.estructuras.ArbolAvl` | Búsqueda | Autocompletado por prefijo | O(L log n) |
| Grafo | `RedVial` | `ruta.estructuras.GrafoListaAdyacencia` | Ruta | Red de estaciones y tramos | vecinos O(1) + O(grado) |

Las cinco genéricas viven en `nucleo/estructuras/impl`, el `java.util` propio del equipo, porque todos los módulos las usan.
El grafo y el AVL son de un solo dominio y se quedan en su módulo.
Mediciones reales contra el Big-O: [docs/rendimiento.md](docs/rendimiento.md).

## Probar la demo (para la defensa)

```bash
./demo.sh --limpio      # compila, carga la red de 30 estaciones y los datos de demo, abre http://localhost:5173
```

`demo.sh` genera las contraseñas de `admin` y de los pasajeros `ana_01`, `luis.m` y `sofia-r`, y las muestra en pantalla.
Usa la carpeta `.demo-data/`, que git ignora, así que `data/` nunca se ensucia.

Qué trae la demo:
- 30 rutas en el historial, para que el top 5 no salga vacío;
- las 5 unidades de la Fig. 10 en la cola de despacho;
- 4 pasajeros en cada fila.

Casos de la red para mostrar en vivo:

| Caso | Cómo verlo |
|---|---|
| Dijkstra no elige el tramo directo | Terminal Occidente → Terminal Oriente: 33 min por el centro, no 45 min por la Panamericana |
| No existe ruta (RN-6) | Cualquier estación → Nejapa, Quezaltepeque o Aguilares (grupo aislado) |
| Estación inactiva | Apopa, San Jacinto y Hospital Rosales: no se sugieren y Dijkstra las salta |
| Grafo dirigido | Centro Histórico → Mercado Central es de un solo sentido |
| Tildes y mayúsculas | "soya" sugiere Soyapango; no se puede crear "Soyapangó" |
| Prefijo en el AVL | "ter" sugiere las tres terminales |

## Instalar JDK 21 y Maven

```bash
sudo apt install openjdk-21-jdk maven   # Ubuntu / Debian
java -version                           # debe decir 21
mvn -version                            # Maven 3.9 o superior
```

Para el frontend: Node 20 o superior.

## Compilar y probar

```bash
mvn -B verify          # compila y corre todas las pruebas (core y api)
cd frontend && npm ci && npm run build

# Mediciones de rendimiento (no corren en el CI); reescriben docs/rendimiento.md
mvn -B -pl core test -Dtest=MedicionesRendimientoTest -Drendimiento=true -Dsurefire.failIfNoSpecifiedTests=false
```

## Levantar el sistema sin datos de demo

```bash
mvn -B package -DskipTests
RUTASV_ADMIN_PASSWORD=una-clave-de-demo java -jar api/target/rutasv-api.jar   # API en :7070
cd frontend && npm install && npm run dev                                     # http://localhost:5173
```

Contrato de la API: [docs/api.md](docs/api.md).

## Estructura del repositorio

```
core/                    modulo Java (edu.udb.rutasv)
  nucleo/                lo compartido; no depende de ningun modulo
    estructuras/         interfaces (Lista, Pila, Cola, ColaPrioridad, Mapa, IndiceNombres, RedVial)
      impl/              implementaciones propias: ListaDinamica, PilaEnlazada, ColaEnlazada,
                         MonticuloBinario, TablaHash
    modelo/              registros del dominio (Estacion, Arista, RutaCalculada, ...)
    texto/               Normalizador (sin mayusculas ni tildes), lo usan Paradas y Busqueda
    excepciones/         excepciones compartidas
    archivo/             escritura atomica y agregado de lineas
  <modulo>/              auth, ruta, paradas, busqueda, terminal, historial
    servicios/           logica de negocio
    repositorios/        guardar y cargar datos
    estructuras/         estructura propia del dominio (ruta: grafo; busqueda: AVL)
    (ruta tambien tiene algoritmos/Dijkstra)
  app/                   unica puerta de entrada
    interfaces/          SistemaTransporte (fachada)
    servicios/           SistemaTransporteImpl, Modulos y RedSoloLectura
    configuracion/       Aplicacion (cableado) y DatosDemo
api/                     capa HTTP delgada (Javalin): un endpoint por metodo de SistemaTransporte
frontend/                React + Vite + TypeScript, una carpeta por rol en src/modulos/
data/                    red de ejemplo en CSV (datos sinteticos)
docs/                    documentacion del equipo
```

Reglas de arquitectura, verificadas por `ArquitecturaTest`:
- `nucleo` no depende de ningún módulo;
- cada módulo depende solo de `nucleo`;
- solo `app` conoce a todos los módulos;
- el core no conoce HTTP ni ninguna interfaz gráfica.

## Variables de entorno

- `RUTASV_ADMIN_USER`: usuario admin inicial (por defecto `admin`).
- `RUTASV_ADMIN_PASSWORD`: contraseña del admin inicial, mínimo 8 caracteres. Sin ella no se crea el admin.
- `RUTASV_PORT`: puerto de la API (por defecto `7070`).
- `RUTASV_DATA_DIR`: carpeta con los CSV y los archivos de usuarios e historial (por defecto `data`).
- `RUTASV_DEMO=1` y `RUTASV_DEMO_PASSWORD`: cargan los datos de demostración (ver `DatosDemo`).
- `RUTASV_API` (frontend): dirección de la API para el proxy de Vite (por defecto `http://localhost:7070`).

## Documentación

- [CONTRIBUTING.md](CONTRIBUTING.md): flujo de ramas y PR
- [docs/arquitectura.md](docs/arquitectura.md) y [docs/decisiones.md](docs/decisiones.md)
- [docs/tabla-hash.md](docs/tabla-hash.md): la tabla hash propia, su Big-O y su bitácora (PR #40 y #41)
- [docs/rendimiento.md](docs/rendimiento.md): mediciones contra el Big-O
- [docs/api.md](docs/api.md): endpoints HTTP
- [docs/contratos.md](docs/contratos.md)
- [docs/documento-ruta.md](docs/documento-ruta.md): pseudocódigo y complejidad de Dijkstra
- [docs/entrega-fase2.md](docs/entrega-fase2.md): histórico de la Fase 2
