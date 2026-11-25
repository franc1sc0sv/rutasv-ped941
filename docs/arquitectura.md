# Arquitectura

El sistema se organiza **por modulo**, uno por rol, mas un `nucleo` compartido y una unica puerta de entrada (`app`).

```
            frontend React  --HTTP-->  api/ (Javalin, sin logica)
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
   (contratos, estructuras propias en impl/, modelo, errores)
```

## Reglas (las hace cumplir `ArquitecturaTest`)

1. El core **no conoce ninguna interfaz**: nada de JavaFX, servlets ni frameworks HTTP.
2. `nucleo` no depende de ningun modulo.
3. Cada modulo depende **solo de `nucleo`**. Si Ruta necesita la cola de prioridad de Terminal, usa el contrato `ColaPrioridad` de `nucleo`, no la clase de `terminal`.
4. Solo `app` conoce a todos los modulos y los conecta (`Aplicacion`). Autoriza (RN-1) y delega.

## Por que asi

- **Abstraccion:** la interfaz de usuario solo habla con `SistemaTransporte`. El frontend React llega por la capa `api/`, que traduce cada pedido HTTP a una llamada de la fachada. El core no conoce ni a React ni a la API.
- **Menos conflictos:** cada companero trabaja en su carpeta y sus pruebas. Solo `app/` se comparte.
- **Estructuras compartidas sin romper las reglas:** las cinco estructuras genericas (lista, pila, cola, cola de prioridad y tabla hash) viven en `nucleo/estructuras/impl`. Cualquier modulo puede usarlas porque solo dependen de `nucleo`. Durante la Fase 2 habia adaptadores `Temporal` con `java.util` mientras cada rol escribia la suya; en la Fase 3 se eliminaron.
- **Pruebas de contrato:** cada implementacion pasa la prueba abstracta de su interfaz (`ListaContractTest`, `MapaContractTest`, ...), asi cualquier cambio interno se valida contra el mismo comportamiento.

## Carpetas dentro de cada modulo

Cada modulo ordena sus archivos por tipo, con las carpetas que necesite: `interfaces/`, `servicios/`, `repositorios/`, `modelo/`, `excepciones/` y `estructuras/` (ruta suma `algoritmos/`). Las carpetas vacias tienen un `package-info.java` que dice que va en ellas. Ejemplo ya completo: `auth/`.

## Concurrencia

La API atiende pedidos en paralelo. Las estructuras propias no son seguras entre hilos, asi que `SistemaTransporteImpl` las protege con un solo `ReentrantReadWriteLock` (lectura para consultar, escritura para cambiar). El login tiene su propio candado en `AuthServiceImpl`. Un modulo nuevo no necesita candados propios si solo se usa desde la fachada.
