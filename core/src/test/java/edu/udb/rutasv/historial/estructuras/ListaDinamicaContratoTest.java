package edu.udb.rutasv.historial.estructuras;

import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.ListaContractTest;

class ListaDinamicaContratoTest extends ListaContractTest {

    @Override
    protected Lista<Integer> crear() {
        return new ListaDinamica<>();
    }
}
