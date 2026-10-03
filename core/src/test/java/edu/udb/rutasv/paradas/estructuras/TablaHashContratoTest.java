package edu.udb.rutasv.paradas.estructuras;

import org.junit.jupiter.api.Disabled;
import edu.udb.rutasv.nucleo.estructuras.Mapa;
import edu.udb.rutasv.nucleo.estructuras.MapaContractTest;

@Disabled("Quita esta linea cuando TablaHash este implementada")
class TablaHashContratoTest extends MapaContractTest {

    @Override
    protected Mapa<String, Integer> crear() {
        return new TablaHash<>();
    }
}
