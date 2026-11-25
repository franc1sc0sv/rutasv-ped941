# Mediciones de rendimiento

Generado por `MedicionesRendimientoTest` (21.0.12.1, 16 núcleos). Cada valor es la mediana de 7 corridas tras calentar la JVM. Datos sintéticos, semilla fija.

La columna "normalizado" divide el tiempo entre la función del Big-O: si se mantiene más o menos constante al crecer n, la medida confirma la teoría.

## 1. Tabla hash contra búsqueda lineal

Buscar 1 000 claves existentes. La tabla hash es O(1) promedio; recorrer una lista es O(n).

| n | TablaHash (ns por búsqueda) | Lista lineal (ns por búsqueda) | Lista / Tabla |
|---:|---:|---:|---:|
| 1,000 | 46.6 | 2,985.3 | 64.0× |
| 10,000 | 40.6 | 9,854.9 | 242.9× |
| 100,000 | 35.4 | 105,334.3 | 2,976.4× |
| 1,000,000 | 40.0 | — | — |

Con un millón de claves la tabla tarda casi lo mismo que con mil: eso es O(1) promedio. La lista crece en proporción a n. Por eso el directorio de estaciones, los usuarios, las sesiones y el grafo usan `TablaHash`.

## 2. Árbol AVL (autocompletado)

Buscar 1 000 nombres en un árbol con n nombres. Teoría: O(L log n), con L el largo del nombre.

| n | ns por búsqueda | log₂ n | ns / log₂ n |
|---:|---:|---:|---:|
| 1,000 | 573.9 | 10.0 | 57.6 |
| 10,000 | 473.3 | 13.3 | 35.6 |
| 100,000 | 345.3 | 16.6 | 20.8 |
| 500,000 | 433.2 | 18.9 | 22.9 |

Los nombres se insertaron **en orden**, el peor caso de un árbol binario sin balancear: sin rotaciones se volvería una lista de altura n y cada búsqueda sería O(n). Con las rotaciones del AVL la altura queda en O(log n) y el tiempo crece muy lento. Que la columna normalizada baje con n se debe a la caché y al JIT, no al algoritmo: lo que importa es que el tiempo no se dispara al pasar de mil a medio millón de nombres.

## 3. Montículo binario (despacho y Dijkstra)

Insertar n elementos y extraerlos todos. Teoría: O(log n) cada operación, O(n log n) en total.

| n | ms total | ns / (n log₂ n) |
|---:|---:|---:|
| 10,000 | 1.2 | 8.9 |
| 100,000 | 16.5 | 10.0 |
| 1,000,000 | 282.6 | 14.2 |

## 4. Dijkstra (ruta más rápida)

Red en cuadrícula de V estaciones con tramos de 1 a 10 minutos en ambos sentidos (E ≈ 4V tramos dirigidos). Ruta de una esquina a la opuesta: el peor caso, porque Dijkstra visita casi toda la red antes de cerrar el destino. Teoría: O((V + E) log V).

| V | E | ms por ruta | ns / ((V+E) log₂ V) |
|---:|---:|---:|---:|
| 100 | 360 | 0.2 | 70.7 |
| 1,024 | 3,968 | 1.2 | 24.8 |
| 10,000 | 39,600 | 6.0 | 9.2 |
| 50,176 | 199,808 | 61.9 | 15.9 |

La red de la demo tiene 30 estaciones y 83 tramos: una ruta tarda microsegundos. El tiempo que muestra la app ("tiempo de cálculo") incluye el calentamiento de la JVM, por eso las primeras rutas tardan más que las siguientes.

## 5. Top 5 de rutas: montículo acotado contra montículo completo

P pares distintos con su conteo. El montículo acotado guarda solo 5: O(P log 5) = O(P). Meter todos los pares y sacar 5 cuesta O(P log P).

| P | acotado (ms) | completo (ms) | completo / acotado |
|---:|---:|---:|---:|
| 10,000 | 0.6 | 0.4 | 0.7× |
| 100,000 | 1.8 | 2.4 | 1.3× |
| 1,000,000 | 14.4 | 31.2 | 2.2× |

Con pocos pares el acotado puede salir **más lento**: hace una extracción en casi cada inserción. La ventaja en tiempo aparece con muchos pares, porque el montículo completo hace hasta log P comparaciones por inserción y el acotado como mucho log 5 ≈ 2,3. En RutaSV hay pocos pares, así que la ganancia real es de **memoria**: guarda 5 elementos en vez de P. Es una limitación honesta de la optimización.
