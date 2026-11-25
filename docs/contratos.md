# Contratos de las estructuras

Cada estructura tiene una interfaz en `edu.udb.rutasv.nucleo.estructuras` y una implementacion propia.

| Interfaz | Implementacion | Prueba de contrato |
|---|---|---|
| `Lista<T>` | `nucleo.estructuras.impl.ListaDinamica` | `ListaDinamicaContratoTest` |
| `Pila<T>` | `nucleo.estructuras.impl.PilaEnlazada` | `PilaEnlazadaContratoTest` |
| `Cola<T>` | `nucleo.estructuras.impl.ColaEnlazada` | `ColaEnlazadaContratoTest` |
| `ColaPrioridad<T>` | `nucleo.estructuras.impl.MonticuloBinario` | `MonticuloBinarioContratoTest` |
| `Mapa<K,V>` | `nucleo.estructuras.impl.TablaHash` | `TablaHashContratoTest` |
| `IndiceNombres` | `busqueda.estructuras.ArbolAvl` | `ArbolAvlContratoTest` |
| `RedVial` | `ruta.estructuras.GrafoListaAdyacencia` | `GrafoListaAdyacenciaContratoTest` |

## Agregar otra implementacion

1. Implementa la interfaz.
2. Agrega una subclase de una linea del test abstracto:

   ```java
   class MiListaTest extends ListaContractTest {
       @Override protected Lista<Integer> crear() { return new MiLista<>(); }
   }
   ```
   `ColaPrioridadContractTest` usa `crear(Comparator<Integer>)` y `MapaContractTest` usa `Mapa<String,Integer>`.
3. Cambia el cableado en `app/configuracion/Aplicacion`.
4. Corre `mvn -B verify`.

## Reglas

- No cambies una interfaz sin avisar al equipo. Un cambio de contrato va en su propio PR.
- Las excepciones del contrato son no verificadas (`ColeccionVaciaException`, `IndiceFueraDeRangoException`, `ValidacionException`).
