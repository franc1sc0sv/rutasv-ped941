# RutaSV

Planificador de rutas de transporte publico para el proyecto PED941 (equipo de 5).
El nucleo esta hecho en Java 21 con Maven (`core`), con una capa HTTP delgada (`api`) y una interfaz en React (`frontend`).

Funcionan de punta a punta: login y roles, busqueda con autocompletado, calculo de ruta con Dijkstra, red vial y despacho de unidades.
La tabla hash, la lista dinamica y el historial estan implementados. Siguen pendientes la edicion y activacion/desactivacion de paradas. La guia de fase 2 es historica: [docs/entrega-fase2.md](docs/entrega-fase2.md).

## Las 7 estructuras

| Estructura | Interfaz | Rol | Uso | Estado |
|---|---|---|---|---|
| Lista dinamica | `Lista<T>` | Historial | Usuarios, historial, vecinos | ✅ `ListaDinamica` |
| Pila | `Pila<T>` | Ruta | Reconstruir la ruta | ✅ `PilaEnlazada` |
| Cola FIFO | `Cola<T>` | Terminal | Fila de pasajeros | ✅ `ColaEnlazada` |
| Cola de prioridad | `ColaPrioridad<T>` | Terminal | Despacho, Dijkstra, top 5 | ✅ `MonticuloBinario` |
| Tabla hash | `Mapa<K,V>` | Paradas | Estaciones, usuarios, conteos | ✅ `TablaHash` |
| Arbol AVL | `IndiceNombres` | Busqueda | Busqueda por prefijo | ✅ `ArbolAvl` |
| Grafo | `RedVial` | Ruta | Red de estaciones y tramos | ✅ `GrafoListaAdyacencia` |

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
mvn -B test            # solo pruebas
```

## Levantar el sistema

```bash
mvn -B package -DskipTests
RUTASV_ADMIN_PASSWORD=una-clave-de-demo java -jar api/target/rutasv-api.jar   # API en :7070
cd frontend && npm install && npm run dev                                     # http://localhost:5173
```

Contrato de la API: [docs/api.md](docs/api.md). Como conectar tu pantalla: [frontend/README.md](frontend/README.md).

## Empieza aqui (sin reuniones)

1. Elige tu rol y asignate su issue: [Ruta #1](../../issues/1), [Busqueda #2](../../issues/2), [Terminal #3](../../issues/3), [Historial #4](../../issues/4), [Paradas #5](../../issues/5).
2. Abre la guia de tu modulo: `core/src/main/java/edu/udb/rutasv/<modulo>/LEEME.md`. Dice que archivos ya existen, que te toca crear y en que orden.
3. Clona, corre `mvn verify` y crea tu rama `feat/<rol>-<tarea>`.
4. Implementa tu estructura, quita el `@Disabled` de su prueba de contrato y abre un PR chico por tarea.

No necesitas esperar a nadie: `nucleo/temporal` trae versiones con `java.util` de todo lo que dependa de otro rol.

## Estructura del repositorio

```
core/                    modulo Java (edu.udb.rutasv)
  nucleo/                contratos compartidos
    estructuras/         interfaces (Lista, Pila, Cola, ColaPrioridad, Mapa, IndiceNombres, RedVial)
    modelo/              registros del dominio
    excepciones/         excepciones compartidas
    archivo/             escritura atomica de archivos
    temporal/            adaptadores temporales (java.util), se reemplazan por los propios
  <modulo>/              auth, ruta, paradas, busqueda, terminal, historial. Cada uno con las que necesite:
    interfaces/          contratos del servicio
    servicios/           logica de negocio
    repositorios/        guardar y cargar datos
    modelo/              clases propias del modulo
    excepciones/         excepciones propias
    estructuras/         estructuras de datos propias del rol
    (ruta tambien tiene algoritmos/)
  app/                   unica puerta de entrada
    interfaces/          SistemaTransporte
    servicios/           SistemaTransporteImpl, Modulos (que modulos estan conectados) y RedSoloLectura
    configuracion/       Aplicacion (cableado)
api/                     capa HTTP delgada (Javalin): un endpoint por metodo de SistemaTransporte
frontend/                React + Vite + TypeScript, una carpeta por rol en src/modulos/
data/                    CSV de ejemplo (datos sinteticos)
docs/                    documentacion del equipo
.github/                 CI y plantilla de PR
```

## Variables de entorno

- `RUTASV_ADMIN_USER`: usuario admin inicial (por defecto `admin`).
- `RUTASV_ADMIN_PASSWORD`: contrasena del admin inicial, minimo 8 caracteres. Sin ella no se crea el admin.
- `RUTASV_PORT`: puerto de la API (por defecto `7070`).
- `RUTASV_DATA_DIR`: carpeta con los CSV y los archivos de usuarios (por defecto `data`).

## Documentacion

- [CONTRIBUTING.md](CONTRIBUTING.md): flujo de ramas y PR
- [docs/arquitectura.md](docs/arquitectura.md)
- [docs/decisiones.md](docs/decisiones.md)
- [docs/contratos.md](docs/contratos.md)
- [docs/tareas-por-rol.md](docs/tareas-por-rol.md)
- [docs/api.md](docs/api.md): endpoints HTTP
- [docs/entrega-fase2.md](docs/entrega-fase2.md): estado, guion del video, bitacora, correcciones del documento y pendientes por rol
- [docs/documento-ruta.md](docs/documento-ruta.md): texto del rol Ruta para el documento (pseudocodigo y complejidad)

## Integracion de TablaHash (22/11/2026)

La tabla usa cubetas con cadenas propias, capacidad inicial 16 y duplicacion al superar
una carga de 0.75. Admite claves/valores nulos y reemplaza valores sin aumentar el tamano.
Una cadena doble de entradas permite copiar las claves en O(n), incluso tras muchos borrados.
No usa colecciones java.util para almacenar datos.

Aplicacion inyecta TablaHash en usuarios, grafo, Dijkstra, busquedas recientes, filas,
estadisticas y Terminal. DirectorioEstaciones la usa por defecto. Los constructores anteriores
de Terminal y el grafo sin argumentos conservan el adaptador temporal por compatibilidad;
la aplicacion real usa la tabla propia. Asi se mantiene la separacion entre modulos.

Con hash/equals constantes: obtener, contiene y eliminar son O(1) esperado y O(n) peor caso;
poner es O(1) esperado amortizado y O(n) peor caso. Claves es O(n), tamano O(1) y memoria
O(capacidad + n). No se reduce la capacidad al borrar. Las cadenas agregan el costo de su
longitud. La tabla requiere sincronizacion externa para acceso concurrente.

Pruebas: contrato habilitado, umbral de crecimiento, 1000 claves, colisiones forzadas,
hash Integer.MIN_VALUE, nulos, copia independiente y 20000 operaciones contra HashMap.
Ejecutar mvn -B verify; en frontend, npm ci y npm run build.

Limitaciones anteriores al cambio: editarEstacion y activarDesactivarEstacion lanzan
NoImplementadoException; directorio y AVL normalizan nombres de manera diferente;
el CSV interpreta valores desconocidos de activa como false. Quedan ListaTemporal en
otros modulos. Deben resolverse o declararse antes de considerar terminada la fase 3.
