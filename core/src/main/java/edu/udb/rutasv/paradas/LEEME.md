# Rol Paradas

**Objetivo:** Guardar las estaciones con acceso directo por codigo o nombre. Su tabla hash tambien la usa el login.

Tu issue: https://github.com/franc1sc0sv/rutasv-ped941/issues/5 (asignatela y marca las casillas).

## Empieza aqui

```bash
git clone https://github.com/franc1sc0sv/rutasv-ped941.git && cd rutasv-ped941
mvn verify                       # debe terminar en BUILD SUCCESS
git checkout -b feat/paradas-<tarea>
```

## Ya esta creado para ti (esqueletos con `TODO`)

- `estructuras/TablaHash.java` implementa `Mapa`

Las pruebas ``TablaHashContratoTest`` (en `src/test/.../paradas/estructuras/`) ya estan conectadas y tienen `@Disabled`. Cuando tu clase este lista, quita esa linea: el test de contrato te dice si cumples.

## Te toca crear

- `servicios/DirectorioEstaciones.java`: alta, edicion, activar y desactivar (RN-1), codigo y nombre unicos
- `repositorios/CargadorCsv.java`: lee `data/estaciones.csv` y `data/tramos.csv` con errores claros por linea
- Conectalo en `SistemaTransporteImpl` (`buscarEstacion`, `crearEstacion`, `editarEstacion`, `activarDesactivarEstacion`)

## Mientras otros terminan lo suyo

Usa `ListaTemporal` de `nucleo/temporal` para `claves()`.

## Ojo

Cuando `TablaHash` pase su contrato, cambia `new MapaTemporal<>()` por `new TablaHash<>()` en `app/configuracion/Aplicacion.java`: el login ya la usa.

## Tus tareas (de la Fase 2)

- [ ] 1. Repositorio y proyecto (**ya hecho**).
- [ ] 2. Tabla hash propia: funcion hash, encadenamiento, redimension con factor 0.75.
- [ ] 3. Directorio de estaciones: alta, edicion, activar/desactivar (RN-1). Codigo y nombre unicos.
- [ ] 4. Carga CSV de estaciones y tramos con errores claros por linea.
- [ ] 5. Pantalla Registro de paradas (Fig 9).
- [ ] 6. Pruebas: 1000 claves antes y despues de redimensionar, colisiones, eliminar inexistente, codigos repetidos, CSV mal formado.
- [ ] 7. Tabla de casos de prueba del equipo.
- [ ] 8. Documento seccion 2.2 tabla hash.

## Reglas

- Tu codigo vive solo en `paradas/`. Depende solo de `nucleo`: `ArquitecturaTest` lo comprueba.
- Si necesitas cambiar un contrato de `nucleo`, abre un PR aparte y avisa por el chat del grupo.
- Un PR por tarea, que lo revise otra persona y con el CI en verde. Ver `CONTRIBUTING.md`.
- La guia completa de tareas esta en `docs/tareas-por-rol.md`.
