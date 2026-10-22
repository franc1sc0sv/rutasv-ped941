package edu.udb.rutasv.busqueda.estructuras;

import edu.udb.rutasv.nucleo.estructuras.IndiceNombres;
import edu.udb.rutasv.nucleo.estructuras.IndiceNombresContractTest;

class ArbolAvlContratoTest extends IndiceNombresContractTest {

    @Override
    protected IndiceNombres crear() {
        return new ArbolAvl();
    }
}
