package edu.udb.rutasv.structures;

import edu.udb.rutasv.structures.temporal.ListaTemporal;

class ListaTemporalTest extends ListaContractTest {
    @Override
    protected Lista<Integer> crear() {
        return new ListaTemporal<>();
    }
}
