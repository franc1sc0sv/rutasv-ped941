# Contratos y como reemplazar una clase Temporal

Cada estructura tiene una interfaz en `edu.udb.rutasv.nucleo.estructuras` y un adaptador `...Temporal` en `nucleo.temporal`.

## Pasos

1. Crea tu clase en `estructuras/` de tu modulo (por ejemplo `paradas/estructuras/` para la tabla hash) e implementa la interfaz.
2. Manten la misma forma del constructor que la clase Temporal (por ejemplo, `ColaPrioridad` recibe un `Comparator<T>`).
3. Busca el test abstracto de tu contrato (por ejemplo `ListaContractTest`) y agrega una subclase de una linea:

   ```java
   class MiListaTest extends ListaContractTest {
       @Override protected Lista<Integer> crear() { return new MiLista<>(); }
   }
   ```
   Nota: `ColaPrioridadContractTest` usa `crear(Comparator<Integer>)` y `MapaContractTest` usa `Mapa<String,Integer>`.
4. Cambia el cableado en `Aplicacion` para usar tu clase.
5. Corre `mvn -B verify`, abre tu PR y marca la casilla de contratos.

## Reglas

- No cambies la interfaz sin avisar al equipo. Un cambio de contrato va en su propio PR.
- Las excepciones del contrato son no verificadas (`ColeccionVaciaException`, `IndiceFueraDeRangoException`, `ValidacionException`).
- No borres la clase Temporal hasta que todo el equipo ya use la propia.
