package edu.udb.rutasv.nucleo.estructuras.impl;

import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.ListaContractTest;

class ListaDinamicaContratoTest extends ListaContractTest {

    @Override
    protected Lista<Integer> crear() {
        return new ListaDinamica<>();
    }
}
