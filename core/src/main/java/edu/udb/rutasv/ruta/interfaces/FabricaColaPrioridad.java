package edu.udb.rutasv.ruta.interfaces;

import edu.udb.rutasv.nucleo.estructuras.ColaPrioridad;
import java.util.Comparator;

/**
 * Crea colas de prioridad vacias. Ruta no puede importar el montículo de Terminal, asi que lo
 * recibe asi desde {@code app}: {@code MonticuloBinario::new}.
 */
@FunctionalInterface
public interface FabricaColaPrioridad {

    <T> ColaPrioridad<T> crear(Comparator<T> comparador);
}
