# Rol Busqueda

**Objetivo:** Que el pasajero encuentre una estacion mientras escribe (autocompletado).

Tu issue: https://github.com/franc1sc0sv/rutasv-ped941/issues/2 (asignatela y marca las casillas).

## Empieza aqui

```bash
git clone https://github.com/franc1sc0sv/rutasv-ped941.git && cd rutasv-ped941
mvn verify                       # debe terminar en BUILD SUCCESS
git checkout -b feat/busqueda-<tarea>
```

## Ya esta creado para ti (esqueletos con `TODO`)

- `estructuras/ArbolAvl.java` implementa `IndiceNombres`

Las pruebas ``ArbolAvlContratoTest`` (en `src/test/.../busqueda/estructuras/`) ya estan conectadas y tienen `@Disabled`. Cuando tu clase este lista, quita esa linea: el test de contrato te dice si cumples.

## Te toca crear

- `servicios/`: busqueda por prefijo (inorden acotado) y normalizacion de texto (sin tildes, minusculas)
- Conectalo en `SistemaTransporteImpl.sugerirEstaciones` y `busquedasRecientes`

## Mientras otros terminan lo suyo

Usa `IndiceNombresTemporal` y `ListaTemporal` de `nucleo/temporal`.

## Tus tareas (de la Fase 2)

- [ ] 1. Contrato de interfaces (**ya hecho**; revisalo).
- [ ] 2. Arbol AVL propio con rotaciones LL, RR, LR, RL, altura y factor de balance.
- [ ] 3. Busqueda por prefijo (inorden acotado).
- [ ] 4. Normalizacion de texto.
- [ ] 5. Ventana base (menu, modo pasajero/admin).
- [ ] 6. Pantalla Buscar ruta (Fig 5).
- [ ] 7. Pruebas: 8 nombres alfabeticos quedan balanceados, prefijos `Ter`, `metro`, `xyz`, eliminar con rotacion.
- [ ] 8. Bitacora del equipo y documento (cambiar TreeMap por AVL propio, M5).

## Reglas

- Tu codigo vive solo en `busqueda/`. Depende solo de `nucleo`: `ArquitecturaTest` lo comprueba.
- Si necesitas cambiar un contrato de `nucleo`, abre un PR aparte y avisa por el chat del grupo.
- Un PR por tarea, que lo revise otra persona y con el CI en verde. Ver `CONTRIBUTING.md`.
- La guia completa de tareas esta en `docs/tareas-por-rol.md`.
