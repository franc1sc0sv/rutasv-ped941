package edu.udb.rutasv.nucleo.estructuras;

import edu.udb.rutasv.nucleo.temporal.PilaTemporal;

class PilaTemporalTest extends PilaContractTest {
    @Override
    protected Pila<Integer> crear() {
        return new PilaTemporal<>();
    }
}
