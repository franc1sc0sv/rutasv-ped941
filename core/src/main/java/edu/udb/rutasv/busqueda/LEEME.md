# Rol Busqueda

**Objetivo:** Que el pasajero encuentre una estacion mientras escribe (autocompletado).

Tu issue: https://github.com/franc1sc0sv/rutasv-ped941/issues/2 (asignatela y marca las casillas).

## Empieza aqui

```bash
git clone https://github.com/franc1sc0sv/rutasv-ped941.git && cd rutasv-ped941
mvn verify                       # debe terminar en BUILD SUCCESS
git checkout -b feat/busqueda-<tarea>
```

## Que hay (hecho y conectado)

- `estructuras/ArbolAvl.java` (`IndiceNombres`) con rotaciones LL, RR, LR y RL; su prueba de contrato esta activa.
- `servicios/Normalizador.java` (sin tildes, minusculas) y `servicios/BusquedaService.java` (sugerencias y hasta 5 busquedas recientes por usuario).
- Conectado en `SistemaTransporteImpl.sugerirEstaciones` y `busquedasRecientes`. Las estaciones que crea el admin entran al indice desde la fachada.

## Pendiente (revision del 23/10)

- `ArbolAvl` (estructura) importa `servicios.Normalizador`: conviene mover la normalizacion a `estructuras/` o inyectarla.
- `BusquedaService.sugerir` lanza `IllegalArgumentException`; el resto del core usa `ValidacionException`.
- El Javadoc de `IndiceNombres` (nucleo) da costos de trie, O(L); con AVL son O(log n) comparaciones y O(log n + k) por prefijo (seccion 2.7). Cambio de contrato: PR aparte.

## Tus tareas (de la Fase 2)

- [x] 1. Contrato de interfaces (**ya hecho**; revisalo).
- [x] 2. Arbol AVL propio con rotaciones LL, RR, LR, RL, altura y factor de balance.
- [x] 3. Busqueda por prefijo (inorden acotado).
- [x] 4. Normalizacion de texto.
- [ ] 5. Ventana base (menu, modo pasajero/admin).
- [ ] 6. Pantalla Buscar ruta (Fig 5).
- [x] 7. Pruebas: 8 nombres alfabeticos quedan balanceados, prefijos `Ter`, `metro`, `xyz`, eliminar con rotacion.
- [ ] 8. Bitacora del equipo y documento (cambiar TreeMap por AVL propio, M5).

## Reglas

- Tu codigo vive solo en `busqueda/`. Depende solo de `nucleo`: `ArquitecturaTest` lo comprueba.
- Si necesitas cambiar un contrato de `nucleo`, abre un PR aparte y avisa por el chat del grupo.
- Un PR por tarea, que lo revise otra persona y con el CI en verde. Ver `CONTRIBUTING.md`.
- La guia completa de tareas esta en `docs/tareas-por-rol.md`.
