package edu.udb.rutasv.structures;

import edu.udb.rutasv.structures.temporal.RedVialTemporal;

class RedVialTemporalTest extends RedVialContractTest {
    @Override
    protected RedVial crear() {
        return new RedVialTemporal();
    }
}
