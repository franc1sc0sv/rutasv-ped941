package edu.udb.rutasv.terminal.estructuras;

import org.junit.jupiter.api.Disabled;
import edu.udb.rutasv.nucleo.estructuras.Cola;
import edu.udb.rutasv.nucleo.estructuras.ColaContractTest;

@Disabled("Quita esta linea cuando ColaEnlazada este implementada")
class ColaEnlazadaContratoTest extends ColaContractTest {

    @Override
    protected Cola<Integer> crear() {
        return new ColaEnlazada<>();
    }
}
