package edu.udb.rutasv.nucleo.estructuras;

import edu.udb.rutasv.nucleo.temporal.RedVialTemporal;

class RedVialTemporalTest extends RedVialContractTest {
    @Override
    protected RedVial crear() {
        return new RedVialTemporal();
    }
}
