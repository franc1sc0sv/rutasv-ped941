package edu.udb.rutasv.nucleo.estructuras;

import edu.udb.rutasv.nucleo.temporal.ColaPrioridadTemporal;
import java.util.Comparator;

class ColaPrioridadTemporalTest extends ColaPrioridadContractTest {
    @Override
    protected ColaPrioridad<Integer> crear(Comparator<Integer> comparador) {
        return new ColaPrioridadTemporal<>(comparador);
    }
}
