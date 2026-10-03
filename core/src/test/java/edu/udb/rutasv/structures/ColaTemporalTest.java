package edu.udb.rutasv.structures;

import edu.udb.rutasv.structures.temporal.ColaTemporal;

class ColaTemporalTest extends ColaContractTest {
    @Override
    protected Cola<Integer> crear() {
        return new ColaTemporal<>();
    }
}
