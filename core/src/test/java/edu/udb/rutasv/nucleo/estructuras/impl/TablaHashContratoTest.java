package edu.udb.rutasv.nucleo.estructuras.impl;

import edu.udb.rutasv.nucleo.estructuras.Mapa;
import edu.udb.rutasv.nucleo.estructuras.MapaContractTest;

class TablaHashContratoTest extends MapaContractTest {

    @Override
    protected Mapa<String, Integer> crear() {
        return new TablaHash<>();
    }
}
