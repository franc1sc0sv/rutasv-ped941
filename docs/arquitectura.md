# Arquitectura

El sistema se organiza **por modulo**, uno por rol, mas un `nucleo` compartido y una unica puerta de entrada (`app`).

```
                 interfaz (JavaFX o API + React): se decide despues
                                  |
                                  v
                    app.SistemaTransporte  <- unica puerta de entrada
                                  |
      +--------+--------+--------+--------+--------+
      |        |        |        |        |        |
    auth     ruta    paradas  busqueda terminal historial
      \        \        |        /        /        /
       +--------+-------+-------+--------+--------+
                         nucleo
         (contratos, modelo, errores, temporales)
```

## Reglas (las hace cumplir `ArquitecturaTest`)

1. El core **no conoce ninguna interfaz**: nada de JavaFX, servlets ni frameworks HTTP.
2. `nucleo` no depende de ningun modulo.
3. Cada modulo depende **solo de `nucleo`**. Si Ruta necesita la cola de prioridad de Terminal, usa el contrato `ColaPrioridad` de `nucleo`, no la clase de `terminal`.
4. Solo `app` conoce a todos los modulos y los conecta (`Aplicacion`). Autoriza (RN-1) y delega.

## Por que asi

- **Abstraccion:** la interfaz de usuario solo habla con `SistemaTransporte`. Con JavaFX se llama directo. Con React se agrega una capa `api/` delgada que traduce HTTP a esas mismas llamadas. El core no cambia en ningun caso.
- **Menos conflictos:** cada companero trabaja en su carpeta y sus pruebas. Solo `app/` se comparte.
- **Reemplazo sin dolor:** las versiones `Temporal` de `nucleo/temporal` se sustituyen una por una en `Aplicacion`, y las pruebas de contrato dicen si la nueva cumple.

## Carpetas dentro de cada modulo

Cada modulo ordena sus archivos por tipo: `interfaces/`, `servicios/`, `repositorios/`, `modelo/`, `excepciones/` y `estructuras/` (ruta suma `algoritmos/`). Las carpetas vacias tienen un `package-info.java` que dice que va en ellas. Ejemplo ya completo: `auth/`.
