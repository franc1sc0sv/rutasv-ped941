package edu.udb.rutasv.nucleo.estructuras;

import edu.udb.rutasv.nucleo.temporal.ColaTemporal;

class ColaTemporalTest extends ColaContractTest {
    @Override
    protected Cola<Integer> crear() {
        return new ColaTemporal<>();
    }
}
