package edu.udb.rutasv.structures;

import edu.udb.rutasv.structures.temporal.IndiceNombresTemporal;

class IndiceNombresTemporalTest extends IndiceNombresContractTest {
    @Override
    protected IndiceNombres crear() {
        return new IndiceNombresTemporal();
    }
}
