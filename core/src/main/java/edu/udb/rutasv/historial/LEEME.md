# Rol Historial

**Objetivo:** Guardar las rutas calculadas por usuario (RN-7) y calcular estadisticas globales.

## Estado final (Fase 3)

- `servicios/HistorialService.java`: una `ListaDinamica` con todas las rutas y una `TablaHash` usuario -> lista. "Mi historial" es O(1) + O(k).
- `repositorios/HistorialRepositorioArchivo.java`: agrega una linea por ruta (O(1), sin reescribir el archivo). Al cargar, una linea danada se omite con un aviso.
- `servicios/EstadisticasService.java`: conteo por par en una `TablaHash` (O(R)) y top 5 con un monticulo acotado de 5 elementos (O(P log 5)).
- La lista dinamica (autoria de este rol) vive en `nucleo/estructuras/impl`.
- Pruebas: `ListaDinamica*Test`, `HistorialServiceTest`, `HistorialRepositorioArchivoTest`, `EstadisticasServiceTest`, `DatosDemoTest`.

## Por que estas estructuras

- Por que lista dinamica y no enlazada: el historial solo crece al final y se recorre en orden; el arreglo da O(1) amortizado y mejor localidad de memoria.
- Por que monticulo acotado para el top 5: guarda 5 elementos en vez de P (memoria O(1)). En tiempo gana solo con muchos pares (medido en `docs/rendimiento.md`).

## Reglas

- El codigo del modulo depende solo de `nucleo` (`ArquitecturaTest` lo comprueba). Las estructuras genericas estan en `nucleo/estructuras/impl`.
- Un cambio de contrato de `nucleo` va en su propio PR y se avisa al equipo.
- Un PR por tarea, con el CI en verde. Ver `CONTRIBUTING.md`.
