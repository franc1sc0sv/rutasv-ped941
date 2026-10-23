# Rol Paradas

**Objetivo:** Guardar las estaciones con acceso directo por codigo o nombre. Su tabla hash tambien la usa el login.

Tu issue: https://github.com/franc1sc0sv/rutasv-ped941/issues/5 (asignatela y marca las casillas).

## Empieza aqui

```bash
git clone https://github.com/franc1sc0sv/rutasv-ped941.git && cd rutasv-ped941
mvn verify                       # debe terminar en BUILD SUCCESS
git checkout -b feat/paradas-<tarea>
```

## Que hay

- `servicios/CargadorCSV.java`: lee `data/estaciones.csv` y `data/tramos.csv` con errores por linea (hecho y probado).
- `servicios/DirectorioEstaciones.java`: alta y busqueda por codigo o nombre, codigo y nombre unicos (hecho). Falta edicion y activar/desactivar.
- Conectado en `SistemaTransporteImpl`: `buscarEstacion` y `crearEstacion`. `Aplicacion` ya carga los CSV en el directorio, la red y el autocompletado.

## Pendiente

- `estructuras/TablaHash.java` sigue siendo esqueleto; `TablaHashContratoTest` tiene `@Disabled`.
- `editar` y `activar/desactivar` en el directorio y en la fachada. Al desactivar, actualiza tambien la red (`red.agregarEstacion` con la estacion inactiva) para que Dijkstra la salte, y el indice de nombres.
- Revision del 23/10: la unicidad de nombre usa `toLowerCase()` sin quitar tildes, mientras el AVL si las quita ("Estacion" y "Estación" chocan en el indice); la columna `activa` del CSV toma cualquier valor distinto de `true` como inactiva sin avisar.

## Cuando `TablaHash` pase su contrato

Cambia `MapaTemporal` por `TablaHash` en todos estos lugares:

- `app/configuracion/Aplicacion.java`: usuarios (login), `MapaTemporal::new` de Dijkstra, `filas` y el mapa de `BusquedaService`.
- Constructores sin argumentos de `ruta/estructuras/GrafoListaAdyacencia` y `paradas/servicios/DirectorioEstaciones` (o pasa las tablas desde `Aplicacion`).
- `terminal/servicios/Terminal.unidadesEnEspera`.

## Tus tareas (de la Fase 2)

- [x] 1. Repositorio y proyecto (**ya hecho**).
- [ ] 2. Tabla hash propia: funcion hash, encadenamiento, redimension con factor 0.75.
- [ ] 3. Directorio de estaciones: alta, edicion, activar/desactivar (RN-1). Codigo y nombre unicos.
- [x] 4. Carga CSV de estaciones y tramos con errores claros por linea.
- [ ] 5. Pantalla Registro de paradas (Fig 9).
- [ ] 6. Pruebas: 1000 claves antes y despues de redimensionar, colisiones, eliminar inexistente, codigos repetidos, CSV mal formado.
- [ ] 7. Tabla de casos de prueba del equipo.
- [ ] 8. Documento seccion 2.2 tabla hash.

## Reglas

- Tu codigo vive solo en `paradas/`. Depende solo de `nucleo`: `ArquitecturaTest` lo comprueba.
- Si necesitas cambiar un contrato de `nucleo`, abre un PR aparte y avisa por el chat del grupo.
- Un PR por tarea, que lo revise otra persona y con el CI en verde. Ver `CONTRIBUTING.md`.
- La guia completa de tareas esta en `docs/tareas-por-rol.md`.
