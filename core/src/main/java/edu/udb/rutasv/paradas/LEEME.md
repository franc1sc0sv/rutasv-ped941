# Rol Paradas

**Objetivo:** Registrar, editar, activar y buscar estaciones (RN-1).

## Estado final (Fase 3)

- `servicios/DirectorioEstaciones.java`: alta, edicion y activar/desactivar. Dos `TablaHash`: por codigo y por nombre normalizado (sin mayusculas ni tildes). Todo es O(1) promedio. Valida codigo, nombre y coordenadas.
- `servicios/CargadorCSV.java`: lee `estaciones.csv` y `tramos.csv`. Una linea mala se reporta con su numero y no detiene la carga.
- La tabla hash propia (`TablaHash`, autoria de este rol, con aportes de dos integrantes: tope de capacidad y orden de insercion) vive en `nucleo/estructuras/impl` porque la usan todos los modulos.
- La fachada (`SistemaTransporteImpl.sincronizar`) lleva cada cambio al grafo y al arbol AVL.
- Pruebas: `TablaHashTest`, `TablaHashContratoTest`, `DirectorioEstacionesTest`, `CargadorCSVTest`, `ParadasYRobustezTest`.

## Por que estas estructuras

- Por que tabla hash y no arbol: el directorio solo busca por clave exacta (codigo o nombre). La tabla da O(1) promedio; un arbol daria O(log n). El orden alfabetico lo da el AVL de Busqueda.
- Encadenamiento y no sondeo lineal: borrar no necesita marcas y la tabla no se degrada con muchas altas y bajas.

## Reglas

- El codigo del modulo depende solo de `nucleo` (`ArquitecturaTest` lo comprueba). Las estructuras genericas estan en `nucleo/estructuras/impl`.
- Un cambio de contrato de `nucleo` va en su propio PR y se avisa al equipo.
- Un PR por tarea, con el CI en verde. Ver `CONTRIBUTING.md`.
