# Rol Terminal

**Objetivo:** Ordenar la salida de las unidades por prioridad (RN-4) y atender pasajeros por orden de llegada (RN-5).

## Estado final (Fase 3)

- `servicios/ReglaDespacho.java`: EXPRES primero, luego hora programada y, si empatan, el id.
- `servicios/Terminal.java`: unidades en un `MonticuloBinario` y una `TablaHash` id -> unidad (detecta repetidas en O(1) y permite ver el orden sobre una copia). Una `ColaEnlazada` por tipo de ruta para los pasajeros, en una `TablaHash`, y otra tabla id -> tipo para no repetir pasajeros.
- El monticulo y la cola enlazada (autoria de este rol) viven en `nucleo/estructuras/impl`; Dijkstra y las estadisticas tambien usan el monticulo.
- Pruebas: `DespachoFigura10Test` (U-014, U-031, U-009, U-022, U-005), `TerminalTest`, `TerminalOrdenTest`, `ReglaDespachoTest`, `MonticuloBinario*Test`, `ColaEnlazada*Test`.

## Por que estas estructuras

- Por que monticulo y no lista ordenada: insertar en una lista ordenada es O(n); en el monticulo, O(log n).
- Por que cola enlazada y no arreglo: encolar y desencolar son O(1) sin mover elementos ni redimensionar.
- `verOrdenDespacho` copia a un monticulo nuevo y extrae de la copia (O(n log n)) para no vaciar la cola real.

## Reglas

- El codigo del modulo depende solo de `nucleo` (`ArquitecturaTest` lo comprueba). Las estructuras genericas estan en `nucleo/estructuras/impl`.
- Un cambio de contrato de `nucleo` va en su propio PR y se avisa al equipo.
- Un PR por tarea, con el CI en verde. Ver `CONTRIBUTING.md`.
