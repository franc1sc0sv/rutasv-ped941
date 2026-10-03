package edu.udb.rutasv.nucleo.estructuras;

import edu.udb.rutasv.nucleo.temporal.IndiceNombresTemporal;

class IndiceNombresTemporalTest extends IndiceNombresContractTest {
    @Override
    protected IndiceNombres crear() {
        return new IndiceNombresTemporal();
    }
}
