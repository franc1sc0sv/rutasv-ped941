package edu.udb.rutasv.structures;

import edu.udb.rutasv.structures.temporal.PilaTemporal;

class PilaTemporalTest extends PilaContractTest {
    @Override
    protected Pila<Integer> crear() {
        return new PilaTemporal<>();
    }
}
