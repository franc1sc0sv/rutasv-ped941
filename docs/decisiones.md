# Decisiones del proyecto

## Cambios respecto a la Fase 1

1. **Login, registro y roles.** Se agregaron PASAJERO y ADMIN. El registro siempre crea PASAJERO. El admin se crea al primer arranque con variables de entorno.
2. **Persistencia en archivos de texto.** Va detras de las interfaces `UsuarioRepositorio` y `HistorialRepositorio`. Se podria cambiar a una base de datos despues sin tocar el resto.
3. **Estructuras temporales.** Hay adaptadores con `java.util` para que el sistema funcione mientras cada companero escribe la suya.
4. **Sin framework.** El core no depende de ninguna libreria, salvo JUnit 5 en pruebas.

## Decisiones pendientes

- Interfaz de usuario: JavaFX o React (con una capa HTTP).
- Si se usa base de datos en lugar de archivos.
