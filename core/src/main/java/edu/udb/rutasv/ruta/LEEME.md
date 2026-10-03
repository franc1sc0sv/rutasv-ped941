# Rol Ruta

**Objetivo:** La red vial como grafo y el calculo de la ruta mas rapida (Dijkstra).

Tu issue: https://github.com/franc1sc0sv/rutasv-ped941/issues/1 (asignatela y marca las casillas).

## Empieza aqui

```bash
git clone https://github.com/franc1sc0sv/rutasv-ped941.git && cd rutasv-ped941
mvn verify                       # debe terminar en BUILD SUCCESS
git checkout -b feat/ruta-<tarea>
```

## Ya esta creado para ti (esqueletos con `TODO`)

- `estructuras/GrafoListaAdyacencia.java` implementa `RedVial`
- `estructuras/PilaEnlazada.java` implementa `Pila`

Las pruebas ``GrafoListaAdyacenciaContratoTest` y `PilaEnlazadaContratoTest`` (en `src/test/.../ruta/estructuras/`) ya estan conectadas y tienen `@Disabled`. Cuando tu clase este lista, quita esa linea: el test de contrato te dice si cumples.

## Te toca crear

- `algoritmos/Dijkstra.java`
- `interfaces/RutaService.java` y `servicios/RutaServiceImpl.java`: arma `RutaCalculada` con tramos, minutos, km y tiempo (`System.nanoTime`)
- Conectalo en `app/servicios/SistemaTransporteImpl.calcularRuta` (hoy lanza `NoImplementadoException`)

## Mientras otros terminan lo suyo

Usa `ListaTemporal`, `MapaTemporal` (pesos y anterior) y `ColaPrioridadTemporal` (con `Comparator`) de `nucleo/temporal`, hasta que lleguen las propias.

## Tus tareas (de la Fase 2)

- [ ] 1. Grafo con lista de adyacencia (`agregarEstacion`, `agregarTramo`, `vecinos`). Rechaza pesos <= 0 (RN-2). Un tramo por sentido (RN-3).
- [ ] 2. Pila enlazada propia.
- [ ] 3. Dijkstra con cola de prioridad y tablas hash de peso y anterior. Salta estaciones inactivas. Avisa si no hay ruta (RN-6).
- [ ] 4. Reconstruccion con la pila y `RutaCalculada`.
- [ ] 5. Tiempo de calculo con `System.nanoTime`.
- [ ] 6. Pantalla Resultado de ruta (Fig 6).
- [ ] 7. Pruebas: Occidente -> Metrocentro 18 min, origen = destino, sin ruta, peso invalido, estacion inactiva.
- [ ] 8. Documento: pseudocodigo con la pila, complejidad de Dijkstra, A*, Bellman-Ford.

## Reglas

- Tu codigo vive solo en `ruta/`. Depende solo de `nucleo`: `ArquitecturaTest` lo comprueba.
- Si necesitas cambiar un contrato de `nucleo`, abre un PR aparte y avisa por el chat del grupo.
- Un PR por tarea, que lo revise otra persona y con el CI en verde. Ver `CONTRIBUTING.md`.
- La guia completa de tareas esta en `docs/tareas-por-rol.md`.
