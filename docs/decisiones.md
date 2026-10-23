# Decisiones del proyecto

## Cambios respecto a la Fase 1

1. **Login, registro y roles.** Se agregaron PASAJERO y ADMIN. El registro siempre crea PASAJERO. El admin se crea al primer arranque con variables de entorno.
2. **Persistencia en archivos de texto.** Va detras de las interfaces `UsuarioRepositorio` y `HistorialRepositorio`. Se podria cambiar a una base de datos despues sin tocar el resto.
3. **Estructuras temporales.** Hay adaptadores con `java.util` para que el sistema funcione mientras cada companero escribe la suya.
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

## Decisiones pendientes

- Si se usa base de datos en lugar de archivos (Fase 3).
