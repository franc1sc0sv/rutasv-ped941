package edu.udb.rutasv.historial.estructuras;

import org.junit.jupiter.api.Disabled;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.ListaContractTest;

@Disabled("Quita esta linea cuando ListaDinamica este implementada")
class ListaDinamicaContratoTest extends ListaContractTest {

    @Override
    protected Lista<Integer> crear() {
        return new ListaDinamica<>();
    }
}
