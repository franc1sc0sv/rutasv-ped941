package edu.udb.rutasv.ruta.estructuras;

import org.junit.jupiter.api.Disabled;
import edu.udb.rutasv.nucleo.estructuras.Pila;
import edu.udb.rutasv.nucleo.estructuras.PilaContractTest;

@Disabled("Quita esta linea cuando PilaEnlazada este implementada")
class PilaEnlazadaContratoTest extends PilaContractTest {

    @Override
    protected Pila<Integer> crear() {
        return new PilaEnlazada<>();
    }
}
