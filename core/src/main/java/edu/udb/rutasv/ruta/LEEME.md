# Rol Ruta

**Objetivo:** La red vial como grafo y el calculo de la ruta mas rapida (Dijkstra).

Tu issue: https://github.com/franc1sc0sv/rutasv-ped941/issues/1 (asignatela y marca las casillas).

## Empieza aqui

```bash
git clone https://github.com/franc1sc0sv/rutasv-ped941.git && cd rutasv-ped941
mvn verify                       # debe terminar en BUILD SUCCESS
git checkout -b feat/ruta-<tarea>
```

## Que hay (hecho)

- `estructuras/GrafoListaAdyacencia.java`: lista de adyacencia sobre tablas hash; RN-2 y RN-3.
- `estructuras/PilaEnlazada.java`: pila con nodos, O(1).
- `algoritmos/Dijkstra.java`: cola de prioridad + tablas hash de peso y anterior; salta inactivas; RN-6. O((V + E) log V).
- `interfaces/RutaService.java` y `servicios/RutaServiceImpl.java`: arma `RutaCalculada` y mide el tiempo con `System.nanoTime`.
- `interfaces/FabricaColaPrioridad` y `FabricaMapa`: `app` le pasa `MonticuloBinario::new` (Terminal) y, cuando llegue, `TablaHash::new` (Paradas).
- Conectado en `SistemaTransporteImpl.calcularRuta`, `verRed` y `agregarTramo`.

## Pendiente de otros roles

- `MapaTemporal::new` -> `TablaHash::new` en `Aplicacion` cuando Paradas termine.
- `ListaTemporal` -> `ListaDinamica` en `GrafoListaAdyacencia.nuevaLista` y `Dijkstra.reconstruir` cuando Historial termine.

## Tus tareas (de la Fase 2)

- [x] 1. Grafo con lista de adyacencia (`agregarEstacion`, `agregarTramo`, `vecinos`). Rechaza pesos <= 0 (RN-2). Un tramo por sentido (RN-3).
- [x] 2. Pila enlazada propia.
- [x] 3. Dijkstra con cola de prioridad y tablas hash de peso y anterior. Salta estaciones inactivas. Avisa si no hay ruta (RN-6).
- [x] 4. Reconstruccion con la pila y `RutaCalculada`.
- [x] 5. Tiempo de calculo con `System.nanoTime`.
- [ ] 6. Pantalla Resultado de ruta (Fig 6).
- [x] 7. Pruebas: Occidente -> Metrocentro 18 min, origen = destino, sin ruta, peso invalido, estacion inactiva.
- [ ] 8. Documento: pseudocodigo con la pila, complejidad de Dijkstra, A*, Bellman-Ford.

## Reglas

- Tu codigo vive solo en `ruta/`. Depende solo de `nucleo`: `ArquitecturaTest` lo comprueba.
- Si necesitas cambiar un contrato de `nucleo`, abre un PR aparte y avisa por el chat del grupo.
- Un PR por tarea, que lo revise otra persona y con el CI en verde. Ver `CONTRIBUTING.md`.
- La guia completa de tareas esta en `docs/tareas-por-rol.md`.
