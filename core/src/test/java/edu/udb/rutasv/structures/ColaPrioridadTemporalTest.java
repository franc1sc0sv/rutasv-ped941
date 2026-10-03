package edu.udb.rutasv.structures;

import edu.udb.rutasv.structures.temporal.ColaPrioridadTemporal;
import java.util.Comparator;

class ColaPrioridadTemporalTest extends ColaPrioridadContractTest {
    @Override
    protected ColaPrioridad<Integer> crear(Comparator<Integer> comparador) {
        return new ColaPrioridadTemporal<>(comparador);
    }
}
