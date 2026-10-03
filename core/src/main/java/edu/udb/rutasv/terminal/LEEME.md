# Rol Terminal

**Objetivo:** Ordenar la salida de las unidades por prioridad y atender pasajeros por orden de llegada.

Tu issue: https://github.com/franc1sc0sv/rutasv-ped941/issues/3 (asignatela y marca las casillas).

## Empieza aqui

```bash
git clone https://github.com/franc1sc0sv/rutasv-ped941.git && cd rutasv-ped941
mvn verify                       # debe terminar en BUILD SUCCESS
git checkout -b feat/terminal-<tarea>
```

## Ya esta creado para ti (esqueletos con `TODO`)

- `estructuras/MonticuloBinario.java` implementa `ColaPrioridad` (recibe un `Comparator`)
- `estructuras/ColaEnlazada.java` implementa `Cola`

Las pruebas ``MonticuloBinarioContratoTest` y `ColaEnlazadaContratoTest`` (en `src/test/.../terminal/estructuras/`) ya estan conectadas y tienen `@Disabled`. Cuando tu clase este lista, quita esa linea: el test de contrato te dice si cumples.

## Te toca crear

- `servicios/`: regla RN-4 como `Comparator` (expres primero, luego hora programada mas proxima), `Terminal` con su fila FIFO (RN-5) y `verOrdenDespacho` sin vaciar la cola real
- Conectalo en `SistemaTransporteImpl` (`registrarUnidad`, `despacharSiguiente`, `verOrdenDespacho`, `encolarPasajero`, `atenderPasajero`)

## Mientras otros terminan lo suyo

Usa `ColaPrioridadTemporal` y `ColaTemporal` de `nucleo/temporal`.

## Ojo

Ruta usa `ColaPrioridad` para Dijkstra y Historial la usa para el top 5: respeta el contrato.

## Tus tareas (de la Fase 2)

- [ ] 1. Cola de prioridad propia (monticulo binario con `Comparator`).
- [ ] 2. Cola FIFO enlazada.
- [ ] 3. Regla de despacho RN-4.
- [ ] 4. Terminal y fila de pasajeros (RN-5).
- [ ] 5. `verOrdenDespacho` sin vaciar la cola real.
- [ ] 6. Pantalla Despacho (Fig 10).
- [ ] 7. Pruebas: U-014, U-031, U-009, U-022, U-005; terminal vacia; FIFO.
- [ ] 8. Documento y autocritica (secciones 2.3, 2.6, 5.2).

## Reglas

- Tu codigo vive solo en `terminal/`. Depende solo de `nucleo`: `ArquitecturaTest` lo comprueba.
- Si necesitas cambiar un contrato de `nucleo`, abre un PR aparte y avisa por el chat del grupo.
- Un PR por tarea, que lo revise otra persona y con el CI en verde. Ver `CONTRIBUTING.md`.
- La guia completa de tareas esta en `docs/tareas-por-rol.md`.
