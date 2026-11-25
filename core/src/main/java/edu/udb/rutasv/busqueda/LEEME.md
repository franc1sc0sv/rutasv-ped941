# Rol Busqueda

**Objetivo:** Autocompletar nombres de estaciones y recordar las ultimas busquedas.

## Estado final (Fase 3)

- `estructuras/ArbolAvl.java` (`IndiceNombres`): arbol AVL por nombre normalizado, con rotaciones LL, RR, LR y RL. Buscar, insertar y eliminar son O(L log n); `conPrefijo` es O(L log n + k). Solo guarda estaciones activas.
- `servicios/BusquedaService.java`: sugerencias (solo lee el AVL) y busquedas recientes: las ultimas 5 rutas calculadas por usuario, en una `TablaHash` usuario -> arreglo fijo.
- `Normalizador` (sin mayusculas ni tildes) vive en `nucleo/texto` porque tambien lo usa Paradas.
- Pruebas: `ArbolAvlTest`, `ArbolAvlContratoTest`, `BusquedaServiceTest`, `NormalizadorTest`.

## Por que estas estructuras

- Por que AVL y no tabla hash: una tabla hash no puede listar "todo lo que empieza con ter" sin recorrerla entera. El AVL esta ordenado y poda las ramas que no comparten el prefijo.
- Por que AVL y no un arbol binario simple: con nombres insertados en orden, un ABB se vuelve una lista (O(n)). El AVL mantiene la altura en O(log n) (ver `docs/rendimiento.md`).
- Alternativa descartada: un trie. Seria O(L + k) por prefijo, pero gasta un nodo por letra y no estaba en el temario base.

## Reglas

- El codigo del modulo depende solo de `nucleo` (`ArquitecturaTest` lo comprueba). Las estructuras genericas estan en `nucleo/estructuras/impl`.
- Un cambio de contrato de `nucleo` va en su propio PR y se avisa al equipo.
- Un PR por tarea, con el CI en verde. Ver `CONTRIBUTING.md`.
