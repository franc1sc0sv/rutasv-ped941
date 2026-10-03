package edu.udb.rutasv.ruta.estructuras;

import org.junit.jupiter.api.Disabled;
import edu.udb.rutasv.nucleo.estructuras.RedVial;
import edu.udb.rutasv.nucleo.estructuras.RedVialContractTest;

@Disabled("Quita esta linea cuando GrafoListaAdyacencia este implementada")
class GrafoListaAdyacenciaContratoTest extends RedVialContractTest {

    @Override
    protected RedVial crear() {
        return new GrafoListaAdyacencia();
    }
}
