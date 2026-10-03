package edu.udb.rutasv.busqueda.estructuras;

import org.junit.jupiter.api.Disabled;
import edu.udb.rutasv.nucleo.estructuras.IndiceNombres;
import edu.udb.rutasv.nucleo.estructuras.IndiceNombresContractTest;

@Disabled("Quita esta linea cuando ArbolAvl este implementada")
class ArbolAvlContratoTest extends IndiceNombresContractTest {

    @Override
    protected IndiceNombres crear() {
        return new ArbolAvl();
    }
}
