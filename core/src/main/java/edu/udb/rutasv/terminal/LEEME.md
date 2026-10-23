# Rol Terminal

**Objetivo:** Ordenar la salida de las unidades por prioridad y atender pasajeros por orden de llegada.

Tu issue: https://github.com/franc1sc0sv/rutasv-ped941/issues/3 (asignatela y marca las casillas).

## Empieza aqui

```bash
git clone https://github.com/franc1sc0sv/rutasv-ped941.git && cd rutasv-ped941
mvn verify                       # debe terminar en BUILD SUCCESS
git checkout -b feat/terminal-<tarea>
```

## Que hay (hecho y conectado)

- `estructuras/MonticuloBinario.java` (`ColaPrioridad` con `Comparator`) y `estructuras/ColaEnlazada.java` (`Cola`), con sus pruebas de contrato activas.
- `servicios/ReglaDespacho.java` (RN-4: expres primero, luego hora; si empatan, el id) y `servicios/Terminal.java` (unidades + fila FIFO, RN-5).
- Prueba de la Fig. 10: `DespachoFigura10Test` (salen U-014, U-031, U-009, U-022, U-005).
- Conectado en `SistemaTransporteImpl`: `registrarUnidad`, `despacharSiguiente`, `verOrdenDespacho`, `encolarPasajero`, `atenderPasajero`. Dijkstra (Ruta) tambien usa `MonticuloBinario`.

## Pendiente (revision del 23/10)

- Fila de pasajeros: hoy la fachada usa una cola por tipo de ruta (`Modulos.filas`) y la fila de `Terminal` no se usa. Decide un solo dueno de RN-5 (ver `docs/decisiones.md`).
- `Terminal` lanza `IllegalArgumentException` donde el resto usa `ValidacionException`.

## Tus tareas (de la Fase 2)

- [x] 1. Cola de prioridad propia (monticulo binario con `Comparator`).
- [x] 2. Cola FIFO enlazada.
- [x] 3. Regla de despacho RN-4.
- [x] 4. Terminal y fila de pasajeros (RN-5).
- [x] 5. `verOrdenDespacho` sin vaciar la cola real (extrae de una copia del monticulo).
- [x] 6. Pantalla Despacho (Fig 10): `frontend/src/modulos/terminal/Despacho.tsx`.
- [x] 7. Pruebas: U-014, U-031, U-009, U-022, U-005; terminal vacia; FIFO.
- [ ] 8. Documento y autocritica (secciones 2.3, 2.6, 5.2).

## Reglas

- Tu codigo vive solo en `terminal/`. Depende solo de `nucleo`: `ArquitecturaTest` lo comprueba.
- Si necesitas cambiar un contrato de `nucleo`, abre un PR aparte y avisa por el chat del grupo.
- Un PR por tarea, que lo revise otra persona y con el CI en verde. Ver `CONTRIBUTING.md`.
- La guia completa de tareas esta en `docs/tareas-por-rol.md`.
