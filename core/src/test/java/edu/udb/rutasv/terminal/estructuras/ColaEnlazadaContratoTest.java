package edu.udb.rutasv.terminal.estructuras;

import edu.udb.rutasv.nucleo.estructuras.Cola;
import edu.udb.rutasv.nucleo.estructuras.ColaContractTest;

class ColaEnlazadaContratoTest extends ColaContractTest {

    @Override
    protected Cola<Integer> crear() {
        return new ColaEnlazada<>();
    }
}
