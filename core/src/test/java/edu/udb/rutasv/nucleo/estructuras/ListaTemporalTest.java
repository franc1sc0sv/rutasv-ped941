package edu.udb.rutasv.nucleo.estructuras;

import edu.udb.rutasv.nucleo.temporal.ListaTemporal;

class ListaTemporalTest extends ListaContractTest {
    @Override
    protected Lista<Integer> crear() {
        return new ListaTemporal<>();
    }
}
