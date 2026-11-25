# Decisiones del proyecto

## Cambios respecto a la Fase 1

1. **Login, registro y roles.** Se agregaron PASAJERO y ADMIN. El registro siempre crea PASAJERO. El admin se crea al primer arranque con variables de entorno.
2. **Persistencia en archivos de texto.** Va detras de las interfaces `UsuarioRepositorio` y `HistorialRepositorio`. Se podria cambiar a una base de datos despues sin tocar el resto.
3. **Estructuras temporales (solo Fase 2).** Hubo adaptadores con `java.util` para que el sistema funcionara mientras cada companero escribia la suya. En la Fase 3 se borraron y las estructuras genericas pasaron a `nucleo/estructuras/impl`.
4. **Sin framework en el core.** El core no depende de ninguna libreria, salvo JUnit 5 en pruebas.
5. **Autocompletado con AVL propio** en lugar de `TreeMap` (seccion 2.7, M5).
6. **Coordenadas en las estaciones.** `Estacion` guarda `x` e `y` para dibujar el mapa. La Fase 1 decia que A* no era posible porque el modelo no tenia coordenadas: ahora si las tiene, asi que A* queda como mejora para la Fase 3.

## Decisiones de la Fase 2

1. **Interfaz en React.** El documento de la Fase 1 no fija tecnologia de interfaz (solo bocetos). El equipo eligio React con una capa HTTP delgada:
   - `api/` (Javalin) traduce cada pedido HTTP a una llamada de `SistemaTransporte`. No tiene logica.
   - `frontend/` (React + Vite + TypeScript) tiene una carpeta por rol.
   - El core no cambio: sigue sin conocer ninguna interfaz (`ArquitecturaTest`).
   - Por que: el equipo ya conoce React, las pantallas se ven igual en cualquier computadora y el video se graba desde el navegador.
   - El plan interno de organizacion mencionaba JavaFX; ese plan se actualiza, el documento entregado no cambia en esto.
2. **`verRed` lo puede leer cualquier sesion.** La pantalla Resultado de ruta (Fig. 6) necesita los minutos de cada tramo y las coordenadas, que `RutaCalculada` no trae. `verRed` devuelve una vista de solo lectura; crear estaciones y tramos sigue siendo solo de ADMIN (RN-1).
3. **La fachada recibe `Modulos`.** Cada rol conecta su modulo agregando su campo en `app/servicios/Modulos.java` y armandolo en `Aplicacion`. Un modulo `null` responde "Pendiente".
4. **Un candado en la fachada.** La API atiende pedidos en paralelo y las estructuras propias no son seguras entre hilos. `SistemaTransporteImpl` usa un `ReentrantReadWriteLock`: lectura para consultar (sugerir, ver red, historial), escritura para cambiar. Calcular una ruta toma el de escritura porque guarda en el historial y en las recientes. `verFila` tambien, porque recorre la cola desencolando y volviendo a encolar. `verRed` devuelve una copia tomada bajo el candado.
5. **Busquedas recientes.** Funcion nueva respecto a la Fase 1: las ultimas 5 rutas calculadas por cada usuario ("Origen → Destino"), de la mas reciente a la mas antigua (`BusquedaService`). En la Fase 2 se guardaba cada texto tecleado; en la Fase 3 se cambio porque mostraba fragmentos sin sentido.
6. **Filas de pasajeros por tipo de ruta.** `Terminal` es la unica duena de RN-5: una cola FIFO por tipo de ruta (NORMAL y EXPRES) en una `TablaHash`, y otra tabla id -> tipo para que un pasajero no este dos veces en las filas. La Fase 1 hablaba de "pasajeros de una misma unidad"; se eligio una fila por tipo porque la unidad aun no tiene capacidad ni asientos en el modelo.
7. **Codigos y nombres normalizados.** Los codigos se guardan sin espacios y en mayusculas; los nombres se comparan sin mayusculas ni tildes con el mismo `nucleo.texto.Normalizador` en el directorio y en el AVL.
7. **Desempates fijos.** Dijkstra desempata por codigo de estacion y RN-4 por id de unidad, para que el resultado sea siempre el mismo y `verOrdenDespacho` no dependa del orden de llegada.
8. **`RutaCalculada` guarda codigos.** Los tramos son los codigos de estacion en orden, mas el usuario y el tiempo de calculo en nanosegundos (para las estadisticas de Historial).
9. **`buscarEstacion` busca por nombre y, si no aparece, por codigo**, sin distinguir mayusculas.
10. **Limites de los tramos.** Minutos enteros entre 1 y 1440 (un dia; tambien evita desbordes en Dijkstra) y km finitos no negativos. Los minutos fraccionarios se rechazan, no se redondean.

## Decisiones pendientes

- Si se usa base de datos en lugar de archivos (Fase 3).
