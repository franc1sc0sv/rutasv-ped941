# Rol Historial

**Objetivo:** Guardar cada consulta por usuario, y mostrar la red y las estadisticas.

Tu issue: https://github.com/franc1sc0sv/rutasv-ped941/issues/4 (asignatela y marca las casillas).

## Empieza aqui

```bash
git clone https://github.com/franc1sc0sv/rutasv-ped941.git && cd rutasv-ped941
mvn verify                       # debe terminar en BUILD SUCCESS
git checkout -b feat/historial-<tarea>
```

## Ya esta creado para ti (esqueletos con `TODO`)

- `estructuras/ListaDinamica.java` implementa `Lista`

Las pruebas ``ListaDinamicaContratoTest`` (en `src/test/.../historial/estructuras/`) ya estan conectadas y tienen `@Disabled`. Cuando tu clase este lista, quita esa linea: el test de contrato te dice si cumples.

## Te toca crear

- `repositorios/HistorialRepositorioArchivo.java`: implementa `HistorialRepositorio` (ya existe la interfaz); guarda y carga rutas con fecha y tiempo (RN-7) y usuario; no guarda si no hay ruta (RN-6)
- `servicios/`: historial por usuario y estadisticas (conteo por par con tabla hash, top 5 con cola de prioridad, tiempo promedio)
- Conectalo en `SistemaTransporteImpl` (`miHistorial`, `verRed`, `agregarTramo`, `estadisticas`)

## Mientras otros terminan lo suyo

Usa `ListaTemporal`, `MapaTemporal` y `ColaPrioridadTemporal` de `nucleo/temporal`. Para escribir el archivo puedes usar `nucleo/archivo/ArchivoAtomico`.

## Tus tareas (de la Fase 2)

- [ ] 1. Modelo de datos (**ya hecho**; revisalo).
- [ ] 2. Lista dinamica propia.
- [ ] 3. Historial de rutas por usuario con fecha y tiempo (RN-7). No guarda si no hay ruta (RN-6). Guardar y cargar archivo.
- [ ] 4. Estadisticas: conteo por par, top 5, tiempo promedio.
- [ ] 5. Pantalla Red vial (Fig 8).
- [ ] 6. Pantallas Mi historial e Historial global (Fig 7 y 11).
- [ ] 7. Pruebas: la lista crece al pasar su capacidad, 3 rutas quedan en orden, el top 5 sale ordenado.
- [ ] 8. Video del equipo y documento (recalcular datos de figuras 7, 8 y 11, M7).

## Reglas

- Tu codigo vive solo en `historial/`. Depende solo de `nucleo`: `ArquitecturaTest` lo comprueba.
- Si necesitas cambiar un contrato de `nucleo`, abre un PR aparte y avisa por el chat del grupo.
- Un PR por tarea, que lo revise otra persona y con el CI en verde. Ver `CONTRIBUTING.md`.
- La guia completa de tareas esta en `docs/tareas-por-rol.md`.
