package edu.udb.rutasv.ruta.estructuras;

import edu.udb.rutasv.nucleo.estructuras.RedVial;
import edu.udb.rutasv.nucleo.estructuras.RedVialContractTest;

class GrafoListaAdyacenciaContratoTest extends RedVialContractTest {

    @Override
    protected RedVial crear() {
        return new GrafoListaAdyacencia();
    }
}
