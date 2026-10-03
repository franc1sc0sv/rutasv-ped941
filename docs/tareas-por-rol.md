# Tareas por rol

Roles: Ruta, Paradas, Busqueda, Terminal, Historial.

## Ruta
1. Grafo con lista de adyacencia (`agregarEstacion`, `agregarTramo`, `vecinos`). Rechaza pesos <= 0 (RN-2). Un tramo por sentido (RN-3).
2. Pila enlazada propia.
3. Dijkstra con cola de prioridad y tablas hash de peso y anterior. Salta estaciones inactivas. Avisa si no hay ruta (RN-6).
4. Reconstruccion con la pila y `RutaCalculada`.
5. Tiempo de calculo con `System.nanoTime`.
6. Pantalla Resultado de ruta (Fig 6).
7. Pruebas: Occidente->Metrocentro 18 min, origen=destino, sin ruta, peso invalido, estacion inactiva.
8. Documento: pseudocodigo con la pila, complejidad de Dijkstra, A*, Bellman-Ford.

**Necesita:** lista dinamica, tabla hash, cola de prioridad. **Desbloquea:** Busqueda e Historial.

## Paradas
1. Repositorio y proyecto (YA HECHO por el lead).
2. Tabla hash propia: funcion hash, encadenamiento, redimension con factor 0.75. `poner`, `obtener`, `contiene`, `eliminar`, `claves`.
3. Directorio de estaciones: alta, edicion, activar/desactivar (RN-1). Codigo y nombre unicos.
4. Carga CSV de estaciones y tramos con errores claros por linea.
5. Pantalla Registro de paradas (Fig 9).
6. Pruebas: 1000 claves antes y despues de redimensionar, colisiones, eliminar inexistente, codigos repetidos, CSV mal formado.
7. Tabla de casos de prueba del equipo.
8. Documento seccion 2.2 tabla hash.

**Nota:** la tabla hash propia tambien se usa en el login. **Desbloquea:** Ruta (tablas de peso y anterior) y el login.

## Busqueda
1. Contrato de interfaces (YA HECHO por el lead; revisar).
2. Arbol AVL propio con rotaciones LL, RR, LR, RL, altura y factor de balance.
3. Busqueda por prefijo (inorden acotado).
4. Normalizacion de texto.
5. Ventana base (menu, modo pasajero/admin).
6. Pantalla Buscar ruta (Fig 5).
7. Pruebas: 8 nombres alfabeticos balanceados, prefijos Ter, metro, xyz, eliminar con rotacion.
8. Bitacora y documento (cambiar TreeMap por AVL propio, M5).

**Necesita:** Ruta (para la pantalla de rutas). **Desbloquea:** la interfaz base.

## Terminal
1. Cola de prioridad propia (monticulo binario con `Comparator`).
2. Cola FIFO enlazada.
3. Regla RN-4: expres primero, luego hora mas proxima.
4. Terminal y fila de pasajeros (RN-5).
5. `verOrdenDespacho` sin vaciar la cola real.
6. Pantalla Despacho (Fig 10).
7. Pruebas: U-014, U-031, U-009, U-022, U-005; terminal vacia; FIFO.
8. Documento y autocritica (secciones 2.3, 2.6, 5.2).

**Desbloquea:** Ruta (cola de prioridad para Dijkstra) e Historial (top 5).

## Historial
1. Modelo de datos (YA HECHO por el lead; revisar).
2. Lista dinamica propia.
3. Historial de rutas por usuario con fecha y tiempo (RN-7). No guarda si no hay ruta (RN-6). Guardar y cargar archivo.
4. Estadisticas: conteo por par con tabla hash, top 5 con cola de prioridad, tiempo promedio.
5. Pantalla Red vial (Fig 8).
6. Pantallas Mi historial e Historial global (Fig 7 y 11).
7. Pruebas.
8. Video y documento.

**Necesita:** Ruta (rutas calculadas), tabla hash y cola de prioridad. **Desbloquea:** estadisticas globales.
