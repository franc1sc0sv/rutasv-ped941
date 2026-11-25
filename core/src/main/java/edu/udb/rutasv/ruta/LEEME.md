# Rol Ruta

**Objetivo:** Calcular la ruta mas rapida (en minutos) entre dos estaciones.

## Estado final (Fase 3)

- `estructuras/GrafoListaAdyacencia.java` (`RedVial`): grafo dirigido con listas de adyacencia. Dos `TablaHash`: estacion por codigo y tramos salientes por codigo. `estacion(codigo)` es O(1) promedio; `vecinos` es O(1) + O(grado).
- `algoritmos/Dijkstra.java`: cola de prioridad (`MonticuloBinario`) con borrado perezoso, tablas hash para peso, km y anterior, y una `PilaEnlazada` para reconstruir el camino. O((V + E) log V). Salta las estaciones inactivas.
- `servicios/RutaServiceImpl.java`: valida origen y destino (existen, estan activos y son distintos), mide el tiempo de calculo y arma la `RutaCalculada`.
- La pila propia (`PilaEnlazada`, autoria de este rol) vive en `nucleo/estructuras/impl` porque es generica.
- Pruebas: `DijkstraTest`, `DijkstraConMonticuloTest`, `GrafoListaAdyacencia*Test`, `RutaServiceImplTest`. Mediciones en `docs/rendimiento.md`.

## Por que estas estructuras

- Por que grafo y no arbol: la red tiene ciclos y varios caminos entre dos estaciones; un arbol solo tiene uno.
- Por que Dijkstra y no BFS: los tramos tienen peso (minutos). BFS minimiza saltos, no tiempo. Bellman-Ford admite pesos negativos, que aqui no existen (RN-2), y cuesta O(V E).
- Por que listas de adyacencia y no matriz: la red es dispersa (E ≈ 3V). La matriz gasta O(V²) memoria y recorrer vecinos seria O(V).

## Reglas

- El codigo del modulo depende solo de `nucleo` (`ArquitecturaTest` lo comprueba). Las estructuras genericas estan en `nucleo/estructuras/impl`.
- Un cambio de contrato de `nucleo` va en su propio PR y se avisa al equipo.
- Un PR por tarea, con el CI en verde. Ver `CONTRIBUTING.md`.
