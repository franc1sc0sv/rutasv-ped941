package edu.udb.rutasv.ruta.estructuras;

import edu.udb.rutasv.nucleo.estructuras.Pila;
import edu.udb.rutasv.nucleo.estructuras.PilaContractTest;

class PilaEnlazadaContratoTest extends PilaContractTest {

    @Override
    protected Pila<Integer> crear() {
        return new PilaEnlazada<>();
    }
}
