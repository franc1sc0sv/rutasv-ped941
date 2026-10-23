# Rol Historial

**Objetivo:** Guardar cada consulta por usuario, y mostrar la red y las estadisticas.

Tu issue: https://github.com/franc1sc0sv/rutasv-ped941/issues/4 (asignatela y marca las casillas).

## Empieza aqui

```bash
git clone https://github.com/franc1sc0sv/rutasv-ped941.git && cd rutasv-ped941
mvn verify                       # debe terminar en BUILD SUCCESS
git checkout -b feat/historial-<tarea>
```

## Hecho

- `estructuras/ListaDinamica.java` implementa `Lista` sobre un arreglo que duplica su capacidad al llenarse. `ListaDinamicaContratoTest` ya corre sin `@Disabled`.
- `repositorios/HistorialRepositorioArchivo.java`: una linea por ruta en `historial.txt` (`origen;destino;E01>E02>E04;minutos;km;fecha;tiempoCalculoNs;usuario`), escrita con `ArchivoAtomico`. Una linea mal formada hace fallar el arranque con su numero de linea, igual que `usuarios.txt`.
- `servicios/HistorialService.java`: carga el historial al arrancar y devuelve las rutas de cada usuario en orden. Conectado en `Modulos` y en `miHistorial`.

## Te toca crear

- `servicios/`: estadisticas (conteo por par con tabla hash, top 5 con cola de prioridad, tiempo promedio)
- Conectalo: implementa `estadisticas`, y guarda la ruta en `SistemaTransporteImpl.calcularRuta` solo cuando el `Optional` tenga valor (RN-6 y RN-7; ver el comentario en ese metodo)
- La pantalla Red vial usa `verRed` y `agregarTramo`, que ya conecto Ruta

## Mientras otros terminan lo suyo

Usa `ListaTemporal`, `MapaTemporal` y `ColaPrioridadTemporal` de `nucleo/temporal`. Para escribir el archivo puedes usar `nucleo/archivo/ArchivoAtomico`.

## Tus tareas (de la Fase 2)

- [x] 1. Modelo de datos (**ya hecho**; revisalo).
- [x] 2. Lista dinamica propia.
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
