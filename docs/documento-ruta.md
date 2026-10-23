# Rol Ruta: texto para el documento

Secciones del documento de la Fase 1 que corrige el rol Ruta (tarea 8): pseudocodigo de la seccion 5.1 con la pila (A1) y complejidad de Dijkstra, A* y Bellman-Ford en la seccion 4 (M1).
El texto coincide con el codigo de `core/src/main/java/edu/udb/rutasv/ruta/`.

## 5.1 Calculo de la ruta optima (reemplaza la Figura 12)

El calculo recibe la red vial, el codigo de la estacion de origen y el de destino. El peso de un tramo es su tiempo en minutos (siempre mayor que cero, RN-2). Las estaciones inactivas no se visitan.

```text
Algoritmo CalcularRuta(red, origen, destino)
  // Paso 0: validacion
  si origen o destino no existen, o alguno esta inactivo:
      devolver SIN_RUTA                                  // RN-6

  // Paso 1: inicio
  peso     <- tabla hash vacia        // codigo -> minutos acumulados
  km       <- tabla hash vacia        // codigo -> km acumulados
  anterior <- tabla hash vacia        // codigo -> codigo de la estacion previa
  cerrada  <- tabla hash vacia        // codigo -> ya tiene su peso definitivo
  cola     <- cola de prioridad (menor peso primero; si empatan, menor codigo)
  peso[origen] <- 0 ;  km[origen] <- 0
  cola.insertar((origen, 0))

  // Paso 2: ciclo principal
  mientras cola no este vacia:
      (actual, p) <- cola.extraer()
      si actual esta en cerrada: continuar               // entrada vieja de la cola
      cerrada[actual] <- verdadero
      si actual = destino: salir del ciclo               // su peso ya es el minimo
      para cada tramo (vecino, minutos, distancia) en red.vecinos(actual):
          si vecino esta en cerrada o esta inactivo: continuar
          nuevo <- p + minutos
          si vecino no esta en peso o nuevo < peso[vecino]:
              peso[vecino]     <- nuevo
              km[vecino]       <- km[actual] + distancia
              anterior[vecino] <- actual
              cola.insertar((vecino, nuevo))

  si destino no esta en peso:
      devolver SIN_RUTA                                  // RN-6: no se guarda en el historial

  // Paso 3: reconstruccion con la pila
  pila <- pila vacia
  e <- destino
  mientras e no sea nulo:
      pila.apilar(e)                  // se apila de destino hacia origen
      e <- anterior[e]
  ruta <- lista dinamica vacia
  mientras pila no este vacia:
      ruta.agregar(pila.desapilar())  // sale de origen hacia destino
  devolver (ruta, peso[destino], km[destino])
```

Que cambio respecto a la Figura 12 de la Fase 1:

1. **La pila se usa de verdad (A1).** Antes se describia el recorrido hacia atras sin la pila; ahora la ruta se apila desde el destino y se desapila desde el origen. Es lo que hace `Dijkstra.reconstruir` con `PilaEnlazada`.
2. **Entradas viejas en la cola.** La cola de prioridad no tiene "disminuir prioridad", asi que una estacion puede entrar varias veces; la tabla `cerrada` descarta las copias viejas.
3. **Estaciones inactivas.** Se saltan en el ciclo, y si el origen o el destino estan inactivos no hay ruta.
4. **Corte temprano.** El ciclo termina al cerrar el destino: su peso ya no puede mejorar.
5. **Kilometros.** Se acumulan junto al peso para mostrarlos al usuario, sin intervenir en la decision.
6. **Tiempo de calculo.** `RutaServiceImpl` mide la llamada con `System.nanoTime` y lo guarda en `RutaCalculada.tiempoCalculoNs` para las estadisticas del historial.

Ejemplo de la Figura 6 (datos de `data/tramos.csv`): Terminal Occidente (E01) -> Metrocentro (E04) = E01, E02, E03, E04 con 8 + 8 + 2 = **18 minutos** y 4.0 + 4.5 + 1.0 = **9.5 km**.

## 4.2 Complejidad de las alternativas (se agrega a la Tabla 3)

V es el numero de estaciones y E el de tramos. En una red vial, E crece casi como V (cada parada conecta con pocas vecinas).

| Algoritmo | Tiempo | Memoria | Pesos negativos | Para RutaSV |
|---|---|---|---|---|
| **Dijkstra con monticulo binario** (implementado) | O((V + E) log V) | O(V + E) | No | Elegido: pesos en minutos, siempre positivos |
| Dijkstra con arreglo (sin cola de prioridad) | O(V²) | O(V) | No | Peor en redes dispersas como esta |
| **A\*** | O((V + E) log V) en el peor caso; en la practica explora menos estaciones | O(V + E) | No | Mejora para la Fase 3: las estaciones ya tienen coordenadas `x`, `y`, asi que la distancia en linea recta puede ser la estimacion |
| **Bellman-Ford** | O(V · E) | O(V) | Si | Descartado: mas lento y su ventaja (pesos negativos) no aplica |

Notas para el texto:

- **Por que O((V + E) log V) y no O(E log V) sin mas:** cada estacion sale de la cola una vez con su peso final (O(V log V)) y cada tramo puede insertar una entrada (O(E log V)). Con las copias viejas, la cola guarda hasta E entradas; log E <= 2 log V, asi que el orden no cambia.
- **Reconstruccion:** O(V) por la pila, como dice la seccion 2.5.
- **Tablas hash de peso y anterior:** O(1) promedio por operacion; no cambian el orden total.
- **A\* con estimacion cero es Dijkstra** (seccion 4.3 de la Fase 1). Con coordenadas en el modelo, la estimacion admisible seria: distancia en linea recta / velocidad maxima de la red, en minutos.

## 2.1 y 2.5: datos que confirma el codigo

- **Lista de adyacencia (2.1):** `GrafoListaAdyacencia` guarda, por codigo de estacion, la lista de sus tramos salientes; el acceso por codigo es por tabla hash (O(1) promedio). Agregar un tramo es O(grado) porque un segundo tramo A->B reemplaza al primero (RN-3: un tramo por sentido). `vecinos` devuelve una copia en O(grado).
- **Pila (2.5):** `PilaEnlazada` con nodos; `apilar`, `desapilar` y `cima` son O(1).
